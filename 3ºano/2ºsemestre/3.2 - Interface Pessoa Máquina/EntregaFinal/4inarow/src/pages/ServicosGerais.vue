<template>
    <div>
      <NavBar title="Serviços Universais" />
      <div class="box">
        <h2>Serviços Atribuídos</h2>
        <router-link to="/servicos-atribuidos" class="custom-button">Ver Serviços Atribuídos</router-link>
      </div>
      <div class="box">
        <h2>Serviços Concluidos</h2>
        <router-link to="/servicos-concluidos" class="custom-button">Ver Serviços Concluidos</router-link>
      </div>
    </div>
  </template>
  
  <script>
  import NavBar from '../components/NavBar.vue';
  export default {
    name: 'ServicosGerais',

    components: {
    NavBar,
    },

    mounted() {
    // Atualiza as horas a cada segundo
    setInterval(this.atualizarHoras, 1000);
  },
  methods: {
    atualizarHoras() {
      const agora = new Date();
      const horas = agora.getHours().toString().padStart(2, '0');
      const minutos = agora.getMinutes().toString().padStart(2, '0');
      const segundos = agora.getSeconds().toString().padStart(2, '0');
      const horaAtual = `${horas}:${minutos}:${segundos}`;

      // Atualiza o elemento HTML com as horas atuais
      document.getElementById('hora-atual').textContent = horaAtual;
    }
  }
  }
  </script>
  
  <style scoped>
  #nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background-color: rgb(124, 150, 167);
}
  .box {
    border: 1px solid #ccc;
    border-radius: 5px;
    padding: 20px;
    margin-top: 20px;
    background-color: rgb(124, 150, 167);
  }
  a {
      text-decoration: none;
      padding: 0.5rem 1.5rem;
      font-weight: 600;
      margin-left: 10px;
      border: 1px solid var(--color-accent);
      border-radius: 5px;
      color: var(--color-accent);
      transition: all 0.3s ease-in;
  }
  a.router-link-active {
      background-color: var(--color-accent);
      color: var(--color-light);
  }
  .custom-button {
  background-color: #AE925B;
  }
  .logo-container {
  display: flex;
  align-items: center;
}

.hora-atual {
  margin-left: 20px; 
}
  </style>
  