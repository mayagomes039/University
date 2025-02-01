<script setup>
    import { onMounted, ref, watch } from 'vue';
    import ErrorPopup from '@/components/ErrorPopup.vue';
    import LogoutPopup from '@/components/LogoutPopup.vue';
    import ToolsPopup from '@/components/ToolsPopup.vue';
    import { getId, getName, getPlan, getUser } from '@/utils/functions';
    import { useRouter } from 'vue-router'; // Para navegação
    import { useProjectStore } from '@/stores/projectStore'
    import axios from 'axios';
    import Navbar from '@/components/Navbar.vue';
    import LoadingScreen from '@/components/LoadingScreen.vue';
    import draggable from 'vuedraggable'
    import { validateParams } from "@/utils/validateParams";
    import { useWebSocketStore } from '@/stores/websocketStore'

    const route = useRoute();
    const projectStore = useProjectStore()
    const webSocketStore = useWebSocketStore()
    /**
     * 
     * Popups
     * 
     */
    const fileSizeError = ref(false)
    const noSelection = ref(false)
    const fileName = ref("");
    
    // can be used for every popup
    const closePopup = () => {
        fileSizeError.value = false;
        fileName.value = '';
        noSelection.value = false;
        logoutPopup.value = false;
        toolsPopupVisible.value = false;
    };

    // for testing
    const simulateError1 = () => {
        fileName.value = "358×430.png";
        fileSizeError.value = true;
    };

    const simulateError2 = () => {
        noSelection.value = true;
    };

    // Logout popup
    const logoutPopup = ref(false)
    const showLogoutPopup = () => {
        logoutPopup.value = true;
    }

    // Placeholder data
    const projectTitle = "Projeto sem título";
    const email = ref(getUser());
    const userName = ref(getName());
    const userPlan = ref(getPlan())

    // previous placeholders
    const basicToolsPlaceholders = ref([ "Resize", "Border", "Brightness", "Contrast", "Rotate"]);
    const advancedToolsPlaceholders = ref(["Watermark","People Counter", "OCR", "Object Finder", "Glasses Filter", "Football Lines"]);

    // distinguish between basic and advanced
    const basicToolNames = ["crop", "scale", "border", "brightness", "contrast", "rotate"]
    const advancedToolNames = ["watermark", "autocrop", "removebg"]
    // "extracttext", "objectrecognition", "peoplecount",

    // needs default so it doesnt give an error at render
    const project = ref({
        name: '', 
        availableTools: [],
        project: {
            images: [], 
            tools: [],
        }
    });

    const projectId = ref(null)

    const fetchProject = async () => {
        projectId.value = route.params.id;
        try {
            const response = await axios.get(`http://localhost:3000/api/projects/project/${projectId.value}`, {withCredentials: true});
            project.value = response.data;

            // populate the tools queue
            if (project.value.project.tools && Array.isArray(project.value.project.tools)) {
                toolsQueue.value = project.value.project.tools.filter((tool) => tool.procedure !== null);
                toolsInfosQueue.value = project.value.project.tools.map((tool) => tool.parameters || {});
                //toolsQueue.value = project.value.project.tools.map((tool) => tool.procedure).filter((name) => name !== null);
                //toolsInfosQueue.value = project.value.project.tools.map((tool) => tool.parameters || {});
            }

            console.log(project.value)
        } catch (error) {
            console.error('Error fetching project:', error);
        }
    }

    /**
     * Images
     */
    const uploadType = ref('images');

    // to add an image
    const addNewImage = async () => {
        try {

            if (userPlan.value === 'free' && project.value.project.images.length >= 20) {
                fileSizeError.value = true;
                fileName.value = "Atingiu o limite de 20 imagens para este projeto.";
                return; 
            }
            
            const input = document.createElement('input');
            input.type = 'file';
            input.multiple = uploadType.value === 'images';
            input.accept = uploadType.value === 'images' ? 'image/*' : '.zip';
            
            // opens the file selection
            input.click();

            input.onchange = async (event) => {
                const files = Array.from(event.target.files);

                // doing this cuz above its not recognizing for some rason
                const isZipFile = files[0]?.type === 'application/zip' || files[0]?.name.endsWith('.zip');
            
                if (isZipFile) {
                    uploadType.value = 'zip';
                }

                if(userPlan.value === 'free' && files.length + project.value.project.images.length >= 20){
                    fileSizeError.value = true;
                    console.log('limite')
                    fileName.value = `Atingiu o limite de 20 imagens para este projeto. Você só pode adicionar ${20 - project.value.project.images.length} imagens.`;
                    return;
                }
                
                // so we can check file sizes
                const totalSize = files.reduce((sum, file) => sum + file.size, 0);
                const maxSize = 5 * 1024 * 1024; // max = 5MB
                
                // do it like this? or in general and we use totalSize
                const validFiles = [];
                const invalidFiles = [];
                
                files.forEach(file => {
                    if (file.size > maxSize) {
                        invalidFiles.push(file);
                    } else {
                        validFiles.push(file);
                    }
                });

                if (invalidFiles.length > 0) {
                    fileSizeError.value = true;
                    fileName.value = invalidFiles.map(f => f.name).join(', ');
                }
                if (validFiles.length > 0) {
                    try {
                        let response;
                        
                        if (uploadType.value === 'images') {
                            console.log("UPLOADING IMAGES")
                            response = await projectStore.uploadImages(validFiles, projectId.value);
                            if (response) {
                                //project.value.images.push(...response.images);
                            }

                        } else {
                            // MISSING THE VERIFICATIONS FOR ZIP
                            // when its a zip
                            if (!isZipFile) {
                                fileSizeError.value = true;
                                fileName.value = 'Por favor utiliza ficheiros zip';
                                return;
                            }
                            console.log("UPLOADING ZIP")
                            response = await projectStore.uploadZip(validFiles[0], projectId.value);
                            if (response) {
                                //value.images.push(...response.images);
                            }
                        }
                        fetchProject();
                    } catch (error) {
                        console.error('Error uploading files:', error);
                        // Handle error display to user
                    }
                }
            };
        } catch (error) {
            console.error('Error in file selection:', error);
        }
    };

    const removeImage = async (index) => {
        const imageId = project.value.project.images[index].id;
        console.log("removing", imageId)
        try {
            await projectStore.deleteImage(projectId.value, imageId);
            
            project.value.project.images.splice(index, 1);  
        } catch (error) {
            console.error(error);
            fileSizeError.value = true;
            fileName.value = 'Erro ao remover a imagem. Tente novamente.';
        }
    };

    const downloadImages = async () => {
        projectStore.downloadImage(project.value.project.name, project.value.project.id)
    }

    // Image related
    const selectedImage = ref(null)
    const selectedImageId = ref(null)

    const selectImage = (image, id) => {
        zoomLevel.value = 1;
        imagePosition.value = { x: 0, y: 0 };
        selectedImage.value = 'http://localhost:9000/' + image;
        selectedImageId.value = id;
    };


    const processaFerramentas = async () => {
        if (toolsQueue.value.length === 0) {
            noSelection.value = true;
            return;
        }

        isLoading.value = true;

        try {
            const toolIds = toolsQueue.value.map(tool => tool.id);
            console.log("ids:", toolIds);

            const response = await projectStore.applyTools(projectId.value, toolIds);
            console.log('Tools processed:', response);

            if (response.message !== "Tool positions updated and images sent to queue") {
                isLoading.value = false;
                fileSizeError.value = true;
                fileName.value = 'Erro no processamento das ferramentas';
                //window.location.reload();
            }
        } catch (error) {
            console.error(error.message);
            isLoading.value = false;
            fileSizeError.value = true;
            fileName.value = 'Erro ao processar ferramentas. Tente novamente.';
        }
    };

    /**
     * 
     * Tools
     * 
     */

    //o que esta a mostra em baixo da pic
    const toolsQueue = ref([]);
    //parametros das tools de cima que estao guardados
    const toolsInfosQueue = ref([]);

    /*
    const removeImage = (index) => {
        imageList.value.splice(index, 1);
    };*/

    const router = useRouter();

    // Dados e métodos para o popup de ferramentas
    const toolsPopupVisible = ref(false);
    const toolsPopupTitle = ref("");
    const toolsPopupParams = ref([]);
    const toolPopupName = ref("");


    /**
     * have to change this now
     * have to adapt this will see it tomorrow or change the validate func
     */
    const openToolsPopup = (tool) => {
        toolsPopupVisible.value = true;
        toolPopupName.value = tool;

        if (tool === "border") {
            toolsPopupTitle.value = "Escolha a largura e cor da borda:";
            toolsPopupParams.value = [
                { id: 1, name: "Largura:", type: "number", value: "" },
                { id: 2, name: "Cor:", type: "color", value: "#000000" },
            ];
        } else if (tool === "brightness") {
            toolsPopupTitle.value = "Escolha o nível de brilho:";
            toolsPopupParams.value = [
                { id: 1, name: "Fator:", type: "number", value: "" },
            ];
        } else if (tool == "contrast"){
            toolsPopupTitle.value = "Escolha o nível de contraste:";
            toolsPopupParams.value = [
                { id: 1, name: "Fator:", type: "number", value: "" },
            ];
        } else if (tool == "scale"){
            toolsPopupTitle.value = "Escolha as dimensões:";
            toolsPopupParams.value = [
                { id: 1, name: "Largura:", type: "number", value: "" },
                { id: 2, name: "Altura:", type: "number", value: "" },
            ];
        } else if (tool == "crop") {
            toolsPopupTitle.value = "Escolha as dimensões do corte:";
            toolsPopupParams.value = [
                { id: 1, name: "Esquerda:", type: "number", value: "" },
                { id: 2, name: "Superior:", type: "number", value: "" },
                { id: 3, name: "Direita:", type: "number", value: "" },
                { id: 4, name: "Inferior:", type: "number", value: "" },
            ];
        } else if (tool == "rotate") {
            toolsPopupTitle.value = "Escolha o ângulo de rotação:";
            toolsPopupParams.value = [
                { id: 1, name: "Ângulo:", type: "number", value: "" },
            ];
        } else if (tool == "watermark") {
            toolsPopupTitle.value = "Escolha as configurações da marca d'água:";
            toolsPopupParams.value = [
                { id: 1, name: "Imagem da Marca d'Água:", type: "file", value: null },
                { id: 2, name: "Fator de Escala:", type: "number", value: "" },
                { id: 3, name: "Opacidade:", type: "number", value: "" },
                { id: 4, name: "Posição X:", type: "number", value: "" },
                { id: 5, name: "Posição Y:", type: "number", value: "" },
            ];
        } else if (tool === "autocrop") {
            toolsPopupTitle.value = "Auto Corte";
            toolsPopupParams.value = [];
        } else if (tool === "removebg") {
            toolsPopupTitle.value = "Remoção de Fundo";
            toolsPopupParams.value = [];
        }
    };

    const removeTool = async (index) => {
        const toolId = toolsQueue.value[index].id;
        console.log("imageid, ", toolId)
        toolsQueue.value.splice(index, 1);
        toolsInfosQueue.value.splice(index, 1);

        // for now ill put it here to test
        await removeToolFromQueue(toolId);
    };

    const savePopup = () => {
        toolsPopupVisible.value = false;
    };

    function hexToRgb(hex) {
        hex = hex.replace('#', '');
    
        const r = parseInt(hex.substring(0, 2), 16);
        const g = parseInt(hex.substring(2, 4), 16);
        const b = parseInt(hex.substring(4, 6), 16);

        return { r, g, b };
    }


    const saveChange = async (toolname, params) => {
        console.log("Saving tool:", toolname, params);
        toolsInfosQueue.value.push(params);
        //toolsQueue.value.push(toolname);
        toolsPopupVisible.value = false;

        const sanitizedParams = {};

        switch (toolname) {
        case "autocrop":
            break;
        case "removebg":
            break;

        case "crop":
        sanitizedParams.left = params[0].value;
        sanitizedParams.upper = params[1].value;
        sanitizedParams.right = params[2].value;
        sanitizedParams.lower = params[3].value;
        break;
    
        case "border":
        const colorHex = params[1].value;

        // Converter hexadecimal para RGB
        const { r, g, b } = hexToRgb(colorHex);

        // Agora, configurar os parâmetros de borda
        sanitizedParams.border_width = params[0].value;
        sanitizedParams.r = r;
        sanitizedParams.g = g;
        sanitizedParams.b = b;
        break;
    
        case "brightness":
        sanitizedParams.brightness_factor = params[0].value;
        break;
    
        case "contrast":
        sanitizedParams.contrast_factor = params[0].value;
        break;
    
        case "rotate": 
        sanitizedParams.angle = params[0].value;
        break;
    
        case "scale":
        sanitizedParams.new_width = params[0].value;
        sanitizedParams.new_height = params[1].value;
        break;
    
        case "watermark":
        //sanitizedParams.watermarkImageURI = params[0].value;
        sanitizedParams.scale_factor = params[1].value;
        sanitizedParams.opacity = params[2].value;
        sanitizedParams.positionX = params[3].value;
        sanitizedParams.positionY = params[4].value;
        break;
    
        default:
        break;
        }
        // estes nao precisam de ser validados
        if (toolname === "autocrop" || toolname === "removebg") {
            await addToolToQueue(toolname, sanitizedParams, "");
            return
        }

        if(validateParams(toolname, sanitizedParams)){
            await addToolToQueue(toolname, sanitizedParams, params[0].value);
            return
        }


        if (!validateParams(toolname, sanitizedParams)) {
            console.error(`Invalid parameters for tool: ${toolname}`, sanitizedParams);
            //popup maybe
            return;
        }

        const watermarkFile = params[0].value;
        console.log(watermarkFile)
        // for now ill put it here to test
        await addToolToQueue(toolname, sanitizedParams, watermarkFile);
    };

    const basicTools = computed(() =>
        project.value.availableTools.filter((tool) => basicToolNames.includes(tool))
    );

    const advancedTools = computed(() =>
        project.value.availableTools.filter((tool) => advancedToolNames.includes(tool))
    );

    // Isto nao tava a dar, provavelmente pelos parametros, ver isto
    const addToolToQueue = async (toolName, parameters, file) => {
        try {
            const formData = new FormData();
            formData.append('procedure', toolName);

            if (toolName === "watermark") {
                console.log("Appending file:", file);
                formData.append('file', file);
            }

            if (toolName === "autocrop" || toolName === "removebg") {
                formData.append('parameters', JSON.stringify({}));
            } else {
                formData.append('parameters', JSON.stringify(parameters));
            }


            const response = await projectStore.addTool(projectId.value, formData);
            console.log('Tool added:', response);

            const { toolId } = response;
            toolsQueue.value = [
                ...toolsQueue.value,
                { procedure: toolName, parameters, id: toolId }
            ];

            console.log(toolsQueue.value)
        } 
        catch (error) {
            console.error(error.message);
        }
    };

    const removeToolFromQueue = async (toolId) => {
        try {
            const response = await projectStore.removeTool(projectId.value, toolId);
            console.log('Tool removed:', response);
        } catch (error) {
            console.error(error.message);
        }
    };

    // id ja é passado
    const updateTool = async (toolId, updatedParameters) => {
        try {
            const response = await projectStore.updateTool(projectId.value, toolId, updatedParameters);
            console.log('Tool updated:', response);
        } catch (error) {
            console.error(error.message);
        }
    };

    /**
     * 
     * Image zoom
     */


    const zoomLevel = ref(1); // 1 = 100% zoom

    const handleWheel = (event) => {
        event.preventDefault();
        if (event.deltaY < 0) {
            // Zoom in
            zoomLevel.value = Math.min(zoomLevel.value + 0.1, 3); // max being 3x
        } else {
            // Zoom out
            zoomLevel.value = Math.max(zoomLevel.value - 0.1, 0.5); // min being 0.5x
        }
    };

    watch(zoomLevel, (newZoomLevel) => {
        // maybe display the zoom level to the user
        console.log(`Zoom level: ${newZoomLevel}`);
    });

    // prob not needed
    /*
    onUnmounted(() => {
        const imageContainer = document.querySelector('.image-container');
        if (imageContainer) {
            imageContainer.removeEventListener('wheel', handleWheel);
        }
    });*/

    /**
     * For dragging the image
     */
    const imagePosition = ref({ x: 0, y: 0 });
    const isDragging = ref(false);
    const startPosition = { x: 0, y: 0 };


    const handleMouseDown = (event) => {
        isDragging.value = true;
        startPosition.x = event.clientX - imagePosition.value.x;
        startPosition.y = event.clientY - imagePosition.value.y;
    };

    const handleMouseMove = (event) => {
        if (isDragging.value) {
            // calc new position
            const x = event.clientX - startPosition.x;
            const y = event.clientY - startPosition.y;

            imagePosition.value = { x, y };
        }
    };

    const handleMouseUp = () => {

        isDragging.value = false;
    };

    const socket = computed(() => webSocketStore.socket)
    const isLoading = ref(false)

    const setupSocketHandler = (socket) => {
        console.log("enter socket handler")
        if (!socket) return

        console.log("socket handler done")
        
        socket.onmessage = (event) => {
            const data2 = JSON.parse(event.data)
            const data = data2.data;


            console.log(data)

            console.log("data.projectId: ", data.projectId)
            console.log("projectId.value: ", projectId.value)
            
            if (data.projectId === projectId.value) {
                if (data.status === "failed") {
                    isLoading.value = false;
                    fileSizeError.value = true;
                    fileName.value = `Erro ao processar ferramenta: ${data.error}`;
                    fetchProject();
                }
                else if (data.status === "finished") {
                    console.log("finished, entering handler")
                    handleFinishedProcessing(data)
                }
            }
        }
    }

    const handleFinishedProcessing = (data) => {
        console.log("Processing finished for image:", data.imageId);

        // check if theres already a final img
        const existingImageIndex = project.value.project.images.findIndex(
            (img) => img.uri === data.finalImageURI
        );

        if (existingImageIndex !== -1) {
            // if the image isfinal and it exists we change it
            project.value.project.images[existingImageIndex] = {
                id: data.finalImageId, 
                uri: data.finalImageURI, 
                isFinal: true,
            };
        } else {
            // if its a new entry
            project.value.project.images.push({
                id: data.finalImageId,
                uri: data.finalImageURI, 
                isFinal: true, 
            });
        }

        // ff current image is selected, update it
        if (selectedImage.value && selectedImageId.value === data.imageId) {
            selectImage(data.finalImageURI);
        } else {

        }

        // count the number of finalized vs non final images
        const finalImagesCount = project.value.project.images.filter((img) => img.isFinal).length;
        const nonFinalImagesCount = project.value.project.images.filter((img) => !img.isFinal).length;

        if (finalImagesCount >= nonFinalImagesCount) {
            isLoading.value = false;
        }
    };

    // if a new socket
    watch(() => webSocketStore.socket, (newSocket) => {
        setupSocketHandler(newSocket)
    })

    /**
     * 
     * When page is launched
     */
    onMounted(async () => {
        fetchProject();

        const userID = getId();
        if (userID && !webSocketStore.isConnected) {
            console.log("Reconnecting WebSocket...");
            webSocketStore.connectWebSocket(userID);
        }

        // for zooming and dragging
        const imageContainer = document.querySelector('.image-preview');
        if (imageContainer) {
            imageContainer.addEventListener('wheel', handleWheel);
            imageContainer.addEventListener('mousedown', handleMouseDown);
            window.addEventListener('mousemove', handleMouseMove);
            window.addEventListener('mouseup', handleMouseUp);
        }

        // this prevents that when u drag the "ghost image" shows up
        imageContainer.addEventListener('dragstart', (event) => {
            event.preventDefault();
        });
        if (webSocketStore.socket) {
            setupSocketHandler(webSocketStore.socket)
        }
        else {
         console.log("no socket")
        }

        //listenForMessages()
    });

    const isProcessing = computed(() => isLoading.value);
    const updateToolOrder = (newQueue) => {
        console.log("Updated Tools Queue:", newQueue);
    };

    watch(() => webSocketStore.socket, (newSocket, oldSocket) => {
        if (newSocket) {
            console.log("New socket connection detected");
            setupSocketHandler(newSocket);
        } else if (oldSocket && !newSocket) {
            console.log("Socket disconnected");
        }
    });


    const addCacheFix = (url) => {
      return `${url}?nocache=${Date.now()}`; 
    }
    /**
     * 
     * Websockets
     * 
     */

     
    // websockets need to learn this
    // need to complete almost everything
    // https://blog.logrocket.com/build-real-time-vue-app-websockets/
</script>

<template>
    <div class="project-main">

        <Navbar
            :projectTitle="project.project.name"
        />
        <!-- Layout do projeto -->
        <div class="project-layout">
            <!-- Sidebard com as ferramentas -->
            <div class="tools-section">
                <h3 class="tools-title">Ferramentas Básicas</h3>
                <ul>
                    <li v-for="tool in basicTools" :key="tool" @click="openToolsPopup(tool)">{{ tool }}</li>
                </ul>

                <h3 class="tools-title">Ferramentas Avançadas</h3>
                <ul>
                    <li v-for="tool in advancedTools" :key="tool" @click="openToolsPopup(tool)">{{ tool }}</li>
                </ul>
            </div>

            <div class="edit-section">
                <!-- Seção de imagens -->
                <div class="images-section">
                    <div class="image-preview">
                        <img v-if="selectedImage" 
                            :src="addCacheFix(selectedImage)" 
                            :style="{ 
                                transform: `scale(${zoomLevel})`, 
                                position: 'absolute',
                                top: `${imagePosition.y}px`,
                                left: `${imagePosition.x}px`
                            }" 
                            alt="selected image"/>
                        <div v-else class="placeholder">Selecione uma imagem para a visualização</div>
                        <div v-if="isLoading" class="loading-overlay">
                            <LoadingScreen />
                        </div>
                    </div>

                    <div class="image-list-section">
                        <button class="add-image-button" @click="addNewImage">Adicionar Imagem</button>
                        <button class="add-image-button" @click="downloadImages">Download Projeto</button>
                        <ul>
                            <li v-for="(image, index) in project.project.images" :key="index" class="image-item">
                                <button class="remove-image-button" @click="removeImage(index)">✕</button>
                                <img :src="addCacheFix('http://localhost:9000/' + image.uri)" @click="selectImage(image.uri, image.id)" alt="Image" />
                            </li>
                        </ul>
                    </div>
                </div>
                <div class="tools-queue">
                <draggable 
                    v-model="toolsQueue" 
                    class="tools-list"
                    :item-key="tool => tool.id"
                    @end="updateToolOrder(toolsQueue)" 
                >
                    <template #item="{ element, index }">
                        <li class="tool-item" v-if="element !== null">
                            {{ element.procedure }}
                            <button class="remove-tool-button" @click="removeTool(index)">✕</button>
                        </li>
                    </template>
                </draggable>
                    <div class="add-tool-box">Clique numa ferramenta para a adicionar</div>
                </div>

                <div class="last-section">
                    <button 
                        class="process-button" 
                        @click="processaFerramentas()"
                        :disabled="isProcessing"
                    >
                        {{ isProcessing ? 'Processando...' : 'Processar todas' }}
                    </button>
                </div>

                <!-- Popups -->
                <ErrorPopup
                    v-if="fileSizeError"
                    :fileName="fileName"
                    title="Não é possível adicionar o seguinte ficheiro:"
                    message="O tamanho do ficheiro excede o permitido para esta conta. Por favor crie/atualize a sua conta de utilizador."
                    @close="closePopup"
                />

                <ErrorPopup
                    v-if="noSelection"
                    :fileName="fileName"
                    title="Não é possível avançar com o pedido."
                    message="Nenhuma imagem ou ferramenta selecionada."
                    @close="closePopup"
                />

                <ToolsPopup 
                    v-if="toolsPopupVisible"
                    :title="toolsPopupTitle"
                    :name="toolPopupName"
                    :parametro="toolsPopupParams"
                    @close="closePopup" 
                    @save="saveChange" 
                />
            </div>
        </div>
    </div>
</template>

<style scoped>
    .project-main {
        display: flex;
        flex-direction: column;
        background-color: #e9f1fe;
        padding: 2rem;
        height: 100vh;
    }

    .project-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
    }

    .logo {
        font-size: 2.5em;
        padding-left: 0.5rem;
        font-weight: bold;
        color: #1e40af;
    }

    .project-title {
        color: #666;
    }

    .user-section {
        display: flex;
        align-items: center;
        gap: 2rem;
    }

    .icon-profile, .icon-settings {
        cursor: pointer;
        transition: transform 0.2s ease;
    }

    .icon-profile img, .icon-settings img {
        width: 50px;
        height: 50px;
    }

    .icon-profile:hover, .icon-settings:hover {
        transform: scale(1.1);
    }

    .project-layout {
        display: flex;
        flex-direction: row;
        gap: 2.5rem;
        margin-top: 1rem;
    }

    .tools-section {
        padding: 1.5rem;
        border-radius: 2rem;
        width: 20rem;
        background-color: #8abaf6;
        display: flex;
        flex-direction: column;
        align-items: center; 
        gap: 1.5rem; 
        max-height: 95vh; 
        overflow-y: auto; 
    }

    .tools-title {
        color: white;
        background-color: #1e40af;
        padding: 0.75rem;
        border-radius: 1rem;
        text-align: center;
        font-weight: bold;
        font-size: 1.2rem;
        width: 100%;
    }


    .tools-section ul {
        list-style: none;
        padding: 0;
        margin: 0;
        width: 100%;
        display: flex;
        flex-direction: column; 
        align-items: center; 
        gap: 1rem; 
    }

    .tools-section li {
        width: 90%;
        background-color: white;
        color: #1e40af;
        padding: 0.75rem 1rem;
        border-radius: 0.5rem;
        text-align: center;
        font-weight: bold;
        font-size: 1rem;
        cursor: pointer;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        transition: all 0.3s ease;
    }

    .tools-section li:hover {
        background-color: #1e40af;
        color: white;
        transform: scale(1.05);
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
    }

    .edit-section {
        display: flex;
        flex-direction: column;
        flex: 1;
    }

    .images-section {
        display: flex;
        flex-direction: row;
        gap: 1rem;
        margin-bottom: 2rem

    }
    .loading-overlay {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: rgba(255, 255, 255, 0.8);
        display: flex;
        justify-content: center;
        align-items: center;
        z-index: 1000;
    }


    .image-preview {
        background-color: white;
        flex: 2;
        border-radius: 1rem;
        height: 40em;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 2rem;

        position: relative;
        overflow: hidden;
        width: 100%;
        height: 100%;
    }

    .image-preview img {
        max-width: 100%;
        max-height: 100%;
        object-fit: contain;

        cursor: pointer;  
        position: absolute;
        left: 0;
        top: 25;
        user-select: none;
    }

    .placeholder {
        color: #aaa;
        font-size: 1.2rem;
    }

    .image-list-section {
        height: 40rem;
        width: 20rem;
        background-color: white;
        padding: 1rem;
        border-radius: 1rem;
        overflow-y: auto;
        display: flex;
        flex-direction: column;
        align-items: center;
    }

    .image-list-section ul {
        display: flex;
        flex-direction: column;
        gap: 1rem;
        align-items: center;
    }

    .image-list-section img {
        width: 12rem;
        object-fit: cover;
        cursor: pointer;
        border: 2px solid transparent;
        border-radius: 0.5rem;
        transition: border-color 0.2s;
    }

    .image-list-section img:hover {
        border-color: #1e40af;
    }

    .tools-queue {
        display: flex;
        flex-wrap: wrap;
        justify-content: flex-start;
        align-items: center;
        background-color: #8abaf6;
        padding: 1.5rem;
        border-radius: 1rem;
        gap: 1rem; 
    }

    .tools-list {
        display: flex;
        flex-wrap: wrap; 
        gap: 1rem;
        list-style: none;
        padding: 0;
        margin: 0;
    }

    .tool-item {
        background-color: white;
        color: #1e40af;
        padding: 0.75rem 1rem;
        border-radius: 0.5rem;
        text-align: center;
        font-weight: bold;
        font-size: 1rem;
        cursor: pointer;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        transition: all 0.3s ease; 
        display: flex;
        justify-content: space-between; 
        align-items: center;
    }

    .tool-item:hover {
        background-color: #1e40af;
        color: white;
        transform: scale(1.05); 
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2); 
    }


    .add-tool-box {
        align-self: flex-end;
    }

    .last-section {
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        margin-top: 1rem;
    }

    .process-button {
        background-color: #1e40af;
        padding: 0.6rem;
        border-radius: 0.5rem;
        transition: background-color 0.3s ease;
        margin-bottom: 20px;
    }

    .process-button:hover {
        background-color: #143682;
    }

    .add-image-button {
        display: block;
        margin-bottom: 1rem;
        background-color: #1e40af;
        color: white;
        padding: 0.5rem 1rem;
        border-radius: 0.5rem;
        cursor: pointer;
        transition: background-color 0.3s ease;
    }

    .add-image-button:hover {
        background-color: #143682;
    }

    .image-item {
        position: relative;
    }

    .image-item img:hover {
        border-color: #1e40af;
    }

    .remove-image-button {
        position: absolute;
        top: 0.5rem;
        right: 0.5rem;
        background-color: red;
        color: white;
        border: none;
        border-radius: 50%;
        width: 1.5rem;
        height: 1.5rem;
        cursor: pointer;
        font-size: 1rem;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: background-color 0.2s ease;
    }

    .remove-image-button:hover {
        background-color: darkred;
    }

    .remove-tool-button {
        background-color: red;
        color: white;
        border: none;
        border-radius: 50%;
        width: 1.5rem;
        height: 1.5rem;
        cursor: pointer;
        font-size: 1rem;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-left: 0.5rem;
        transition: background-color 0.2s ease;
    }

    .remove-tool-button:hover {
        background-color: darkred;
    }
    /* Estilos do menu lateral */
    .menu {
        position: fixed;
        top: 0;
        right: 0;
        width: 200px;
        height: 100%;
        background-color: #fff;
        border-left: 1px solid #ddd;
        padding: 2rem;
        display: flex;
        flex-direction: column;
        gap: 1.5rem;
        box-shadow: -2px 0px 5px rgba(0, 0, 0, 0.2);
    }

    .menu ul {
        list-style: none;
        padding: 0;
        margin: 0;
    }

    .menu-item {
        padding: 1rem;
        cursor: pointer;
        transition: background-color 0.2s ease;
        color: #000; /* Texto preto */
    }

    .menu-item:hover {
        background-color: #e9f1fe;
    }

    .menu li {
        padding: 1rem;
        cursor: pointer;
        transition: background-color 0.2s ease;
    }

    .menu li:hover {
        background-color: #e9f1fe;
    }

    .user-info {
        display: flex;
        flex-direction: column; /* Garante que os itens vão para linhas diferentes */
    }

    .user-name,
    .user-email {
        color: black;
        margin: 0;
    }

    .process-button:disabled {
        background-color: #888;
        cursor: not-allowed;
    }
</style>