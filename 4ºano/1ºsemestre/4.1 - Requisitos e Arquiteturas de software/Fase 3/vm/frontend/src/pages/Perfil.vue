<template>
    <div class="profile-main">
      <!-- "NAVBAR" 
      <header class="project-header">
        <h1 class="logo">PICTURAS</h1>
        <div class="user-section">
          <div class="user-info">
            <span class="user-name">{{ userName }}</span>
            <span class="user-email">{{ email }}</span>
          </div>
          <button class="icon-profile">
            <img src="@/assets/user-solid.svg" alt="profile icon" />
          </button>
          <button class="icon-settings" @click="toggleMenu">
            <img src="@/assets/bars-solid.svg" alt="settings bar icon" />
          </button>
        </div>
      </header>

      <div v-if="menuVisible" class="menu">
            <ul>
                <li @click="goToProjects" class="menu-item">Página principal</li> 
                <li @click="goToEditProfile" class="menu-item">Editar perfil</li>
                <li @click="showLogoutPopup" class="menu-item">Sair</li>
            </ul>
        </div>
      -->

      <Navbar/>
  
      <!-- User profile view -->
      <div class="profile-container">
        <h2 class="profile-title">Perfil de Utilizador</h2>
  
        <div class="profile-view">
          <div class="form-member">
            <label for="fullname">Nome:</label>
            <p class="profile-field">{{ userName }}</p>
          </div>
          <div class="form-member">
            <label for="email">Email:</label>
            <p class="profile-field">{{ email }}</p>
          </div>
          <div class="form-member">
            <label for="subscription">Subscrição:</label>
            <p class="profile-field">{{ plan }}</p>
          </div>
        </div>
      </div>
    </div>
</template>

  
  <script setup>
  import { ref } from 'vue';
  import { getName, getUser, getPlan } from '@/utils/functions';
  import Navbar from '@/components/Navbar.vue';
  
  const userName = ref(getName());
  const email = ref(getUser());
  const plan = ref(getPlan());

  // Logout popup
const logoutPopup = ref(false)
const showLogoutPopup = () => {
    logoutPopup.value = true;
}


  // Menu lateral e navegação
    const menuVisible = ref(false);
    const toggleMenu = () => {
        menuVisible.value = !menuVisible.value;
    };

    const router = useRouter();

    const goToProjects = () => {
        router.push('/projects');  
    };

    // Navegar para o perfil
    const goToEditProfile = () => {
        router.push('/editprofile');
        menuVisible.value = false;  // Fechar o menu ao redirecionar
    };

  </script>
  
  <style scoped>
  .profile-main {
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
  
  .icon-profile,
  .icon-settings {
    cursor: pointer;
    transition: transform 0.2s ease;
  }
  
  .icon-profile img,
  .icon-settings img {
    width: 50px;
    height: 50px;
  }
  
  .icon-profile:hover,
  .icon-settings:hover {
    transform: scale(1.1);
  }
  
  .profile-container {
    background-color: #ffffff;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 2rem;
    border-radius: 10px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    margin: 2rem;
    width: 50%;
    max-width: 600px;
    margin: auto;
  }
  
  .profile-title {
    font-size: 1.5em;
    font-weight: bold;
    color: #1e40af;
    text-align: center;
    margin-bottom: 1rem;
  }
  
  .profile-view {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
  }
  
  .form-member {
    color: #000;
    display: flex;
    flex-direction: column;
  }
  
  label {
    font-weight: bold;
    margin-bottom: 0.5rem;
  }
  
  .profile-field {
    font-size: 1rem;
    color: #666;
    padding: 0.75rem;
    border: 1px solid #ccc;
    border-radius: 5px;
    background-color: #f5f5f5;
    margin-top: 0.25rem;
  }
  
  .profile-field[readonly] {
    background-color: #e9e9e9;
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
  </style>
  