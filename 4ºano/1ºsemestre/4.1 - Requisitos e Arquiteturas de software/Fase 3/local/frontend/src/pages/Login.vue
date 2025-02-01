<template>
  <div class="login-container">
    <h1>PICTURAS</h1>
    <h2>Login</h2>
    <form @submit.prevent="handleLogin">
      <div class="input-group">
        <label for="email">E-mail:</label>
        <input
          type="text"
          id="email"
          v-model="email"
          placeholder="example@email.com"
          required
        />
      </div>
      <div class="input-group">
        <label for="password">Palavra-passe:</label>
        <input
          type="password"
          id="password"
          v-model="password"
          placeholder="********"
          required
        />
      </div>
      <p v-if="errorMessage" class="error-message">
        <i class="icon-warning">⚠️</i>
        {{ errorMessage }}
      </p>
      <div class="register-section">
        <p>Não tem uma conta? 
          <a @click.prevent="goToRegister">Registe-se aqui</a>.
        </p>
      </div>
      <button type="submit" class="submit-button"><span class="arrow">➔</span></button>
    </form>
  </div>
</template>

<script>
import { el } from 'vuetify/locale';
import axios from 'axios';
import { useWebSocketStore } from '@/stores/websocketStore';
import { getId } from '@/utils/functions';
  
export default {
  data() {
    return {
      email: '',
      password: '',
      errorMessage: '',
    };
  },
  methods: {
    onMounted() {
      localStorage.removeItem('name');
        localStorage.removeItem('newPlan');
        localStorage.removeItem('type');
        localStorage.removeItem('token');
        localStorage.removeItem('user');
    },
    async handleLogin() {
      try {
        const response = await axios.post('http://localhost:3000/api/users/user/login', {
          email: this.email,
          password: this.password,
        }, {
          withCredentials: true,
        });
        console.log(response.data);
        console.log(response.status);

        if (response.status === 200) {
          window.localStorage.setItem('token', response.data.token);
          this.errorMessage = '';

          const { connectWebSocket } = useWebSocketStore();
          const userId = getId();
          console.log(userId)
          connectWebSocket(userId)

          this.$router.push('/projects');
        } else {
          this.errorMessage = 'Credenciais não correspondem a nenhum utilizador. Por favor tente de novo.';
        }
      } catch (error) {
        this.errorMessage = 'Credenciais não correspondem a nenhum utilizador. Por favor tente de novo.';
      }
    },
    goToRegister() {
      this.$router.push('/chooseRegister');
    },
  },
};
</script>

<style scoped>
.login-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background-color: #c8dbe7; /* Azul claro */
  font-family: Arial, sans-serif;
  color: #1d3557;
}

h1 {
  font-size: 2.5rem;
  font-weight: bold;
  margin-bottom: 0.5rem;
}

h2 {
  font-size: 1.5rem;
  margin-bottom: 2rem;
}

form {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  max-width: 400px;
}

.input-group {
  display: flex;
  flex-direction: column;
  width: 100%;
  margin-bottom: 1rem;
}

label {
  margin-bottom: 0.5rem;
  font-size: 1rem;
}

input {
  padding: 0.8rem;
  font-size: 1rem;
  border: 1px solid #ccc;
  border-radius: 5px;
  background-color: white; /* Fundo branco para as caixas de texto */
}

input:focus {
  outline: none;
  border-color: #457b9d;
  color: #000; /* Texto preto quando o campo está em foco */
}

/* Quando o usuário começa a digitar (com texto inserido) */
input:not(:placeholder-shown) {
  color: #000; /* Texto preto quando o campo tem valor */
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

.submit-button {
  background-color: #1d3557;
  color: #fff;
  font-size: 1.5rem; 
  width: 60px; 
  height: 60px; 
  border-radius: 50%; /* Tornar o botão circular */
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background-color 0.3s;
}

.submit-button:hover {
  background-color: #457b9d;
}

.submit-button .arrow {
  font-size: 1.5rem; /* Tamanho da seta */
}

.register-section {
  margin-top: 1rem;
  text-align: center;
  margin-bottom: 1rem;
}

.register-section p {
  font-size: 1rem;
  color: #1d3557;
}

.register-section a {
  color: #457b9d;
  font-weight: bold;
  cursor: pointer;
  text-decoration: underline;
  transition: color 0.3s;
}

.register-section a:hover {
  color: #1d3557;
}

</style>