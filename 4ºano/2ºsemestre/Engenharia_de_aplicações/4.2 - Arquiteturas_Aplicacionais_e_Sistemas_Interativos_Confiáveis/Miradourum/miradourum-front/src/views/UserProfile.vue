<template>
  <div class="history-page">
    <!-- Navbar -->
    <top-tool-bar-menu/>

    <!-- Conteúdo principal -->
    <div class="profile-header">
      <div class="profile-picture-wrapper">
        <img class="profile-picture" :src="profileImage" alt="Foto de Perfil"/>
        <h2 class="username-display">{{ username }}</h2>
        <v-btn v-if="userStore.id === userId" color="#427F99" rounded="lg" variant="outlined" to="/editProfile" prepend-icon="fa-solid fa-pen">Editar Perfil</v-btn>
      </div>
    </div>

    <div class="content-container">
      <!-- Visitas Recentes -->
      <div class="recent-visits">
        <h3 class="d-inline-flex align-center">
          <v-icon icon="fa-solid fa-map-marker-alt" class="mr-2" />
          Visitas Recentemente
        </h3>
        <ul>
          <li v-for="(ponto, index) in visitedPoints" :key="index"><v-btn
              variant="text"
              :ripple="false"
              @click="$router.push(`/pi/details/${ponto.id}`)"
          >
            {{ ponto.name }}
          </v-btn></li>
          <li v-if="visitedPoints.length === 0">Sem visitas recentes.</li>
        </ul>
      </div>
        <div v-if="enlargedImage" class="modal" @click="enlargedImage = null">
          <img :src="enlargedImage" alt="Imagem ampliada" class="modal-image" />
        </div>

      <!-- Mapa no centro -->
      <div class="map-container" ref="mapContainer"></div>

      <!-- Galeria à direita -->
      <div class="gallery">
       <div
  class="gallery-header clickable-text"
  role="button"
  tabindex="0"
  @click="goToGallery"
  @keydown.enter="goToGallery"
>
  <v-icon icon="fa-solid fa-camera" class="mr-2" />
  Galeria de fotos
</div>


        <div class="gallery-grid">
      <div
        v-for="img in images"
        :key="img.id"
        class="gallery-item"
        @click="openImage(img.url)"

      >
        <img
          :src="img.url"
          :alt="'Foto ' + img.id"
          class="gallery-image"
        />
        <p class="photo-caption">{{ img.pontoInteresseName || 'Miradouro desconhecido' }}</p>
      </div>
          
          <p v-if="images.length === 0">Sem imagens publicadas.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {useRoute, useRouter} from 'vue-router'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import TopToolBarMenu from "../components/TopToolBarMenu.vue";
import { ref, onMounted, watch } from 'vue'
import {UserStore} from "@/store/userStore.js";

const router = useRouter()
const route = useRoute()
const userId = ref(route.params.id)

const userStore = UserStore()
const token = userStore.authToken

const profileImage = ref('')
const username = ref(userStore.username)

// Dados do backend
const visitedPoints = ref([]) // {name, lat, lng}
const images = ref([]) // {id, url, ...}


const API_BASE_URL = import.meta.env.VITE_API_URL;

const enlargedImage = ref(null)
const openImage = (url) => {
  enlargedImage.value = url
}
const goToGallery = () => {
  router.push(`/photoGallery/${userId.value}`)
}

const fetchUserData = async () => {

  if (userId.value === userStore.id) {
    console.log("Using user data")
    profileImage.value = userStore.avatarUrl
    username.value = userStore.username
  } else {
    console.log("Fetching user data")
    const res = await fetch(`${API_BASE_URL}/user/${userId.value}`, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })

    if (!res.ok) {
      console.warn("Informações do utilizador não encontradas")
      return
    }

    const data = await res.json()
    console.log(data)
    profileImage.value = data.profileImage
    username.value = data.username
  }
}

// Fetch pontos visitados
const fetchVisitedPoints = async () => {
  try {
    const res = await fetch(`${API_BASE_URL}/user/pontos/${userId.value}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    if (!res.ok) {
      console.warn('Erro a carregar pontos visitados')
      return
    }
    const data = await res.json()

    // 💡 Mapear latitude/longitude para lat/lng
    visitedPoints.value = data.map(ponto => ({
      ...ponto,
      lat: ponto.latitude,
      lng: ponto.longitude
    }))
  } catch (err) {
    console.error('Erro fetch pontos:', err)
  }
}


// Fetch galeria de imagens do utilizador
const fetchImages = async () => {
  try {
    const res = await fetch(`${API_BASE_URL}/user/images/${userId.value}`, {
      headers: {Authorization: `Bearer ${token}`}
    })
    if (!res.ok) {
      console.warn('Erro a carregar imagens')
      return
    }
    const data = await res.json()
    // Supondo que retorna lista com {id, url}
    images.value = data.sort((a, b) => new Date(b.date) - new Date(a.date)) // ordem cronológica decrescente
  } catch (err) {
    console.error('Erro fetch imagens:', err)
  }
}

// Setup mapa leaflet
const mapContainer = ref(null)
let map = null

// Setup mapa leaflet
const setupMap = () => {
  if (!mapContainer.value) return

  map = L.map(mapContainer.value).setView([38.7169, -9.1399], 13) // Lisboa default

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap contributors'
  }).addTo(map)

  // Ícone verde customizado
  const greenIcon = new L.Icon({
    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
  })

  const pontosValidos = visitedPoints.value.filter(p =>
      typeof p.lat === 'number' &&
      typeof p.lng === 'number' &&
      !isNaN(p.lat) &&
      !isNaN(p.lng)
  )

  if (pontosValidos.length === 0) return

  // Adiciona marcadores com ícone verde e navegação
  const markers = pontosValidos.map(ponto => {
    const marker = L.marker([ponto.lat, ponto.lng], { icon: greenIcon })
    
    // Criar popup com botão clicável
    const popupContent = `
      <div style="text-align: center;">
        <strong>${ponto.name}</strong><br>
        <button 
          onclick="window.navigateToPoint('${ponto.id}')" 
          style="
            background-color: #427F99; 
            color: white; 
            border: none; 
            padding: 5px 10px; 
            border-radius: 4px; 
            cursor: pointer; 
            margin-top: 5px;
          "
        >
          Ver Detalhes
        </button>
      </div>
    `
    
    marker.bindPopup(popupContent)
    
    return marker
  })

  markers.forEach(marker => marker.addTo(map))

  const group = new L.featureGroup(markers)
  map.fitBounds(group.getBounds().pad(0.5))
}

// Função global para navegação (chamada pelo botão no popup)
window.navigateToPoint = (pointId) => {
  router.push(`/pi/details/${pointId}`)
}

onMounted(async () => {
  await fetchUserData()
  await fetchVisitedPoints()
  await fetchImages()
  setupMap()
})

</script>

<style scoped>
.gallery-header {
  font-size: 1.25rem;
  font-weight: 600;
  color: #427F99;
  display: inline-flex;
  align-items: center;
  cursor: pointer;
  user-select: none;
  margin-bottom: 1rem;
}

.gallery-header:focus {
  outline: 2px solid #427F99;
  outline-offset: 2px;
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
  z-index: 1500;
}

.modal-image {
  max-width: 90%;
  max-height: 90%;
  border-radius: 8px;
}

.history-page {
  background-color: white;
  min-height: 100vh;
  font-family: sans-serif;
  color: white;
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

.profile-picture-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.profile-header {
  text-align: center;
  margin: 2rem 0;
}

.profile-picture-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.profile-picture {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid white;
  margin-bottom: 1rem;
  background-color: #427F99;
}

.username-display {
  color: #427F99;
  font-size: 1.5rem;
  font-weight: bold;
}
.photo-caption {
  font-size: 0.9rem;
  color: #427F99;
  text-align: center;
}

.content-container {
  display: flex;
  max-width: 1200px;
  margin: 0 auto;
  gap: 2rem;
  padding: 0 1rem;
  color: black;
}

/* Visitas recentas */
.recent-visits {
  flex: 1;
  background-color: #e6f0f5;
  border-radius: 12px;
  padding: 1rem;
  max-height: 400px;
  overflow-y: auto;
}

.recent-visits h3 {
  margin-bottom: 1rem;
  color: #427F99;
}

.recent-visits ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.recent-visits li {
  padding: 0.4rem 0;
  border-bottom: 1px solid #ccc;
  color: #427F99;
}

/* Mapa */
.map-container {
  flex: 2;
  height: 400px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 0 10px rgba(66, 127, 153, 0.4);
}

/* Galeria */
.gallery {
  flex: 1;
  background-color: #e6f0f5;
  border-radius: 12px;
  padding: 1rem;
  max-height: 400px;
  overflow-y: auto;
  text-align: center;
}

.gallery h3 {
  color: #427F99;
  margin-bottom: 1rem;
}

.gallery-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  justify-content: center;
}

.gallery-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  border: 2px solid #427F99;
}

/* Responsive */
@media (max-width: 900px) {
  .content-container {
    flex-direction: column;
  }

  .recent-visits, .gallery, .map-container {
    max-height: none;
    width: 100%;
  }
}
</style>