<template>
  <div class="payment-container">
    <h1>Formulário de Pagamento</h1>
    <h2>Preenche os seus dados para pagar a sua subscrição.</h2>

    <!-- Formulário de Multibanco -->
    <div v-if="paymentMethod === 'multibanco'" class="form-section">
      <form @submit.prevent="handlePaymentSubmit">
        <label for="address">Morada</label>
        <input 
          type="text" 
          id="address" 
          v-model="multibancoAddress" 
          placeholder="Ex: Rua das Flores, 123" 
          required
          style="color: black;" 
        />

        <label for="postalCode">Código Postal</label>
        <input 
          type="text" 
          id="postalCode" 
          v-model="multibancoPostalCode" 
          placeholder="Ex: 1000-123" 
          required
          style="color: black;" 
        />

        <label for="accountHolder">Titular da Conta</label>
        <input 
          type="text" 
          id="accountHolder" 
          v-model="multibancoAccountHolder" 
          placeholder="Ex: João Silva" 
          required
          style="color: black;" 
        />

        <label for="cardNumber">Número do Cartão</label>
        <input 
          type="text" 
          id="cardNumber" 
          v-model="multibancoCardNumber" 
          placeholder="Ex: 1234 5678 9012 3456" 
          required
          style="color: black;" 
        />
        <button type="submit" class="btn submit-btn">Concluir pagamento</button>
      </form>
    </div>

    <!-- Botões de ação -->
    <div class="action-buttons">
      <button class="btn change-plan-btn" @click="changePlan">Alterar Subscrição</button>
    </div>
  </div>
</template>

<script setup>
import axios from 'axios';
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { getId, getName, getPlan, getUser } from '@/utils/functions';
import { useRoute } from 'vue-router';
const route = useRoute();

const paymentMethod = ref('multibanco');  // Garantir que apenas 'multibanco' seja mostrado
const multibancoAddress = ref('');
const multibancoPostalCode = ref('');
const multibancoAccountHolder = ref('');
const multibancoCardNumber = ref('');
const router = useRouter();

// Função para redirecionar para a página de edição de perfil
const changePlan = () => {
  router.push('/editProfile');
};

// Função para tratar a submissão do pagamento
const handlePaymentSubmit = async () => {
  try {

    console.log('userId:', getId());
    console.log('plan:', getPlan());


    const paymentResponse = await axios.post(`/api/subscriptions/payment/`, {
      userId: getId(),
      extra: JSON.stringify({
        "transaction_id": multibancoCardNumber.value, 
        "receipt_url": ""
      }),
    }, {
      withCredentials: true,
    });

    if (paymentResponse.status === 200) {
      console.log('Pagamento processado com sucesso:', paymentResponse.data);

      const fromEdit = route.query.fromEdit === 'true';

      alert('Obrigada pela sua subscrição!');
      if (fromEdit){
        router.push('/projects'); // Redireciona para a página de edição de perfil
      }else {
        router.push('/login'); // Redireciona para a página de login
      }

    } else {
      alert('Erro ao processar o pagamento, tente novamente.');
    }
  } catch (error) {
    console.error('Erro ao processar o pagamento:', error.response?.data || error.message);
    alert('Erro ao processar o pagamento. Por favor, tente novamente.');
  }
};
</script>

<style scoped>
/* Garantir que o body e html ocupem 100% da altura da janela */
html, body {
  height: 100%;
  margin: 0;
}

.payment-container {
  display: flex;
  flex-direction: column;
  justify-content: center; /* Centraliza o conteúdo verticalmente */
  align-items: center;
  min-height: 100vh; /* Garante que o container ocupe toda a altura da janela */
  padding: 2rem;
  background-color: #c8dbe7;
  color: #333;
  font-family: Arial, sans-serif;
}

h1 {
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 2rem;
}

.form-section {
  width: 100%;
  max-width: 400px;
  margin-bottom: 2rem;
}

.form-section h3 {
  font-size: 1.5rem;
  margin-bottom: 1rem;
}

label {
  font-size: 1rem;
  margin-bottom: 0.5rem;
  display: block;
}

input {
  width: 100%;
  padding: 0.75rem;
  font-size: 1rem;
  margin-bottom: 1rem;
  border-radius: 5px;
  border: 1px solid #ddd;
  background-color: white; 
  color: black; /* Cor do texto nos campos de input */
}

.btn {
  width: 100%;
  padding: 1rem;
  font-size: 1rem;
  border-radius: 5px;
  cursor: pointer;
  border: none;
}

.submit-btn {
  background-color: #1d3557;
  color: white;
}

.submit-btn:hover {
  background-color: #457b9d;
}

.change-plan-btn {
  background-color: #e63946;
  color: white;
}

.change-plan-btn:hover {
  background-color: #f1a5a5;
}

.action-buttons {
  margin-top: 2rem;
}

.action-buttons .btn {
  width: auto;
  margin: 0 0.5rem;
}
</style>
