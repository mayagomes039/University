<template>
    <div class="subscription-container">
      <h1>PICTURAS</h1>
      <h2>Subscrições</h2>
      <div class="plans">
        <!-- Plano Gratuito -->
        <div class="plan free">
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
        <div class="plan monthly">
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
        <div class="plan yearly">
          <h3 class="yearly-title">Anual <span class="badge">⭐</span></h3>
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
  
      <button @click="goBackToRegister" class="back-button">Voltar ao Registo</button>
  
      <p class="disclaimer">*Pagamentos mensais e anuais de débito direto.</p>
    </div>
  </template>
  
  <script>
  export default {
    name: 'Subscriptions',
    methods: {
      goBackToRegister() {
        this.$router.push('/register'); // Voltar à página de registo
      }
    }
  };
  </script>
  
  <style scoped>
  /* Estilos do componente */
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
  }
  
  .plan.free {
    border-color: #457b9d;
  }
  
  .plan.monthly,
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
  
  .back-button {
    background-color: #457b9d;
    color: white;
    padding: 1rem;
    font-size: 1rem;
    border: none;
    border-radius: 5px;
    cursor: pointer;
    margin-top: 2rem;
    width: 100%;
    text-align: center;
  }
  
  .back-button:hover {
    background-color: #1d3557;
  }
  </style>
  