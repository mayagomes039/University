<template>

  <TopToolBarMenu/>
  <div class="home-page">


    <div class="review-container">
      <h1>Revisão de Pontos de Interesse</h1>

      <ErrorPopup v-if="errorMessage" :text="errorMessage" />
      <SuccessPopup v-if="successMessage" :text="successMessage" />

      <div v-if="loading" class="loading-message">A carregar pontos de interesse...</div>

      <div v-else-if="pontos.length === 0" class="no-results">Nenhum ponto de interesse para rever.</div>

      <div v-else class="points-list">
        <div class="poi-card" v-for="poi in pontos" :key="poi.id">
          <h2>{{ poi.name }}</h2>
          <p><strong>Descrição:</strong> {{ poi.description }}</p>
          <p><strong>Dificuldade:</strong> {{ poi.difficulty }}</p>
          <p><strong>Acessibilidade:</strong> {{ poi.accessibility ? 'Sim' : 'Não' }}</p>
          <p><strong>Premium:</strong> {{ poi.premium ? 'Sim' : 'Não' }}</p>
          <p><strong>Score:</strong> {{ poi.score?.toFixed(1) ?? 'N/A' }}</p>
          <p><strong>Data de Criação:</strong> {{ formatDate(poi.creationDate) }}</p>
          <p><strong>Criado por:</strong> {{ poi.creatorEmail }}</p>
          <div class="action-buttons">
            <button class="accept-button" @click="handleDecision(poi.id, true)">Aceitar</button>
            <button class="refuse-button" @click="askForComment(poi.id)">Recusar</button>
          </div>
          <div v-if="poi.images?.length" class="poi-images">
  <p><strong>Imagens:</strong></p>
  <div class="images-container">
    <img v-for="img in poi.images" :key="img.id" :src="img.url" alt="Imagem do ponto" />
  </div>
</div>


        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import TopToolBarMenu from "../components/TopToolBarMenu.vue";
import { ref, onMounted } from 'vue'
import LogoButton from '@/components/LogoButton.vue'
import ErrorPopup from '@/components/ErrorPopup.vue'
import SuccessPopup from '@/components/SuccessPopup.vue'
import { useRouter } from 'vue-router'
import {UserStore} from "@/store/userStore.js";
const API_BASE_URL = import.meta.env.VITE_API_URL;


const pontos = ref([])
const loading = ref(true)
const errorMessage = ref('')
const successMessage = ref('')
const userStore = UserStore()

const formatDate = (dateString) => {
  if (!dateString) return 'Desconhecida'
  return new Date(dateString).toLocaleDateString('pt-PT', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}

const fetchPontos = async () => {
  loading.value = true
  try {
    const token = userStore.authToken
    const response = await fetch(`${API_BASE_URL}/admin/pi`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    })

    if (response.status === 401) {
      showError('Não autorizado. Verifique as permissões.')
      return
    }

    if (!response.ok) {
      const text = await response.text()
      showError(text || 'Erro ao obter dados.')
      return
    }

    pontos.value = await response.json()
    successMessage.value = 'Pontos de interesse carregados com sucesso!'
    setTimeout(() => successMessage.value = '', 3000)
  } catch (err) {
    errorMessage.value = 'Erro de rede. Tente novamente.'
    console.error(err)
  } finally {
    loading.value = false
  }
}

const handleDecision = async (id, accepted, comment = '') => {
  try {
    const token = userStore.authToken
    const response = await fetch(`${API_BASE_URL}/admin/pi/${id}`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(accepted ? { accepted: true } : { accepted: false, comment }),
    })

    if (!response.ok) {
      const text = await response.text()
      showError(text || 'Erro ao submeter decisão.')

      return
    }

    successMessage.value = accepted
      ? 'Ponto aceite com sucesso.'
      : 'Ponto recusado com sucesso.'

    pontos.value = pontos.value.filter(p => p.id !== id) // Remove from list
    setTimeout(() => successMessage.value = '', 3000)
  } catch (err) {
    showError('Erro ao enviar decisão.')
    console.error(err)
  }
}

const askForComment = async (id) => {
  const comment = prompt('Escreva o motivo da recusa:')
  if (comment && comment.trim().length > 0) {
    handleDecision(id, false, comment.trim())
  } else {
    showError('Motivo de recusa obrigatório.')
  }
}

// Ativa o popup de erro no canto esquerdo
function showError(msg) {
  errorMessage.value = msg
  successMessage.value = '' // Limpar mensagem de sucesso
  setTimeout(() => {
    errorMessage.value = ''
  }, 3000)
}

// Ativa o popup de sucesso no canto esquerdo
function showSuccess(msg) {
  successMessage.value = msg
  errorMessage.value = '' // Limpar mensagem de erro
  setTimeout(() => {
    successMessage.value = ''
  }, 3000)
}


onMounted(fetchPontos)
</script>

<style scoped>

.poi-images {
  margin-top: 1rem;
}

.images-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.images-container img {
  width: 150px;
  height: auto;
  border-radius: 8px;
  object-fit: cover;
  box-shadow: 0 1px 5px rgba(0, 0, 0, 0.2);
}

.action-buttons {
  margin-top: 1rem;
  display: flex;
  gap: 1rem;
}

.accept-button,
.refuse-button {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.accept-button {
  background-color: #4CAF50;
  color: white;
}

.accept-button:hover {
  background-color: #45a049;
}

.refuse-button {
  background-color: #f44336;
  color: white;
}

.refuse-button:hover {
  background-color: #d32f2f;
}


.review-container {
  padding: 2rem;
  max-width: 1000px;
  margin: auto;
  color: black;
  background-color: white;
}

h1 {
  text-align: center;
  margin-bottom: 2rem;
}

.loading-message,
.no-results {
  text-align: center;
  margin-top: 2rem;
  font-size: 1.2rem;
  color: #333;
}
.navbar {
  display: flex;
  justify-content: space-between;
  background-color: #427F99;
  padding: 0.75rem 1.5rem;
  color: white;
}

.nav-button {
  background-color: white;
  color: #6e98db;
  border: none; /* ✅ Remove border */
  border-radius: 8px;
  padding: 0.5rem 1rem;
  margin-left: 0.5rem;
  text-decoration: none;
  font-weight: bold;
  transition: background-color 0.2s ease;
  cursor: pointer;
}


.nav-button:hover {
  background-color: #e0e0e0;
}

.points-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.poi-card {
  background-color: #f5f5f5;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.poi-card h2 {
  margin-bottom: 0.5rem;
  color: #427F99;
}

.poi-card p {
  margin: 0.2rem 0;
}

/* 📱 Responsive */
@media (max-width: 768px) {
  .review-container {
    padding: 1rem;
  }

  .poi-card {
    padding: 1rem;
  }
}
</style>
