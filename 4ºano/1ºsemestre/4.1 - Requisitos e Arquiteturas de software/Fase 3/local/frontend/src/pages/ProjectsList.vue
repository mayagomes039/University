<script setup>
    import { onMounted, ref } from 'vue';
    import { useRouter } from 'vue-router';
    import ErrorPopup from '@/components/ErrorPopup.vue';
    import LogoutPopup from '@/components/LogoutPopup.vue';
    import createProjectPopup from '@/components/createProjectPopup.vue';
    import confirmDeletePopup from '@/components/confirmDeletePopup.vue';
    import { getId, getName, getPlan, getUser } from '@/utils/functions';
    import { useProjectStore } from '@/stores/projectStore'
    import Navbar from '@/components/Navbar.vue';

    const email = ref(getUser());
    const userName = ref(getName() || window.localStorage.getItem('name'));

    const router = useRouter();

    // Estado para controle do menu
    const deleteConfirmationMenu = ref(false)

    const createPopup = ref(false);
    const showCreatePopup = () => {
        createPopup.value = true;
    };

    const close = () => {
        createPopup.value = false;
        deleteConfirmationMenu.value = false;
    };

    const projectStore = useProjectStore();
    const projects = ref([]); // Substituir por dados reais de projetos

    onMounted( async () => {
        try {
            const userId = getId(); 
            console.log(userId)
            await projectStore.fetchProjects(userId);
        }
        catch (error) {
            console.log(error)
        }
    });

    const projectSelected = (project) => {
        // A lógica da seleção do projeto vai aqui
        router.push(`/project/${project.id}`);
    }

    const createProject = async (name) => {
        // A lógica de criação do projeto vai aqui
        try {
            const userId = getId(); 
            // it doesnt return one, need to change on the backend
            const newProject = await projectStore.createProject(userId, name);
            close();
            projectStore.fetchProjects(userId);
            // go to the project: router.push(`/project/${newProject.id}`);
        } catch (error) {
            console.log(error);
        }
    }
    const projectForDeletion = ref(null)
    const handleDelete = async (project) => {
        deleteConfirmationMenu.value = true;
        projectForDeletion.value = project
    }

    const confirmDelete = async () => {
        if (projectForDeletion.value) {
            try {
                await projectStore.deleteProject(projectForDeletion.value.id); // Call store's delete method
                deleteConfirmationMenu.value = false;
                projectForDeletion.value = null;
            } catch (error) {
                console.error('Failed to delete project:', error);
            }
        }
    };
</script>

<template>
    <div class="main-page">
        <!-- "NAVBAR" aqui"-->
        <Navbar
        
        />

        <div class="button-section">
            <button class="add-project-button" @click="showCreatePopup">
                Criar projeto
            </button>
        </div>

        <createProjectPopup
            v-if="createPopup"
            @close="close"
            @create="createProject"
        />

        <div class="projects-list">
            <confirmDeletePopup
                    v-if="deleteConfirmationMenu"
                    @close="close"
                    @confirm="confirmDelete"
                />
            <div v-if="projectStore.projects.length">
                <ProjectCard
                    v-for="project in projectStore.projects"
                    :key="project.id"
                    :project="project"
                    @select="projectSelected"
                    @delete="handleDelete"
                />
            </div>
            <p v-else class="empty">Começa por criar um projeto</p>
        </div>
    </div>
</template>

<style scoped>
    .main-page {
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

    .user-section {
        display: flex;
        align-items: center;
        gap: 2rem;
    }

    .user-info {
        display: flex;
        flex-direction: column;
        color: #666;
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

    .button-section{
        display: flex;
        justify-content: center;
        margin-top: 2rem;
    }

    .add-project-button {
        background-color: #1e40af;
        padding: 1rem;
        border-radius: 25px;
    }

    .projects-list {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(80%, 1fr));
        margin-top: 2rem;
        padding: 1rem;
    }

    .empty {
        color: #666;
        text-align: center;
        font-size: 1.2rem;
        font-weight: bold;
    }

    /* Estilos para o menu lateral */
    .menu {
        position: absolute;
        top: 50px;
        right: 0;
        background-color: #fff;
        box-shadow: -2px 2px 10px rgba(0, 0, 0, 0.1);
        padding: 1rem;
        border-radius: 10px;
        width: 200px;
    }

    .menu ul {
        list-style: none;
        padding: 0;
    }

    .menu li {
        padding: 1rem;
        cursor: pointer;
        transition: background-color 0.3s ease;
        color: black; /* Garantir que o texto será preto */
    }

    .menu li:hover {
        background-color: #f1f1f1;
    }
</style>
