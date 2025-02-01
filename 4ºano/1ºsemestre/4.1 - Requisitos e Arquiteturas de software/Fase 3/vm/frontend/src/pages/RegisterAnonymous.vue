<template>
    <div class="register-container">
      <h1>PICTURAS</h1>
      <h2>Registo anónimo</h2>
      <form @submit.prevent="handleRegisterAnonymous">
        <div class="input-group">
          <label for="name">Nome:</label>
          <input
            type="text"
            id="name"
            v-model="name"
            placeholder="Seu Nome Completo"
            required
          />
        </div>

        <p v-if="errorMessage" class="error-message">
          <i class="icon-warning">⚠️</i>
          {{ errorMessage }}
        </p>
        <div class="login-section">
          <p>Já tem uma conta? 
            <a @click.prevent="goToLogin">Iniciar sessão</a>.
          </p>
        </div>
        <button type="submit" class="submit-button">
          <span class="arrow">➔</span>
        </button>

      </form>
    </div>
  </template>
  
  <script>
  import axios from 'axios';

  export default {
    data() {
      return {
        name: '',
        errorMessage: '',
      };
    },
    methods: {
      goToLogin() {
        this.$router.push('/login');
      },
  async handleRegisterAnonymous() {
    // Verifica se o nome foi preenchido
    if (!this.name) {
      this.errorMessage = 'Por favor, insira o seu nome.';
      return;
    }

    // Se o nome estiver preenchido, preparamos os dados
    this.errorMessage = '';

    try {
      // Envia a requisição para o backend com tipo "anonymous"
      const response = await axios.post(`/api/users/user/register`, {
        name: this.name,
        email: "",          
        password: "",       
        type: "anonymous",  // Tipo fixo "anonymous"
      },
      {
        withCredentials: true,
      });

      console.log(response.data);

      if (response.status === 200) {
    
        window.localStorage.setItem('token', response.data.token);
      
        window.localStorage.setItem('type', 'anonymous');
        window.localStorage.setItem('name', this.name);


        this.errorMessage = '';
        console.log("window.localStorage", window.localStorage)

        // Redirecionar para a página projects diretamente
        this.$router.push('/projects'); 
      } else {
        alert('Erro ao registrar, por favor tente novamente.');
      }
    } catch (error) {
      console.error(error);
      this.errorMessage = 'Ocorreu um erro ao tentar registrar. Por favor, tente novamente.';
    }
  },
},

  };
  </script>
  
  <style scoped>
  .register-container {
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
    color: #666;
  }
  
  input:focus {
    outline: none;
    border-color: #457b9d;
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

  .login-section {
    margin-top: 1rem;
    text-align: center;
    margin-bottom: 1rem;
  }

  .login-section p {
    font-size: 1rem;
    color: #1d3557;
  }

  .login-section a {
    color: #457b9d;
    font-weight: bold;
    cursor: pointer;
    text-decoration: underline;
    transition: color 0.3s;
  }

  .login-section a:hover {
    color: #1d3557;
  }
  </style>
  