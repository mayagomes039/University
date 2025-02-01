<template>
    <div class="register-choice-container">
      <h1>PICTURAS</h1>
      <h2>Escolha uma opção de registo</h2>
      <div class="options">
        <button 
          class="option-button" 
          @click="navigateToRegister"
        >
          Registar-me
        </button>
        <button 
          class="option-button" 
          @click="navigateToRegisterAnonymous"
        >
          Registar-me como Anónimo
        </button>
      </div>
      <div class="login-section">
          <p>Já tem uma conta? 
            <a @click.prevent="goToLogin">Iniciar sessão</a>.
          </p>
        </div>
    </div>
  </template>
  
  <script>
  export default {
    methods: {
      navigateToRegister() {
        this.$router.push('/register'); // Redireciona para a página de registo normal
      },
      navigateToRegisterAnonymous() {
        this.$router.push('/registerAnonymous'); // Redireciona para a página de registo anónimo
      },
      goToLogin() {
        this.$router.push('/login');
      },
    },
  };
  </script>
  
  <style scoped>
  .register-choice-container {
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
  
  .options {
    display: flex;
    flex-direction: column;
    align-items: center;
  }
  
  .option-button {
    background-color: #457b9d;
    color: white;
    font-size: 1.2rem;
    padding: 1rem 2rem;
    margin: 0.5rem;
    border: none;
    border-radius: 5px;
    cursor: pointer;
    width: 80%;
    max-width: 400px;
    text-align: center;
    transition: background-color 0.3s;
  }
  
  .option-button:hover {
    background-color: #1d3557;
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
  