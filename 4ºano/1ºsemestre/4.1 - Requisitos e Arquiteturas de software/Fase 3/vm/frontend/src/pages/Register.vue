<template>
    <div class="register-container">
      <h1>PICTURAS</h1>
      <h2>Registo</h2>
      <form @submit.prevent="handleRegister">
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
        <div class="input-group">
          <label for="email">E-mail:</label>
          <input
            type="email"
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
        
        <div class="input-group">
          <label>Escolha o seu plano:</label>
          <div class="plans">
            <button 
              type="button"
              :class="{'selected': selectedPlan === 'free'}"
              @click="selectedPlan = 'free'"
            >
              Plano Gratuito
            </button>
            <button 
              type="button"
              :class="{'selected': selectedPlan === 'monthly'}"
              @click="selectedPlan = 'monthly'"
            >
              Plano Mensal
            </button>
            <button 
              type="button"
              :class="{'selected': selectedPlan === 'annual'}"
              @click="selectedPlan = 'annual'"
            >
              Plano Anual
            </button>
          </div>
        </div>
  
        <button 
          type="button" 
          class="navigate-button" 
          @click="navigateToSubscriptions"
        >
          Ver Detalhes dos Planos
        </button>
  
        <p v-if="errorMessage" class="error-message">
          <i class="icon-warning">⚠️</i>
          {{ errorMessage }}
        </p>
        <button type="submit" class="submit-button">
          <span class="arrow">➔</span>
        </button>
      </form>
      <div class="login-section">
          <p>Já tem uma conta? 
            <a @click.prevent="goToLogin">Iniciar sessão</a>.
          </p>
        </div>
    </div>
  </template>
  
  <script>
  import axios from 'axios';
  import { getId, getName, getPlan, getUser } from '@/utils/functions';

  const id = ref(getId());

  export default {
    data() {
      return {
        name: '',
        email: '',
        password: '',
        selectedPlan: 'free',  // Inicialmente o plano gratuito está selecionado
        planoRegisto: 'free',  // 'free' ou 'premium'
        errorMessage: '',
      };
    },
    methods: {
      async handleRegister() {
        if (!this.name || !this.email || !this.password) {
          this.errorMessage = 'Por favor preencha todos os campos.';
          return;
        }
  
        this.errorMessage = '';
        // Enviar dados de registro para o backend
      try {

        if (this.selectedPlan === 'monthly' || this.selectedPlan === 'annual'){
          this.planoRegisto = 'premium';
        }

        //USERS
        //const response = await axios.post(`/api/user/register`, {
        const response = await axios.post(`/api/users/user/register`, {
        name: this.name,
        email: this.email,
        password: this.password,
        type: this.planoRegisto,  // 'free' ou 'premium'
      },
      {
        withCredentials: true,
        });

        console.log('Resposta do backend:', response);

        if (response.status === 200) {
        window.localStorage.setItem('token', response.data.token);
        window.localStorage.setItem('user', JSON.stringify(response.data.user));

        this.errorMessage = '';

        if (this.selectedPlan === 'monthly' || this.selectedPlan === 'annual') {
          // SUBSCRIPTIONS
          try {
              console.log('userId:', getId());
                const response2 = await axios.post(`/api/subscriptions/subscription/`, {
                    userId: getId(),
                    type: this.selectedPlan,
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
                    this.$router.push('/payment'); // Redireciona para a página de pagamento
                } else {
                    alert('Erro ao criar a assinatura, tente novamente.');
                }
            } catch (error) {
                console.log(error);
                console.error('Erro ao criar a assinatura:', error.response?.data || error.message);
                alert('Erro ao criar a assinatura. Por favor, tente novamente.');
            }
        } else {
          alert(`Registo bem-sucedido! Plano escolhido: ${this.selectedPlan}`);
          this.$router.push('/login');  // free vai para a página de login
        }
        } else {
          alert('Erro no registro, por favor tente novamente.');
        }

      } catch (error) {
        console.error(error);
        if (error.response && error.response.status === 400) {
          this.errorMessage = 'E-mail já está em uso. Tente outro e-mail.';
        } else {
          this.errorMessage = 'Ocorreu um erro ao tentar registrar. Por favor, tente novamente.';
        }
      }
      },
      goToLogin() {
        this.$router.push('/login');
      },
      navigateToSubscriptions() {
        this.$router.push('/viewSubscriptions')
      }
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
  
  input, select {
    padding: 0.8rem;
    font-size: 1rem;
    border: 1px solid #ccc;
    border-radius: 5px;
    background-color: white; /* Fundo branco para as caixas de texto */
    color: #666;
  }
  
  input:focus, select:focus {
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
  
  .navigate-button {
    background-color: #457b9d;
    color: white;
    font-size: 1rem;
    padding: 0.8rem 1.5rem;
    border: none;
    border-radius: 5px;
    cursor: pointer;
    margin-bottom: 1rem;
    transition: background-color 0.3s;
  }
  
  .navigate-button:hover {
    background-color: #1d3557;
  }
  
  .plans {
    display: flex;
    justify-content: space-between;
    width: 100%;
    margin-top: 1rem;
  }
  
  .plans button {
    padding: 0.8rem 1.2rem;
    font-size: 1rem;
    background-color: #f1f1f1;
    border: 1px solid #ccc;
    border-radius: 5px;
    cursor: pointer;
    transition: background-color 0.3s, transform 0.2s;
    width: 30%;
    text-align: center;
  }
  
  .plans button:hover {
    background-color: #f0f0f0;
    transform: scale(1.05);
  }
  
  .plans button.selected {
    background-color: #457b9d;
    color: white;
    font-weight: bold;
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
  