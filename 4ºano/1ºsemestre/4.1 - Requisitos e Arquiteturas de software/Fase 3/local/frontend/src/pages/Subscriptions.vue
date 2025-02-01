<template>
  <div class="subscription-container">
    <h1>PICTURAS</h1>
    <h2>Subscrições</h2>
    <div class="plans">
      <!-- Plano Gratuito -->
      <div 
        class="plan free" 
        :class="{ selected: selectedPlan === 'free' }" 
        @click="selectPlan('free')"
      >
        <h3 class="free-title">Gratuito</h3>
        <ul>
          <li>5 Operações p/ dia <i class="icon">✔️</i></li>
          <li>Imagens de todos os tamanhos <i class="icon">✔️</i></li>
          <li>3 Downloads p/ dia <i class="icon">✔️</i></li>
          <li>Downloads de baixa resolução com limite de 1GB <i class="icon">✔️</i></li>
          <li>Limitação de ferramentas <i class="icon">✔️</i></li>
        </ul>
        <p class="price">Grátis</p>
      </div>

      <!-- Plano Mensal -->
      <div 
        class="plan monthly" 
        :class="{ selected: selectedPlan === 'monthly' }" 
        @click="selectPlan('monthly')"
      >
        <h3 class="monthly-title">Mensal <span class="badge">⭐</span></h3>
        <ul>
          <li>∞ Operações p/ dia <i class="icon">✔️</i></li>
          <li>Imagens de todos os tamanhos <i class="icon">✔️</i></li>
          <li>∞ Downloads p/ dia <i class="icon">✔️</i></li>
          <li>Downloads HD s/ limite <i class="icon">✔️</i></li>
          <li>Disponíveis todas as ferramentas <i class="icon">✔️</i></li>
        </ul>
        <p class="price">X,xx € p/ mês *</p>
      </div>

      <!-- Plano Anual -->
      <div 
        class="plan yearly" 
        :class="{ selected: selectedPlan === 'annual' }" 
        @click="selectPlan('annual')"
      >
        <h3 class="yearly-title">Annual <span class="badge">⭐</span></h3>
        <ul>
          <li>∞ Operações p/ dia <i class="icon">✔️</i></li>
          <li>Imagens de todos os tamanhos <i class="icon">✔️</i></li>
          <li>∞ Downloads p/ dia <i class="icon">✔️</i></li>
          <li>Downloads HD s/ limite <i class="icon">✔️</i></li>
          <li>Disponíveis todas as ferramentas <i class="icon">✔️</i></li>
        </ul>
        <p class="price">Y,yy € p/ ano *</p>
      </div>
    </div>

    <!-- Botões de ação -->
    <div class="action-buttons">
      <button class="btn save-btn" @click="savePlan">Guardar alteração de plano</button>
      <button class="btn back-btn" @click="goBack">Voltar à edição de perfil</button>
    </div>

    <p class="disclaimer">*Pagamentos mensais e anuais de débito direto.</p>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';

const selectedPlan = ref(null);
const router = useRouter();

// Função para selecionar plano
const selectPlan = (plan) => {
  selectedPlan.value = plan;
};

// Função para salvar a alteração do plano e atualizar o tipo do usuário
const savePlan = async () => {
  if (selectedPlan.value) {
    //if (selectedPlan.value  == 'monthly' || selectedPlan.value == 'yearly' ) {
      //selectedPlan.value = "premium";
    //}

    window.localStorage.setItem('newPlan', selectedPlan.value); // Atualiza o plano no localStorage
    alert("Guarda as alterações na próxima página!");
    router.push(`/editProfile?fromSubscription=true`); 
  } else {
    alert("Por favor, selecione um plano antes de salvar.");
  }
};

// Função para voltar à página anterior
const goBack = () => {
  router.go(-1); 
};
</script>

<style scoped>
.subscription-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  min-height: 100vh;
  padding: 2rem;
  background-color: #c8dbe7;
  color: #1d3557;
  font-family: Arial, sans-serif;
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

.plans {
  display: flex;
  gap: 2rem;
  justify-content: center;
  flex-wrap: wrap;
  flex-grow: 1;
}

.plan {
  background: #ffffff;
  border-radius: 10px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
  text-align: center;
  width: 300px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.3s ease;
}

.plan.selected {
  border: 3px solid #ff9900; /* Cor do contorno ao selecionar o plano */
  box-shadow: 0 0 10px rgba(255, 153, 0, 0.7); /* Efeito de sombra para destacar ainda mais */
}

.plan.free {
  border-color: #457b9d;
}

.plan.monthly {
  border-color: #a020f0;
}

.plan.yearly {
  border-color: #a020f0;
}

h3 {
  font-size: 1.5rem;
  margin-bottom: 1rem;
}

.badge {
  background-color: #a020f0;
  color: #fff;
  padding: 0.2rem 0.5rem;
  border-radius: 5px;
  font-size: 0.9rem;
}

ul {
  list-style: none;
  padding: 0;
  margin: 0;
  margin-bottom: 1.5rem;
}

li {
  font-size: 1rem;
  margin-bottom: 0.5rem;
}

.icon {
  margin-left: 0.5rem;
  color: #1d3557;
}

.price {
  font-size: 1.2rem;
  font-weight: bold;
  margin-bottom: 1rem;
  color: #457b9d;
}

.free-title {
  color: #457b9d;
}

.monthly-title {
  color: #a020f0;
}

.yearly-title {
  color: #a020f0;
}

.disclaimer {
  font-size: 0.9rem;
  color: #6c757d;
  margin-top: 2rem;
  text-align: center;
}

.action-buttons {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
}

.btn {
  padding: 0.75rem 2rem;
  font-size: 1rem;
  border-radius: 5px;
  cursor: pointer;
}

.save-btn {
  background-color: #1d3557;
  color: white;
  border: none;
}

.save-btn:hover {
  background-color: #457b9d;
}

.back-btn {
  background-color: #e63946;
  color: white;
  border: none;
}

.back-btn:hover {
  background-color: #f1a5a5;
}
</style>
