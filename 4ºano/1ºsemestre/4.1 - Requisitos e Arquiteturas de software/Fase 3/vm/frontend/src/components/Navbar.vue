@ -0,0 +1,146 @@
<script setup>
    import { ref } from 'vue';
    import { useRouter, useRoute } from 'vue-router';
    import { getId, getName, getPlan, getUser } from '@/utils/functions';
    import LogoutPopup from './LogoutPopup.vue';
    import { useWebSocketStore } from '@/stores/websocketStore';
    import axios from 'axios';

    const props = defineProps({
        projectTitle: {
            type: String,
            default: '',
        },
    });

    // user related
    const userName = ref(getName())
    const email = ref(getUser());

    const webSocketStore = useWebSocketStore();

    const router = useRouter();
    const menuVisible = ref(false);

    const toggleMenu = () => {
        menuVisible.value = !menuVisible.value;
    };

    const goToProjects = () => {
        router.push('/projects');
    };

    const goToProfile = () => {
        router.push('/perfil');
    };
    
    const goToEditProfile = () => {
        router.push('/editprofile');
        menuVisible.value = false;
    };
    
    const checkAnonymous = () => {
        return window.localStorage.getItem('type') !== 'anonymous';
    }

    const logoutPopup = ref(false);
    const showLogoutPopup = () => {
        toggleMenu();
        logoutPopup.value = true;
    };

    const close = () => {
        logoutPopup.value = false;
    }
    
    // Função para realizar o logout
    const logout = async () => {
        const token = localStorage.getItem('token');
        await axios.post(`/api/users/user/logout`, {}, { withCredentials: true });
        localStorage.removeItem('name');
        localStorage.removeItem('newPlan');
        localStorage.removeItem('type');
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        webSocketStore.closeWebSocket();
        router.push('/login');
    }

</script>

<template>
    <header class="navbar">
        <h1 class="logo" @click="goToProjects">PICTURAS</h1>
        <p> {{ projectTitle }}</p>
        <div class="user-section">
            <div class="user-info">
                <span class="user-name"> {{ userName }}</span>
                <span class="user-email"> {{ email }}</span>
            </div>
            <!-- Ícone de perfil com evento para redirecionar para a página de perfil -->
            <button  v-if="checkAnonymous()" class="icon-profile" @click="goToProfile">
                <img src="@/assets/user-solid.svg" alt="profile icon" />
            </button>
            <button class="icon-settings" @click="toggleMenu">
                <img src="@/assets/bars-solid.svg" alt="settings bar icon" />
            </button>
        </div>

        <!-- Sidebar Menu -->
        <div v-if="menuVisible" class="dropdown-menu">
            <div class="menu-item" @click="goToProjects">Página principal</div>
            <div class="menu-item" v-if="checkAnonymous()" @click="goToEditProfile">Editar perfil</div>
            <div class="menu-item" @click="showLogoutPopup">Logout</div>
        </div>
    </header>

    <LogoutPopup
            v-if="logoutPopup"
            @close="close"
            @confirm="logout"
        />

</template>
  
<style scoped>
    .navbar {
        display: flex;
        justify-content: space-between;
        align-items: center;
        background-color: #e9f1fe;
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
        cursor: pointer;
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
    
    .dropdown-menu {
        position: absolute;
        top: 90px; /* Depende do tamanho do icon */
        right: 10px;
        background-color: #6eb6ff;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
        border-radius: 4px;
        overflow: hidden;
        z-index: 1000;
    }

    .menu-item {
        padding: 10px 15px;
        cursor: pointer;
        transition: background-color 0.2s ease;
        white-space: nowrap;
    }

    .menu-item:hover {
        background-color: #e9f1fe;
    }
    
    p{
        color: black;
    }
</style>
  