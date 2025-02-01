<template>
  <div class="profile-main">
    <!-- "NAVBAR" -->
    <!--
    <header class="project-header">
      <h1 class="logo">PICTURAS</h1>
      <div class="user-section">
        <div class="user-info">
          <span class="user-name">{{ userName }}</span>
          <span class="user-email">{{ email }}</span>
        </div>
        <button class="icon-profile" @click="goToProfile">
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
    <!-- User profile form -->
    <div class="profile-container">
      <h2 class="profile-title">Perfil de Utilizador</h2>
      
      <!-- Mensagem de erro -->
      <p v-if="errorMessage" class="error-message">
        <i class="icon-warning">⚠️</i>
        {{ errorMessage }}
      </p>

      <div class="profile-form">
        <div class="form-member">
          <label for="fullname">Nome:</label>
          <input
            type="text"
            id="full-name"
            placeholder="Nome Completo"
            class="input-field"
            v-model="userName"
          />
        </div>
        <div class="form-member">
          <label for="email">Email:</label>
          <input
            type="text"
            id="email"
            placeholder="email"
            class="input-field"
            v-model="email"
          />
        </div>
        <div class="form-member">
          <label for="password">Password:</label>
          <input
            type="password"
            id="password"
            placeholder="********"
            class="input-field"
            v-model="password"
          />
        </div>

        <!-- Botão para alterar plano de subscrição -->
        <button class="btn change-plan-btn" @click="goToSubscription">
          Alterar plano de subscrição
        </button>

        <!-- Botões de ação -->
        <div class="action-buttons">
          <button class="btn save-btn" @click="editProfile">Editar e Guardar</button>
          <button class="btn cancel-btn" @click="cancel">Cancelar</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { getId, getName, getPlan, getUser } from '@/utils/functions';
import Navbar from '@/components/Navbar.vue';
import { useRoute } from 'vue-router';
const route = useRoute();

const userName = ref(getName());
const password = ref('');
const errorMessage = ref('');
const email = ref(getUser());
const id = ref(getId());
const currentPlan = getPlan();
const router = useRouter();
const plane = ref('free');

// Função para editar o perfil
const editProfile = async () => {
  try {
    // Obtém o novo plano do localStorage
    if (window.localStorage.getItem('newPlan')  == 'monthly' || window.localStorage.getItem('newPlan') == 'annual' ) {
      plane.value = "premium";
    }

    console.log('Plano atual:', currentPlan);
    console.log('Novo plano:', window.localStorage.getItem('newPlan'));
    console.log('Plano:', plane.value);

    const response = await axios.post(
      `/api/users/user/edit/${id.value}`, // Usando o método POST para editar o perfil
      {
        name: userName.value,
        email: email.value,
        password: password.value,
        type: plane.value || currentPlan, 
      },
      {
        headers: {
          Authorization: `Bearer ${window.localStorage.getItem('token')}`, // Adicionando o token de autenticação
        },
      },
      {
          withCredentials: true,
      }
    );

    if (response.status === 200) {
      console.log('Perfil atualizado:', response.data);
      window.localStorage.setItem('token', response.data.token); // Atualiza o token no localStorage
      alert('Perfil atualizado com sucesso!');

      //atualizar as subscrições
      try {

        console.log('userId:', getId());
        console.log('plan:', window.localStorage.getItem('newPlan'));

        if (window.localStorage.getItem('newPlan') === null || window.localStorage.getItem('newPlan') === undefined) {
          router.push('/projects'); // Redireciona para a página de projetos ou outra página
          return;
        }
        // SUBSCRIPTIONS cancel and post

        if (currentPlan != 'free') {
          const response3 = await axios.post(`/api/subscriptions/subscription/cancel/`, {
          userId: getId(),
          }, {
            withCredentials: true,
          });

          console.log('Data: ',response3.data);
        }
        
        if(window.localStorage.getItem('newPlan') != 'free') {

                const response2 = await axios.post(`/api/subscriptions/subscription/`, {
                    userId: getId(),
                    type: window.localStorage.getItem('newPlan'),
                    extra: JSON.stringify({
                      "transaction_id": "",
                      "receipt_url": ""
                  }),
                }, {
                    withCredentials: true,
                });
                console.log('Data: ',response2.data);

                if (response2.status === 200) {
                    console.log('Assinatura criada com sucesso:', response2.data);

                    const fromSubscription = route.query.fromSubscription === 'true';
                    console.log('From subscription:', fromSubscription);

                    if (fromSubscription){
                      router.push(`/payment?fromEdit=true`);
                    } else {
                      router.push('/projects'); // Redireciona para a página de projetos ou outra página
                    }
                } else {
                    alert('Erro ao criar a assinatura, tente novamente.');
                }
              } else {
                router.push('/projects');  // free vai para a página de login
              }
      } catch (error) {
        console.log(error);
        console.error('Erro ao criar a assinatura:', error.response?.data || error.message);
        alert('Erro ao criar a assinatura. Por favor, tente novamente.');
      }
    } else {
      errorMessage.value = 'Erro ao atualizar o perfil. Tente novamente.';
    }
  } catch (error) {
    console.error('Erro ao atualizar perfil:', error);
    errorMessage.value = 'Erro ao atualizar o perfil. Tente novamente.';
  }
  window.localStorage.removeItem('newPlan');
};

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

  const goToProjects = () => {
      router.push('/projects');  
  };

  // Navegar para o perfil
  const goToEditProfile = () => {
      router.push('/editprofile');
      menuVisible.value = false;  // Fechar o menu ao redirecionar
  };

// Função para cancelar a edição e voltar à página anterior
const cancel = () => {
  router.go(-1); // Volta à página anterior
};

// Função para redirecionar para o perfil
const goToProfile = () => {
  router.push('/perfil');
};

// Função para redirecionar para a página de assinatura (subscriptions)
const goToSubscription = () => {
  router.push('/subscriptions'); // Redireciona para a página de subscriptions
};
</script>

<style scoped>
/* Estilos existentes */
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

.project-title {
  color: #666;
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

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-member {
  display: flex;
  flex-direction: column;
}

label {
  font-weight: bold;
  margin-bottom: 0.5rem;
}

.input-field {
  padding: 0.75rem;
  border: 1px solid #ccc;
  border-radius: 5px;
  font-size: 1rem;
  color: #666;
}

.action-buttons {
  display: flex;
  gap: 1rem;
  margin-top: 1.5rem;
}

.btn {
  padding: 0.75rem 1.5rem;
  font-size: 1rem;
  font-weight: bold;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.save-btn {
  background-color: #1e40af;
  color: white;
}

.save-btn:hover {
  background-color: #153a91;
}

.cancel-btn {
  background-color: #ef4444;
  color: white;
}

.cancel-btn:hover {
  background-color: #dc2626;
}

.change-plan-btn {
  background-color: #4caf50;
  color: white;
  margin-top: 1.5rem;
}

.change-plan-btn:hover {
  background-color: #45a049;
}

.error-message {
  color: #e63946;
  font-size: 0.9rem;
  margin: -0.5rem 0 1rem;
  text-align: left;
  width: 100%;
}

.error-message .icon-warning {
  margin-right: 0.5rem;
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
