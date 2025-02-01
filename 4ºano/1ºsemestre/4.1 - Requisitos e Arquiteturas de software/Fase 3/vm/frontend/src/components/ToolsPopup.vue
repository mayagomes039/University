<template>
    <div class="tools-popup-container">
        <div class="tools-popup">
            <h1 class="tools-popup-title">{{ title }}</h1>
            <div v-for="param in parametro" :key="param.id" class="param-group">
                <div v-if="param.type === 'number'">
                    <label>{{ param.name }}</label>
                    <input 
                        type="number" 
                        v-model="param.value" 
                        class="param-input" 
                        @input="validateParams"
                        @change="validateParams"
                    />
                </div>
                <div v-else-if="param.type === 'color'">
                    <label>{{ param.name }}</label>
                    <input type="color" v-model="param.value" class="param-input" />
                </div>
                <div v-else-if="param.type === 'file'">
                    <label>{{ param.name }}</label>
                    <input 
                        type="file" 
                        @change="handleFileChange($event, param)"
                        accept="image/*" 
                        class="param-input" 
                    />
                </div>
            </div>
            <div v-if="warningMessage" class="warning-message">{{ warningMessage }}</div>
            <div class="tools-popup-buttons">
                <button @click="$emit('close')" class="popup-button close-button">Fechar</button>
                <button 
                    @click="saveData" 
                    class="popup-button save-button" 
                    :disabled="isSaveDisabled"
                >
                    Guardar
                </button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">


const props = defineProps<{
    title: string;
    name: string;
    parametro: any[];
}>();


const emit = defineEmits(["close", "save"]);

const isSaveDisabled = ref(false);
const warningMessage = ref('');

const validateParams = () => {

    // Verifica cada parâmetro
    props.parametro.forEach(param => {
        const floatValue = parseFloat(param.value);
        console.log('Float value: ', floatValue);

        if (param.type === 'number') {
            isSaveDisabled.value = false;
            warningMessage.value = '';
            console.log('Validating number:',props.title);
            
            if (props.title.includes('borda')) {
                if (floatValue <= 0 || isNaN(floatValue)) {
                    warningMessage.value = "A largura da borda deve ser maior que 0.";
                    isSaveDisabled.value = true;
                    return;
                }
            }
            
            else if (props.title.includes('brilho') || props.title.includes('contraste')) {
                if (floatValue < 0 || floatValue > 2 || isNaN(floatValue)) {
                    warningMessage.value = "O valor de brilho ou contraste deve estar entre 0 e 2.";
                    isSaveDisabled.value = true;
                    return;
                }
            }

            else if (props.title.includes('ângulo')) {
                if (floatValue < 0 || floatValue >= 360 || isNaN(floatValue)) {
                    warningMessage.value = "O valor do ângulo deve estar entre 0 e 360.";
                    isSaveDisabled.value = true;
                    return;
                }
            }

            if (props.title.includes('marca d\'água')) {
                const imageParam = props.parametro.find(param => param.name === 'Imagem da Marca d\'Água:');
                if (imageParam && !imageParam.value) {
                    warningMessage.value = "Por favor, selecione uma imagem para a marca d'água.";
                    isSaveDisabled.value = true;
                    return false; 
                }

                const scaleFactorParam = props.parametro.find(param => param.name === 'Fator de Escala:');
                if (scaleFactorParam && parseFloat(scaleFactorParam.value) <= 0) {
                    warningMessage.value = "O fator de escala deve ser maior que 0.";
                    isSaveDisabled.value = true;
                    return false;
                }

                const opacityParam = props.parametro.find(param => param.name === 'Opacidade:');
                if (opacityParam && (parseFloat(opacityParam.value) < 0 || parseFloat(opacityParam.value) > 1)) {
                    warningMessage.value = "A opacidade deve estar entre 0 e 1.";
                    isSaveDisabled.value = true;
                    return false;
                }

                const positionXParam = props.parametro.find(param => param.name === 'Posição X:');
                if (positionXParam && parseFloat(positionXParam.value) <= 0) {
                    warningMessage.value = "A posição X deve ser maior que 0.";
                    isSaveDisabled.value = true;
                    return false;
                }

                const positionYParam = props.parametro.find(param => param.name === 'Posição Y:');
                if (positionYParam && parseFloat(positionYParam.value) <= 0) {
                    warningMessage.value = "A posição Y deve ser maior que 0.";
                    isSaveDisabled.value = true;
                    return false; 
                }
            }

             
            else if (props.title.includes("Fator")) {
                if (floatValue <= 0 || isNaN(floatValue)) {
                    warningMessage.value = "O fator deve ser maior que 0.";
                    isSaveDisabled.value = true;
                    return;
                }
            }

            else if (props.title.includes('corte')) {
                const left = parseFloat(props.parametro.find(param => param.name === 'Esquerda:').value);
                const upper = parseFloat(props.parametro.find(param => param.name === 'Superior:').value);
                const right = parseFloat(props.parametro.find(param => param.name === 'Direita:').value);
                const lower = parseFloat(props.parametro.find(param => param.name === 'Inferior:').value);

                if (
                    isNaN(left) || isNaN(upper) || isNaN(right) || isNaN(lower) ||
                    left < 0 || upper < 0 || right <= left || lower <= upper
                ) {
                    warningMessage.value = "Os parâmetros de corte (corte à esquerda, superior, direita e inferior) devem ser válidos e satisfazer as condições: esquerda >= 0, superior >= 0, direita > esquerda e inferior > superior.";
                    isSaveDisabled.value = true;
                    return;
                }
            }

        }
        /*if (param.type === 'string' && param.name === "watermarkImageURI") {
            if (typeof param.value !== 'string' || param.value.trim() === '') {
                warningMessage = "A URI da imagem do watermark deve ser uma string válida.";
                isSaveDisabled = true;
            }
        }*/

        if (param.type === 'file' && param.value && param.value.length === 0) {
            warningMessage.value = "Por favor, selecione um arquivo.";
            isSaveDisabled.value = true;
            return;
        }
    });
    return !isSaveDisabled.value;
};

    const handleFileChange = (event: Event, param: any) => {
        const input = event.target as HTMLInputElement;

        input.setAttribute("accept", "image/*");

        if (input.files?.length) {
            param.value = input.files[0];
            console.log("Selected file:", param.value); 
        }
    };


const saveData = () => {
    isSaveDisabled.value = false;
    warningMessage.value = ''; 
    if (validateParams()) {
        // Se não houver erro de validação, então emite o evento de guardar
        props.parametro.forEach(param => {
            if (param.type === 'number') {
                const floatValue = parseFloat(param.value);
                console.log('Float value: ', floatValue);
                if (!isNaN(floatValue)) {
                    param.value = floatValue; 
                } else {
                    console.warn(`Valor inválido para conversão: ${param.value}`);
                }
            }
        });

        emit('save', props.name, props.parametro);
        emit('close');
    }
};
</script>

<style scoped>
.tools-popup-container {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
}

.tools-popup {
    background-color: #ffffff;
    border-radius: 15px;
    padding: 2rem;
    width: 400px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    display: flex;
    flex-direction: column;
    gap: 1rem;
    text-align: center;
}

.tools-popup-title {
    font-size: 1.5rem;
    font-weight: bold;
    color: #1e40af;
}

.param-group {
    display: flex;
    flex-direction: column;
    width: 100%;
}

.param-input,
.param-checkbox {
    padding: 0.5rem;
    border: 1px solid #d1d5db;
    border-radius: 5px;
    font-size: 1rem;
    width: 100%;
    color: #000000; 
}

.param-input[type="color"] {
    border: 1px solid #d1d5db;
    padding: 0;
    width: 40px;
}

.param-input[type="color"]:focus {
    outline: none;
    border-color: #1e40af; 
}

label {
    color: #000000; 
    font-size: 1rem;
    text-align: left;
}

.tools-popup-buttons {
    display: flex;
    justify-content: space-between;
    gap: 1rem;
}

.popup-button {
    padding: 0.5rem 1rem;
    border: none;
    border-radius: 5px;
    font-size: 1rem;
    cursor: pointer;
    transition: background-color 0.2s ease;
}

.close-button {
    background-color: #e5e7eb;
    color: #1f2937;
}

.close-button:hover {
    background-color: #d1d5db;
}

.save-button {
    background-color: #1e40af;
    color: #ffffff;
}

.save-button:hover {
    background-color: #1a376d;
}

.warning-message {
    color: #ff0000;
    font-size: 0.9rem;
    margin-top: 1rem;
    text-align: center;
}

.tools-popup-buttons {
    display: flex;
    justify-content: space-between;
    gap: 1rem;
}

.popup-button:disabled {
    background-color: #d1d5db;
    cursor: not-allowed;
}
</style>
