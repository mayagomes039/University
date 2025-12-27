<template>
  <div class="details-page">
    <TopToolBarMenu />

    <SuccessPopup v-if="successMessage" :text="successMessage" /> <!-- mostra se successMessage não estiver vazio-->
    <ErrorPopup v-if="errorMessage" :text="errorMessage" /> <!-- mostra se errorMessage não estiver vazio-->


    <!-- Conteúdo principal -->
    <div class="content-container">
      <!-- Detalhes do ponto (lado esquerdo) -->
      <div class="point-details">
        <div class="point-details-header">
        <h2>{{ pointDetails.name || 'Carregando...' }}</h2>
        <button class="action-butn" @click="openInGoogleMaps">Ver no Google Maps</button>
      </div>


        <div class="details-section">
          <h3>Informações</h3>
          <p><strong>Data de Criação:</strong> {{ formatDateSemHoras(pointDetails.creationDate) }}</p>
          <p><strong>Descrição:</strong> {{ pointDetails.description || 'Sem descrição.' }}</p>

          <!-- Imagens do ponto -->
          <div v-if="pointDetails.images && pointDetails.images.length">
            <h4>Imagens</h4>
            <div class="point-images">
              <img
                v-for="img in pointDetails.images"
                :key="img.id"
                :src="img.url"
                alt="Imagem do ponto"
                class="point-image"
                @click="openImage(img.url)"
              />
            </div>
          </div>

          <!-- Modal da imagem -->
          <div v-if="enlargedImage" class="modal" @click="enlargedImage = null">
            <img :src="enlargedImage" alt="Imagem ampliada" class="modal-image" />
          </div>

          <!-- Outras informações -->
          <p><strong>Premium:</strong> {{ pointDetails.premium ? 'Sim' : 'Não' }}</p>
          <p><strong>Latitude:</strong> {{ pointDetails.latitude }}</p>
          <p><strong>Longitude:</strong> {{ pointDetails.longitude }}</p>
          <p><strong>Avaliação:</strong> <v-rating
              :model-value="pointDetails.score"
              readonly
              color="amber"
              size="25"
              half-increments
          /> ({{pointDetails.score ? pointDetails.score.toFixed(2) : ''}}) </p>
          <p><strong>Dificuldade:</strong> {{ pointDetails.difficulty }}</p>
        </div>

        <div class="buttons">
          <!-- <button class="action-btn" @click="markAsVisited" :disabled="loadingVisit">
            {{ loadingVisit ? 'A processar...' : 'Marcar como visitado' }}
          </button> -->
          <button class="action-btn" @click="openCommentModal">Adicionar comentário</button>
        </div>
      </div> <!-- fecha .point-details -->


      <!-- Reviews (lado direito) -->
      <div class="reviews-section">
        <h3 class="mb-4">Reviews</h3>

        <v-container fluid class="pa-0" v-if="reviews.length > 0">
          <v-row dense>
            <v-col
                v-for="review in sortedReviews"
                :key="review.creationDate"
                cols="12"
                class="mb-4"
            >
              <!-- 🟦 v-card por review -->
              <v-card class="pa-4" elevation="2" rounded>
                <v-row align="start" justify="space-between" class="mb-1">
                  <!-- Username + Data -->
                  <v-col cols="auto">
                    <router-link
                        :to="`/profile/${review.userId}`"
                        class="text-decoration-none font-weight-bold"
                        style="color: #427F99; font-size: 1.3rem;"
                    >
                      {{ review.username }}
                    </router-link>
                    <div style="font-size: 1rem; color: #6c757d">
                      {{ formatDate(review.creationDate) }}
                    </div>
                  </v-col>

                  <!-- Rating + Edit/Delete Icons -->
                  <v-col cols="auto" justify="space-between" class="text-right">
                    <v-rating
                        :model-value="review.rating"
                        readonly
                        color="amber"
                        size="25"
                        half-increments
                    />

                    <!-- Icons below rating -->
                    <v-row dense no-gutters justify="end">
                      <v-col cols="auto" class="mr-2">
                        <v-tooltip
                            v-if="parseInt(review.userId) === parseInt(userStore.id)"
                            text="Editar Review"
                            location="top"
                        >
                          <template v-slot:activator="{ props }">
                            <v-icon
                                icon="fa-solid fa-pen"
                                size="x-small"
                                color="#1976D2"
                                v-bind="props"
                                @click="editReview(review)"
                            />
                          </template>
                        </v-tooltip>
                      </v-col>
                      <v-col cols="auto">
                        <v-tooltip
                            v-if="parseInt(review.userId) === parseInt(userStore.id)"
                            text="Apagar Review"
                            location="top"
                        >
                          <template v-slot:activator="{ props }">
                            <v-icon
                                icon="fa-solid fa-trash"
                                size="x-small"
                                @click="deleteReview(review.id)"
                                v-bind="props"
                                color="#E53935"
                            />
                          </template>
                        </v-tooltip>
                      </v-col>
                    </v-row>
                  </v-col>
                </v-row>

                <v-divider/>
                <div class="mb-1">
                  <div class="font-weight-bold" style="color: #427F99">Comentário:</div>
                  <div>{{ review.comment }}</div>

                  <div v-if="review.images && review.images.length">
                    <p class="font-weight-bold" style="color: #427F99">Fotografias:</p>
                    <v-row dense>
                      <v-col
                          v-for="(img, index) in review.images"
                          :key="index"
                          cols="4"
                          sm="3"
                          md="2"
                      >
                        <v-img
                            :src="typeof img === 'string' ? img : img.url"
                            alt="Imagem da review"
                            aspect-ratio="1"
                            class="rounded"
                            @click="openImage(img.url)"
                            cover
                        ></v-img>
                      </v-col>
                    </v-row>
                  </div>
                </div>
              </v-card>
              
            </v-col>
          </v-row>
        </v-container>

        <p v-else>Sem reviews disponíveis.</p>
      </div>
    </div> <!-- fecha .content-container -->

    <!-- Modal para adicionar comentário -->
    <div v-if="showCommentModal" class="modal-overlay" @click.self="closeCommentModal">
      <div class="modal-content">
        <h3>Adicionar Comentário</h3>
        <textarea v-model="newComment" placeholder="Escreve o teu comentário aqui..."></textarea>
        <div
            style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 1rem;"
        >
          <label style="margin-right: 0.5rem; font-weight: bold;">Avaliação:</label>
          <v-rating
              v-model="newRating"
              length="5"
              color="amber"
              background-color="grey lighten-1"
              large
              hover
          />
          <span style="margin-left: 0.5rem; font-weight: bold;">{{ newRating ? newRating : 0 }}/5</span>
        </div>


        <label class="file-upload-label">
          Adicionar imagem
        <input type="file" accept="image/*" multiple @change="handleReviewImageUpload" />
        </label>

        <div v-if="reviewImagePreview.length" class="preview">
  <img
    v-for="(src, idx) in reviewImagePreview"
    :key="idx"
    :src="src"
    alt="Preview da imagem"
    class="preview-image"
  />
</div>

        <div class="modal-buttons">
          <button @click="submitComment" :disabled="loadingComment">
            {{ loadingComment ? 'A enviar...' : 'Enviar' }}
          </button>
          <button @click="closeCommentModal">Cancelar</button>
        </div>
      </div>
    </div>

    <!-- Popup de erro -->
    <ErrorPopup v-if="showError" :text="errorText" />
    <SuccessPopup v-if="showSuccess" :text="successText" />

  </div>
</template>


<script setup>
import { ref, onMounted, computed } from 'vue'
import { calculateDistance } from '@/utils/distance.js'
import { useRouter, useRoute } from 'vue-router'
import LogoButton from '@/components/LogoButton.vue'
import { S3Client } from '@aws-sdk/client-s3'
import { Upload } from '@aws-sdk/lib-storage'
import axios from "axios";
import {UserStore} from "@/store/userStore.js";
import TopToolBarMenu from "../components/TopToolBarMenu.vue";
import ErrorPopup from "../components/ErrorPopup.vue"
import SuccessPopup from "../components/SuccessPopup.vue"

const editingReviewId = ref(null)
const reviewImageFiles = ref([])
const reviewImagePreview = ref('')
const reviewImage = ref('/default-review.png')
const router = useRouter()
const route = useRoute()
const pi = ref(null)
const userStore = UserStore()

const showError = ref(false)
const errorText = ref('')
const showSuccess = ref(false)
const successText = ref('')
const API_BASE_URL = import.meta.env.VITE_API_URL;
const Minio_URL = import.meta.env.VITE_MINIO_URL;

const token = userStore.authToken;

const pointDetails = ref({})
const reviews = ref([])

const loadingVisit = ref(false)
const loadingComment = ref(false)

const showCommentModal = ref(false)
const newComment = ref('')
const newRating = ref('')

const successMessage = ref('')
const errorMessage = ref('')

const rating = ref(0)

function displayError(msg) {
  errorText.value = msg
  showError.value = true
  setTimeout(() => {
    showError.value = false
  }, 3000) // popup desaparece após 3 segundos
}

function displaySuccess(msg) {
  successText.value = msg
  showSuccess.value = true
  setTimeout(() => {
    showSuccess.value = false
  }, 3000) // desaparece após 3 segundos
}


const sortedReviews = computed(() => {
  return reviews.value.slice().sort((a, b) => new Date(b.creationDate) - new Date(a.creationDate))
})


// Pega o id do ponto da rota
const pointId = route.params.id

// Fetch detalhes do ponto
const fetchPointDetails = async () => {
  try {
    const res = await fetch(`${API_BASE_URL}/pi/details/${pointId}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    
    if (res.status === 204) {
      // Premium content not allowed, redirect
      displayError('Este ponto é premium. Redirecionando...');
      setTimeout(() => {
        router.push('/subscribe/premium');
      }, 2000); // Optional short delay for user feedback
      return;
    }

    if (!res.ok) {
      console.warn('Erro ao carregar detalhes do ponto')
      return
    }
    
    
    const data = await res.json()
    const { lat, lng } = userStore.location
    
    const enrichedPonto = {
      ...data,
      distanceFromUser: lat && lng
        ? calculateDistance(lat, lng, data.latitude, data.longitude)
        : null
    }

    // Atribuir tanto os detalhes do ponto como as reviews
    pointDetails.value = enrichedPonto
    reviews.value = data.reviews || [] // Extrair as reviews dos detalhes
    
    console.log('Detalhes do ponto recebidos:', JSON.stringify(enrichedPonto, null, 2))
    console.log('Reviews extraídas:', JSON.stringify(data.reviews, null, 2))
    
    reviewImage.value = data.reviewImage || '/default-review.png'
  } catch (err) {
    console.error('Erro fetch detalhes:', err)
  }
}

const enlargedImage = ref(null)

function openImage(url) {
  enlargedImage.value = url
}





const editReview = (review) => {
  newComment.value = review.comment
  newRating.value = review.rating
  editingReviewId.value = review.id
  showCommentModal.value = true
}


const deleteReview = async (reviewId) => {
  if (!confirm("Tens a certeza que queres apagar esta review?")) return

  try {
    const res = await axios.delete(`${API_BASE_URL}/reviews/${reviewId}`, {
      headers: { Authorization: `Bearer ${token}` }
    })

    if (res.status === 200) {
      displaySuccess('Review apagada com sucesso!')
      await fetchPointDetails()
    } else {
      displayError("Ocorreu um erro ao apagar review. Tente novamente mais tarde!")
    }
  } catch (err) {
    console.error("Erro ao apagar review", err)
    displayError("Ocorreu um erro. Tente novamente mais tarde!")
  }
}







//upload de imagens
const handleReviewImageUpload = (event) => {
  const files = Array.from(event.target.files)
  reviewImageFiles.value = files
  reviewImagePreview.value = files.map(file => URL.createObjectURL(file))
}

const s3Client = new S3Client({
  region: 'us-east-1',
  endpoint: `${Minio_URL}`,
  credentials: {
    accessKeyId: 'admin',
    secretAccessKey: 'admin123',
  },
  forcePathStyle: true,
})

const uploadReviewImageToMinIO = async (file) => {
  const fileName = `review-${Date.now()}-${file.name}`
  const upload = new Upload({
    client: s3Client,
    params: {
      Bucket: 'review-images',
      Key: fileName,
      Body: file,
      ContentType: file.type,
      ACL: 'public-read',
    },
  })
  await upload.done()
  return `${Minio_URL}/review-images/${fileName}`
}


// Modal comentário
const openCommentModal = () => {
  newComment.value = ''
  newRating.value = ''
  reviewImageFiles.value = []
  reviewImagePreview.value = []
  editingReviewId.value = null
  showCommentModal.value = true
}

const closeCommentModal = () => {
  showCommentModal.value = false
  editingReviewId.value = null
  reviewImageFiles.value = []
  reviewImagePreview.value = []
}


// Submeter comentário
async function submitComment() {
  if (!pointDetails.value || !pointId) {
    console.error("Erro: PI ainda não carregado.");
    return;
  }
if (!newComment.value.trim()) {
    displayError('O campo comentário é obrigatório.')
    return
  }
  if (!newRating.value) {
    displayError('Por favor, selecione uma avaliação.')
    return
  }
  
  if (!pointDetails.value || !pointId) {
    console.error("Erro: PI ainda não carregado.");
    return;
  }
  loadingComment.value = true

  try {
    let imageUrls = []

    for (const file of reviewImageFiles.value) {
      const imageUrl = await uploadReviewImageToMinIO(file)
      imageUrls.push(imageUrl)
    }

    const payload = {
      rating: newRating.value,
      comment: newComment.value,
      images: imageUrls
    }

    let response
    if (editingReviewId.value) {
      // EDIT mode
      response = await axios.put(
        `${API_BASE_URL}/reviews/${editingReviewId.value}`,
        payload,
        { headers: { Authorization: `Bearer ${token}` } }
      )
    } else {
      // CREATE mode
      response = await axios.post(
        `${API_BASE_URL}/pi/${pointId}/reviews`,
        payload,
        { headers: { Authorization: `Bearer ${token}` } }
      )
    }

    console.log("Comentário enviado/editado com sucesso", response.data)

    await fetchPointDetails()
    displaySuccess(editingReviewId.value ? 'Review atualizada com sucesso!' : 'Review adicionada com sucesso!')
    closeCommentModal()
    reviewImageFiles.value = null
    reviewImagePreview.value = ''
    editingReviewId.value = null // reset after submit
  } catch (error) {
    console.error("Erro ao enviar comentário", error)
    displayError("Ocorreu um problema. Tente novamente mais tarde!")
  } finally {
    loadingComment.value = false
    reviewImageFiles.value = []
    reviewImagePreview.value = []
  }
}


const formatDate = (dateString) => {
  if (!dateString) return 'Data não disponível'

  let d = new Date(dateString)

  if (isNaN(d)) {
    d = new Date(dateString + 'T00:00:00')
  }

  if (isNaN(d)) return 'Data inválida'

  return d.toLocaleString('pt-PT', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const formatDateSemHoras = (dateString) => {
  if (!dateString) return 'Data não disponível'

  // Tenta criar a data diretamente
  let d = new Date(dateString)

  // Se inválido, tenta acrescentar T00:00:00
  if (isNaN(d)) {
    d = new Date(dateString + 'T00:00:00')
  }

  if (isNaN(d)) return 'Data inválida'

  return d.toLocaleDateString('pt-PT', { day: '2-digit', month: 'short', year: 'numeric' })
}

const openInGoogleMaps = () => {
  const lat = pointDetails.value.latitude
  const lng = pointDetails.value.longitude

  if (!lat || !lng) {
    displayError('Coordenadas indisponíveis.')
    return
  }

  const url = `https://www.google.com/maps?q=${lat},${lng}`
  window.open(url, '_blank')
}


onMounted(() => {
  fetchPointDetails()
})
</script>

<style scoped>
.point-images {
  display: flex;
  gap: 10px;
  margin-top: 0.5rem;
  flex-wrap: wrap;
}

.point-image {
  width: 120px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  border: 2px solid #427F99;
}

.details-page {
  background-color: white;
  min-height: 100vh;
  font-family: sans-serif;
  color: black;
}

.navbar {
  display: flex;
  justify-content: space-between;
  background-color: #427F99;
  padding: 0.75rem 1.5rem;
  color: white;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.nav-button {
  background-color: white;
  color: #427F99;
  border-radius: 8px;
  padding: 0.5rem 1rem;
  font-weight: bold;
  cursor: pointer;
  border: none;
}

.nav-button:hover {
  background-color: #e0e0e0;
}

.content-container {
  display: flex;
  max-width: 1200px;
  margin: 2rem auto;
  gap: 2rem;
  padding: 0 1rem;
  color: black;
}
.review-actions {
  margin-top: 0.5rem;
  display: flex;
  gap: 0.5rem;
  justify-content: flex-end;
}

.review-actions button {
  padding: 0.4rem 0.8rem;
  font-size: 0.9rem;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  font-weight: bold;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.review-actions .edit-btn {
  background-color: #ffd966; /* Light yellow */
  color: #333;
}

.review-actions .edit-btn:hover {
  background-color: #ffcf33;
}

.review-actions .delete-btn {
  background-color: #e06666; /* Soft red */
  color: white;
}

.review-actions .delete-btn:hover {
  background-color: #d43f3f;
}

.point-details-header {
  display: flex;
  align-items: center;
  justify-content: space-between; /* ou flex-start se quiseres mais junto */
  gap: 1rem;
  flex-wrap: wrap; /* para mobile não quebrar */
}

/* Lado esquerdo - detalhes */
.point-details {
  flex: 1;
  background-color: #e6f0f5;
  border-radius: 12px;
  padding: 1rem 1.5rem;
  max-height: 600px;
  overflow-y: auto;
}

.point-details h2 {
  color: #427F99;
  margin-bottom: 1rem;
}

.details-section p {
  margin: 0.5rem 0;
}

/* Botões */
.buttons {
  margin-top: 2rem;
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.action-btn {
  background-color: #427F99;
  color: white;
  padding: 0.75rem 1.2rem;
  border-radius: 8px;
  border: none;
  font-weight: bold;
  cursor: pointer;
  flex-grow: 1;
}


.action-btn:disabled {
  background-color: #7ea5b7;
  cursor: not-allowed;
}

.action-butn {
  background-color: #427F99;
  color: white;
  padding: 0.75rem 1.2rem;
  border-radius: 8px;
  border: none;
  font-weight: bold;
  cursor: pointer;
}

/* Lado direito - reviews */
.reviews-section {
  flex: 1;
  background-color: #e6f0f5;
  border-radius: 12px;
  padding: 1rem 1.5rem;
  max-height: 600px;
  overflow-y: auto;
}

.reviews-section h3 {
  color: #427F99;
  margin-bottom: 1rem;
}

.review-item {
  margin-bottom: 1rem;
  text-align: left;
  color: #333;
}

.review-item hr {
  border: none;
  border-bottom: 1px solid #ccc;
  margin-top: 0.5rem;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0,0,0,0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 100;
}

.modal-content {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.3);
}

.modal-content textarea {
  width: 100%;
  height: 100px;
  resize: vertical;
  margin-bottom: 1rem;
  padding: 0.5rem;
  font-size: 1rem;
  border: 1px solid black;
  border-radius: 6px;
}

.modal-content select {
  width: 100%;
  padding: 0.5rem;
  margin-bottom: 1rem;
  font-size: 1rem;
  border: 1px solid black;
  border-radius: 6px;
}

.modal-content .preview {
  display: flex;
  justify-content: center;
  margin: 1rem 0;
}
.preview-image {
  max-width: 120px;
  max-height: 120px;
  margin: 0.5rem;
  border-radius: 8px;
  border: 2px solid #427F99;
  object-fit: cover;
}


.modal-content .preview img {
  max-width: 120px;
  max-height: 120px;
  width: auto;
  height: auto;
  border-radius: 8px;
  border: 2px solid #427F99;
  object-fit: cover;
}

/* File upload label styling for modal */
.modal-content .file-upload-label {
  display: block;
  margin: 1rem 0;
  background-color: #f8f9fa;
  color: #427F99;
  padding: 0.75rem;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
  text-align: center;
  border: 2px solid #427F99;
  transition: all 0.2s ease;
  width: 100%;
  box-sizing: border-box;
}

.modal-content .file-upload-label:hover {
  background-color: #e9ecef;
}

.modal-content .file-upload-label input {
  display: none;
}
.modal-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

.modal-buttons button {
  padding: 0.5rem 1rem;
  font-weight: bold;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.modal-buttons button:first-child {
  background-color: #427F99;
  color: white;
}

.modal-buttons button:last-child {
  background-color: #ccc;
  color: black;
}

.modal-buttons button:disabled {
  background-color: #7ea5b7;
  cursor: not-allowed;
}

.review-picture-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.review-picture {
  width: 100px;
  height: 100px;
  border-radius: 0%;
  object-fit: cover;
  border: 4px solid white;
  margin-bottom: 1rem;
}


.point-image:hover {
  transform: scale(1.05);
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.modal-image {
  max-width: 90%;
  max-height: 90%;
  border-radius: 8px;
}



/* Responsive */
@media (max-width: 900px) {
  .content-container {
    flex-direction: column;
  }
  .point-details, .reviews-section {
    max-height: none;
    width: 100%;
  }
}
</style>
