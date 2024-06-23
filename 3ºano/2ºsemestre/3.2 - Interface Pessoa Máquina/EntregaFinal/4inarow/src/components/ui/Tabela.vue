<template>
  <div class="tabela">
    <div class="sort-options">
      <label for="sort">Ordenar por:</label>
      <select id="sort" @change="changeSortOption">
        <option value="duration">Duração</option>
        <option value="date">Data</option>
      </select>
    </div>
    <table>
      <thead>
        <tr>
          <th>ID</th>
          <th>Veículo</th>
          <th>Estado</th>
          <th>Agendamento</th>
          <th>Data</th>
          <th>Duração prevista (minutos)</th>

        </tr>
      </thead>
      <tbody>
        <tr v-for="service in sortedServices" :key="service.id">
          <td>{{ service.id }}</td>
          <td>{{ service.vehicleId }}</td>
          <td>{{ service.estado }}</td>
          <td>{{ service.agendamento }}</td>
          <td v-if="service.data">{{ formatDate(service.data) }}</td>
          <td v-else> sem data</td>
          <td>{{ getServiceDescription(service['service-definitionId']) }}</td>
          <td v-if="service.estado == 'realizado'"><router-link :to="'/detalhes-servicoConcluido/' + service.id"
              style="color: white;">Visualizar Serviço</router-link></td>
          <td v-else><router-link :to="'/detalhes-servico/' + service.id" style="color: white;">Visualizar
              Serviço</router-link></td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
import { defineComponent } from 'vue';

export default defineComponent({
  props: {
    services: {
      type: Array,
      required: true,
    },
    servicesDefinitions: {
      type: Array,
      required: true,
    },
  },
  data() {
    return {
      sortBy: 'none',
    };
  },
  computed: {
    getServiceDescription() {
      return (serviceDefinitionId) => {
        const serviceDefinition = this.servicesDefinitions.find(
          (serviceDefinition) => serviceDefinition.id === serviceDefinitionId
        );
        return serviceDefinition ? serviceDefinition['duraÃ§Ã£o'] : 'Sem duração prevista';
      };
    },
    sortedServices() {
      const servicesWithData = this.services.filter(service => service.data);
      const servicesWithoutData = this.services.filter(service => !service.data);
      if (this.sortBy === 'duration') {
        return this.services.slice().sort((a, b) => {
          const durationA = this.getServiceDuration(a['service-definitionId']);
          const durationB = this.getServiceDuration(b['service-definitionId']);
          return durationA - durationB;
        });
      } else if (this.sortBy === 'date') {
        const sortedByDate = servicesWithData.slice().sort((a, b) => {
          const dateA = new Date(a.data);
          const dateB = new Date(b.data);
          return dateA - dateB;
        });
        return sortedByDate.concat(servicesWithoutData);
      } else {
        return this.services;
      }
    },
  },
  methods: {
    toggleSortByDuration() {
      this.sortByDuration = !this.sortByDuration;
    },
    getServiceDuration(serviceDefinitionId) {
      const serviceDefinition = this.servicesDefinitions.find(
        (serviceDefinition) => serviceDefinition.id === serviceDefinitionId
      );
      return serviceDefinition ? serviceDefinition['duraÃ§Ã£o'] : Infinity;
    },

    formatDate(date) {
      const { dia, mes, ano, hora, minutos } = date;
      return `${dia}/${mes}/${ano} ${hora}:${minutos}`;
    },
    changeSortOption(event) {
      this.sortBy = event.target.value;
    },
  },
});
</script>

<style scoped>
.tabela {
  margin-top: 20px;
}

table {
  width: 100%;
  border-collapse: collapse;
  background-color: #DAC08F;
}

th,
td {
  border: 1px solid #ccc;
  padding: 8px;
}

th {
  background-color: #AE925B;
}
</style>
