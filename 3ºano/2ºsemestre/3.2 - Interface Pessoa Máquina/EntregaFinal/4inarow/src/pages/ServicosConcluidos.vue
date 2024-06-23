<template>
  <div>
    <nav id="nav">
      <div class="logo-container">
        <img src="../../public/image.png" alt="Logo" width="80" height="60">
        <div id="hora-atual" class="hora-atual"></div>
      </div>
      <h1>Serviços Concluídos</h1>
      <div class="nav-items">
        <div class="nav-items">
          <router-link to="/logOut">LogOut</router-link>
          <router-link to="/servicos-gerais">Voltar atrás</router-link>
        </div>
      </div>
    </nav>
    <div class="box">
      <Tabela :services="filteredServices" :services-definitions="servicesDefinitions" />
      <!-- Conteúdo dos serviços atribuídos -->
    </div>
  </div>
</template>

<script>
import Tabela from '../components/ui/Tabela.vue';

export default {
  components: {
    Tabela,
  },

  data() {
    return {
      services: [],
      servicesDefinitions: [],
    };
  },

  computed: {
    filteredServices() {
      return this.services.filter(service => {
        const servicedefinition = this.servicesDefinitions.find(s => s.id === service['service-definitionId']);
        return servicedefinition && (servicedefinition['tipo'] === "universal") && service.estado === "realizado";
      });
    }
  },

  methods: {
    getServices() {
      fetch('http://localhost:3000/services/')
        .then(response => {
          if (response.ok) {
            return response.json();
          } else {
            throw new Error('Something went wrong');
          }
        })
        .then(data => {
          this.services = data;
        })
        .catch(error => console.error('Erro na requisição:', error));
    },
    getServicesDefinitions() {
      fetch('http://localhost:3000/service-definitions/')
        .then(response => {
          if (response.ok) {
            return response.json();
          } else {
            throw new Error('Something went wrong');
          }
        })
        .then(data => {
          this.servicesDefinitions = data;
        })
        .catch(error => console.error('Erro na requisição:', error));
    }
  },
  created() {
    this.getServices();
    this.getServicesDefinitions();
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

.logo-container {
  display: flex;
  align-items: center;
}

.hora-atual {
  margin-left: 20px;
}
</style>
