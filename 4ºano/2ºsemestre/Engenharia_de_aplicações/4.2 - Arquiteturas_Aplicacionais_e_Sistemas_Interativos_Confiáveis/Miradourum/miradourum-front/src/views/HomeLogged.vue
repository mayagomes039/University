<template>
  <div class="home-page">
    <TopToolBarMenu/>


    <div class="content">
      <div class="map-container">
        <!-- Division "Pontos de Interesse" e botões em cima do mapa -->
        <div class="map-header">
          <h2>Pontos de Interesse</h2>
          <div class="map-controls">
            <!-- Botão para obter localização do utilizador -->
            <v-tooltip location="bottom" text="Atualizar localização atual">
              <template v-slot:activator="{ props }">
                <button
                    v-bind="props"
                    @click="getCurrentLocation"
                    :disabled="isLoadingLocation"
                    class="location-btn"
                >
                  <span v-if="isLoadingLocation">🔄</span>
                  <span v-else>📍</span>
                  {{ isLoadingLocation ? 'Obtendo localização...' : ' Localização' }}
                </button>
              </template>
            </v-tooltip>

            <!-- Botão para pesquisar coordenadas -->
            <v-tooltip location="bottom" text="Procurar um ponto de interesse por coordenadas">
              <template #activator="{ props }">
                <button
                    @click="showSearchModal = true"
                    class="search-btn"
                    v-bind="props"
                >
                  🔍 Pesquisar Coordenadas
                </button>
              </template>
            </v-tooltip>

            <!-- Botão para filtros -->
            <v-tooltip location="bottom" text="Aplicar filtros avançados.">
              <template #activator="{ props }">
                <button
                    @click="showFiltersModal = true"
                    class="filters-btn"
                    v-bind="props"
                >
                  ⚙️ Filtros
                  <span v-if="hasActiveFilters" class="filter-indicator">●</span>
                </button>
              </template>
            </v-tooltip>

            <!-- Selector de tipo de mapa -->
            <v-tooltip location="bottom">
              <template #activator="{ props }">
                <select
                    v-model="selectedLayer"
                    @change="changeMapLayer"
                    class="layer-select"
                    v-bind="props"
                >
                  <option value="streets">Ruas</option>
                  <option value="satellite">Satélite</option>
                  <option value="terrain">Terreno</option>
                </select>
              </template>
              <span>Escolher tipo de mapa</span>
            </v-tooltip>

            <!-- Botão para adicionar ponto -->
            <v-tooltip location="bottom" text="Criar um novo Ponto de Interesse">
              <template #activator="{ props }">
                <button
                    class="add-point-button"
                    @click="goToCreate"
                    v-bind="props"
                >
                  <span class="plus-icon">+</span>
                </button>
              </template>
            </v-tooltip>
          </div>
        </div>
        
        <!-- Legenda dos pontos adicionados no mapa-->
        <div class="legend">
          <div class="legend-item">
            <span class="legend-color green"></span>
            <span>Visitado</span>
          </div>
          <div class="legend-item">
            <span class="legend-color yellow"></span>
            <span>Não Visitado</span>
          </div>
          <div class="legend-item">
            <span class="legend-color gray"></span>
            <span>Premium</span>
          </div>
          <div class="legend-item">
            <span class="legend-color blue"></span>
            <span>Sua Localização</span>
          </div>
          <div class="legend-info">
            <button
              v-if="userStore.userType === 1"
              class="help-button"
              @mouseenter="hovering = true"
              @mouseleave="hovering = false"
              @click="toggleTooltip"
              aria-label="Informação"
            >
              <svg xmlns="http://www.w3.org/2000/svg" 
                  width="16" height="16" viewBox="0 0 24 24" fill="none" 
                  stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="16" x2="12" y2="12"></line>
                <line x1="12" y1="8" x2="12" y2="8"></line>
              </svg>
            </button>

            <!-- Tooltip aparece se hover OU toggle ativado -->
            <div v-if="hovering || toggled" class="tooltip">
              Se quiser ter acesso a mais miradouros torne-se membro 
              <router-link to="/subscribe/premium" class="premium-link">Premium</router-link>
            </div>

          </div>
        </div>
        
        <div class="map-content">
          <!-- Lista lateral de pontos -->
          <div class="points-sidebar" v-if="pontosInteresse.length > 0">
            <div class="sidebar-header">
              <h3>Pontos Encontrados</h3>
            </div>
            <div class="sidebar-content">
              <div class="points-list">
                <div 
                  v-for="(ponto, index) in sortedPontos" 
                  :key="ponto.id"
                  class="point-item"
                  :class="{ 'active': selectedPointId === ponto.id }"
                  @click="selectPoint(ponto)"
                >
                  <div class="point-marker" :style="{ backgroundColor: getMarkerColor(ponto) }"></div>

                  <!-- Informação a mostrar sobre cada ponto (SEM CARREGAR)-->
                  <div class="point-info">
                    <h4>{{ ponto.name }}</h4>
                    <p class="point-distance">
                      📍 {{ ponto.distanceFromUser ? ponto.distanceFromUser.toFixed(2) + ' km' : 'N/A' }}
                    </p>
                    <p class="point-rating" v-if="ponto.score && ponto.score > 0">
                      ⭐ {{ ponto.score.toFixed(1) }}
                    </p>
                    
                    <!-- São tags que aparecem no fundo de cada PI-->
                    <div class="point-tags">
                      <span v-if="ponto.visited" class="tag visited">Visitado</span>
                      <span v-if="ponto.premium" class="tag premium">Premium</span>
                      <span v-if="ponto.accessibility" class="tag accessible">Acessível</span>
                      <span v-if="ponto.difficulty" class="tag difficulty">Dificuldade: {{ ponto.difficulty }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- Mapa -->
          <div class="map-wrapper" :class="{ 'with-sidebar': pontosInteresse.length > 0}">
            <div ref="mapContainer" class="map" id="map"></div>
          </div>
        </div>
        
        <!-- Footer que indica quantos pontos foram loaded-->
        <div class="map-footer">
          <div class="stats">
            <span><strong>{{ pontosInteresse.length }}</strong> pontos encontrados</span>
            <span v-if="isLoadingPontos">Carregando pontos...</span>
          </div>
          
          <!-- Ao clicar no mapa seleciona um ponto e mostra as coordenadas desse sítio-->
          <div class="map-info" v-if="clickedLocation">
            <p><strong>Localização clicada:</strong></p>
            <p>{{ clickedLocation.lat.toFixed(6) }}, {{ clickedLocation.lng.toFixed(6) }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- TODOS OS MODAIS AGRUPADOS AQUI, FORA DO CONTAINER DO MAPA -->
    
    <!-- Modal de filtros -->
    <div v-if="showFiltersModal" class="modal-overlay" @click="closeFiltersModal">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>Filtros de Busca</h3>
          <button @click="closeFiltersModal" class="close-btn">×</button>
        </div>
        
        <div class="modal-body">
          <div class="filter-group">
            <label>Distância Máxima (km):</label>
            <input v-model.number="filters.maxDistance" type="number" min="1" max="100" step="0.1">
          </div>
          
          <div class="filter-group">
            <label>Classificação Mínima:</label>
            <select v-model="filters.minScore">
              <option :value="null">Qualquer</option>
              <option :value="1">1 estrela</option>
              <option :value="2">2 estrelas</option>
              <option :value="3">3 estrelas</option>
              <option :value="4">4 estrelas</option>
              <option :value="5">5 estrelas</option>
            </select>
          </div>
          
          <div class="filter-group">
            <label>Data Mínima de Criação:</label>
            <input v-model="filters.minCreationDate" type="date">
          </div>
          
          <div class="filter-group">
            <label>Acessibilidade:</label>
            <select v-model="filters.accessibility">
              <option :value="null">Qualquer</option>
              <option :value="true">Sim</option>
              <option :value="false">Não</option>
            </select>
          </div>
          
          <div class="filter-group">
            <label>Dificuldade Máxima:</label>
            <select v-model="filters.maxDifficulty">
              <option :value="null">Qualquer</option>
              <option :value="1">1 - Muito Fácil</option>
              <option :value="2">2 - Fácil</option>
              <option :value="3">3 - Moderado</option>
              <option :value="4">4 - Difícil</option>
              <option :value="5">5 - Muito Difícil</option>
            </select>
          </div>
        </div>
        
        <div class="modal-footer">
          <button @click="resetFilters" class="reset-btn">Limpar Filtros</button>
          <button @click="applyFilters" class="apply-btn">Aplicar Filtros</button>
        </div>
      </div>
    </div>

    <!-- Modal de pesquisa por coordenadas -->
    <div v-if="showSearchModal" class="modal-overlay" @click="closeSearchModal">
      <div class="modal search-modal" @click.stop>
        <div class="modal-header">
          <h3>Pesquisar por Coordenadas</h3>
          <button @click="closeSearchModal" class="close-btn">×</button>
        </div>
        
        <div class="modal-body">
          <div class="search-group">
            <label>Latitude:</label>
            <input v-model.number="searchCoords.lat" type="number" step="any" placeholder="Ex: 41.1579">
          </div>
          
          <div class="search-group">
            <label>Longitude:</label>
            <input v-model.number="searchCoords.lng" type="number" step="any" placeholder="Ex: -8.6291">
          </div>
          
          <div class="search-examples">
            <p><strong>Exemplos:</strong></p>
            <button @click="setSearchCoords(41.1579, -8.6291)" class="example-btn">Porto</button>
            <button @click="setSearchCoords(38.7223, -9.1393)" class="example-btn">Lisboa</button>
            <button @click="setSearchCoords(40.2033, -8.4103)" class="example-btn">Coimbra</button>
          </div>
        </div>
        
        <div class="modal-footer">
          <button @click="searchByCoordinates" :disabled="!searchCoords.lat || !searchCoords.lng" class="apply-btn">
            Ir para Localização
          </button>
        </div>
      </div>
    </div>

    <!-- Modal de detalhes do ponto -->
    <div v-if="showDetailsModal" class="modal-overlay" @click="closeDetailsModal">
      <div class="modal details-modal" @click.stop>
        <div class="modal-header">
          <h3>{{ selectedPointDetails?.name || 'Detalhes do Ponto' }}</h3>
          <button @click="closeDetailsModal" class="close-btn">×</button>
        </div>
        
        <div class="modal-body" v-if="selectedPointDetails">
          <div v-if="isLoadingDetails" class="loading-content">
            <p>Carregando detalhes...</p>
          </div>
          
          <div v-else class="details-content">
            <div class="detail-section">
              <h4>📍 Localização</h4>
              <p><strong>Coordenadas:</strong> {{ selectedPointDetails.latitude.toFixed(6) }}, {{ selectedPointDetails.longitude.toFixed(6) }}</p>
              <p v-if="selectedPointDetails.distanceFromUser">
                <strong>Distância:</strong> {{ selectedPointDetails.distanceFromUser.toFixed(2) }} km
              </p>
            </div>
            
            <div class="detail-section">
              <h4>ℹ️ Informações Gerais</h4>
              <p><strong>Status:</strong> {{ selectedPointDetails.state ? 'Ativo' : 'Inativo' }}</p>
              <p><strong>Classificação:</strong> 
                <span v-if="selectedPointDetails.score && selectedPointDetails.score > 0">
                  {{ selectedPointDetails.score.toFixed(1) }}★
                </span>
                <span v-else>Sem classificação</span>
              </p>
              <p><strong>Dificuldade:</strong> 
                <span v-if="selectedPointDetails.difficulty">
                  {{ selectedPointDetails.difficulty }} - 
                  {{ ['', 'Muito Fácil', 'Fácil', 'Moderado', 'Difícil', 'Muito Difícil'][selectedPointDetails.difficulty] }}
                </span>
                <span v-else>Não especificada</span>
              </p>
              <p><strong>Data de Criação:</strong> {{ new Date(selectedPointDetails.creationDate).toLocaleDateString('pt-PT') }}</p>
            </div>
            
            <div class="detail-section">
              <h4>🏷️ Características</h4>
              <div class="characteristics">
                <span v-if="selectedPointDetails.premium" class="char-tag premium">👑 Premium</span>
                <span v-if="selectedPointDetails.accessibility" class="char-tag accessible">♿ Acessível</span>
                <span v-if="!selectedPointDetails.premium" class="char-tag free">🆓 Gratuito</span>
                <span v-if="!selectedPointDetails.accessibility" class="char-tag free">🚫 Não Acessível a Cadeira de Rodas</span>
              </div>
            </div>
            
            <div class="detail-section" v-if="selectedPointDetails.description">
              <h4>📝 Descrição</h4>
              <div class="description-content">{{ selectedPointDetails.description }}</div>
            </div>
          </div>
        </div>
        
        <div class="modal-footer">
          <button @click="closeDetailsModal" class="reset-btn">Fechar</button>
          <button @click="goToDetailsPage" class="apply-btn">Obter mais detalhes</button>
        </div>
      </div>
    </div>
    <SuccessPopup v-if="showSuccessPopup" :text="successMessage" />
    <ErrorPopup v-if="showError" :text="errorText" />

  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import LogoButton from '@/components/LogoButton.vue'
import TopToolBarMenu from "../components/TopToolBarMenu.vue";
import {UserStore} from "@/store/userStore.js";
import { calculateDistance } from '../utils/distance.js'
import SuccessPopup from '@/components/SuccessPopup.vue'
import ErrorPopup from '@/components/ErrorPopup.vue'

const showSuccessPopup = ref(false)
const successMessage = ref('')


const router = useRouter()
const route = useRoute()
const userStore = UserStore()

// Refs para o mapa e dados
const mapContainer = ref(null)
const userLocation = ref(userStore.location)
const clickedLocation = ref(null)
const isLoadingLocation = ref(false)
const selectedLayer = ref('streets')
const pontosInteresse = ref([])
const isLoadingPontos = ref(false)
const selectedPointId = ref(null)

// Modal de detalhes do ponto
const showDetailsModal = ref(false)
const selectedPointDetails = ref(null)
const isLoadingDetails = ref(false)

// Modais (Popups)
const showFiltersModal = ref(false)
const showSearchModal = ref(false)

// Pesquisa por coordenadas
const searchCoords = ref({
  lat: null,
  lng: null
})
const API_BASE_URL = import.meta.env.VITE_API_URL;


const hovering = ref(false)
const toggled = ref(false)

function toggleTooltip() {
  toggled.value = !toggled.value
}


// Filtros
const filters = ref({
  maxDistance: 20.0,
  minScore: null,
  minCreationDate: null,
  accessibility: null,
  maxDifficulty: null
})
const showError = ref(false)
const errorText = ref('')

let map = null
let userMarker = null
let clickMarker = null
let piMarkers = []

// Configurações dos layers e cores dos marcadores
const mapLayers = {
  streets: {
    url: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
    attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
  },
  satellite: {
    url: 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}',
    attribution: '© <a href="https://www.esri.com/">Esri</a>, © <a href="https://www.digitalglobe.com/">DigitalGlobe</a>'
  },
  terrain: {
    url: 'https://{s}.tile.opentopomap.org/{z}/{x}/{y}.png',
    attribution: '© <a href="https://opentopomap.org">OpenTopoMap</a> contributors'
  }
}

const markerColors = {
  visited: '#28a745',    // Verde
  notVisited: '#ffc107', // Amarelo
  premium: '#6c757d',    // Cinzento
  user: '#007bff'        // Azul
}

const isAdmin = ref(false)


// Computed
const hasActiveFilters = computed(() => {
  return filters.value.maxDistance !== 20.0 ||
         filters.value.minScore !== null ||
         filters.value.minCreationDate !== null ||
         filters.value.accessibility !== null ||
         filters.value.maxDifficulty !== null
})
const goToCreate = () => {
  router.push('/create')
}

const triggerSuccessPopup = (message) => {
  successMessage.value = message
  showSuccessPopup.value = true
  setTimeout(() => {
    showSuccessPopup.value = false
  }, 3000)
}

const errorMessage = ref('')

// Ativa o popup de erro no canto esquerdo
function showErrorMsg(msg) {
  errorMessage.value = msg
  successMessage.value = '' // Limpar mensagem de sucesso
  setTimeout(() => {
    errorMessage.value = ''
  }, 3000)
}


const checkAdmin = async () => {
  try {
    const token = userStore.authToken // or wherever your JWT is stored
    if (!token) return

    const res = await fetch(`${API_BASE_URL}/admin/isadmin`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })

    if (res.ok) {
      const data = await res.json()
      isAdmin.value = data === true
    } else {
      isAdmin.value = false
    }
  } catch (error) {
    console.error('Failed to check admin status:', error)
    isAdmin.value = false
  }
}




const sortedPontos = computed(() => {
  return [...pontosInteresse.value].sort((a, b) => {
    const distA = a.distanceFromUser || 0
    const distB = b.distanceFromUser || 0
    return distA - distB
  })
})

// Função para obter cor do marcador
const getMarkerColor = (ponto) => {
  if (ponto.visited) return markerColors.visited
  if (ponto.premium) return markerColors.premium
  return markerColors.notVisited
}




// Funções do mapa
const initMap = () => {
  const initialLat = 41.1579
  const initialLng = -8.6291
  
  map = L.map('map').setView([initialLat, initialLng], 13)
  
  L.tileLayer(mapLayers.streets.url, {
    attribution: mapLayers.streets.attribution,
    maxZoom: 19
  }).addTo(map)
  
  map.on('click', (e) => {
    clickedLocation.value = {
      lat: e.latlng.lat,
      lng: e.latlng.lng
    }
    
    if (clickMarker) {
      map.removeLayer(clickMarker)
    }
    
    clickMarker = L.marker([e.latlng.lat, e.latlng.lng])
      .addTo(map)
      .bindPopup(`<b>Localização selecionada</b><br>Lat: ${e.latlng.lat.toFixed(6)}<br>Lng: ${e.latlng.lng.toFixed(6)}`)
      .openPopup()
  })
}

// Função para criar ícone colorido customizado
const createColoredIcon = (color, isSelected = false) => {
  const size = isSelected ? 35 : 25
  const borderWidth = isSelected ? 4 : 3
  
  return L.divIcon({
    className: 'custom-marker',
    html: `<div style="
      background-color: ${color};
      width: ${size}px;
      height: ${size}px;
      border-radius: 50%;
      border: ${borderWidth}px solid white;
      box-shadow: 0 3px 6px rgba(0,0,0,0.4);
      cursor: pointer;
      transition: all 0.2s ease;
      ${isSelected ? 'transform: scale(1.1);' : ''}
    "></div>`,
    iconSize: [size, size],
    iconAnchor: [size/2, size/2]
  })
}

// Função para buscar pontos de interesse na API
const fetchPontosInteresse = async () => {
  if (!userStore.location) {
    console.log('Aguardando localização do usuário...')
    return
  }

  isLoadingPontos.value = true
  
  try {
    const token = userStore.authToken
    if (!token) {
      throw new Error('Token não encontrado')
    }

    const requestBody = {
      userLatitude: userStore.location.lat,
      userLongitude: userStore.location.lng,
    }

    if (filters.value.maxDistance !== 20.0) {
      requestBody.maxDistance = filters.value.maxDistance
    }
    
    if (filters.value.minScore !== null) {
      requestBody.minScore = filters.value.minScore
    }
    
    if (filters.value.minCreationDate !== null && filters.value.minCreationDate !== '') {
      requestBody.minCreationDate = filters.value.minCreationDate + 'T00:00:00'
    }
    
    if (filters.value.accessibility !== null) {
      requestBody.accessibility = filters.value.accessibility
    }
    
    if (filters.value.maxDifficulty !== null) {
      requestBody.maxDifficulty = filters.value.maxDifficulty
    }

    console.log('Enviando requisição:', requestBody)

    const response = await fetch(`${API_BASE_URL}/pi/filtered`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(requestBody)
    })

    if (!response.ok) {
      const errorText = await response.text()
      console.error('Erro da API:', response.status, errorText)
      throw new Error(`Erro na API: ${response.status} - ${errorText}`)
    }

    const data = await response.json()
    console.log('Pontos recebidos:', data)
    // Calcular distâncias e atualizar pontos
   // Remove duplicates by ID
    const uniqueMap = new Map()
    data.forEach(ponto => {
      if (!uniqueMap.has(ponto.id)) {
        const enrichedPonto = {
          ...ponto,
          distanceFromUser: userLocation.value
            ? calculateDistance(userLocation.value.lat, userLocation.value.lng, ponto.latitude, ponto.longitude)
            : null
        }
        uniqueMap.set(ponto.id, enrichedPonto)
      }
    })
    pontosInteresse.value = Array.from(uniqueMap.values())

    
    // Limpar marcadores anteriores
    clearPIMarkers()
    
    // Adicionar novos marcadores
    addPIMarkersToMap(data)

  } catch (error) {
    console.error('Erro ao buscar pontos de interesse:', error)
    showErrorMsg("Não foi possível obter pontos de interesse. Tente novamente mais tarde.")
  } finally {
    isLoadingPontos.value = false
  }
}

// Função para limpar marcadores de PIs
const clearPIMarkers = () => {
  piMarkers.forEach(marker => {
    map.removeLayer(marker)
  })
  piMarkers = []
  selectedPointId.value = null
}

// Adicione esta função global para o botão do popup:
window.openPointDetails = (pointId) => {
  router.push(`/pi/details/${pointId}`)
}

// Substitua a função addPIMarkersToMap existente por esta versão corrigida:

const addPIMarkersToMap = (pontos) => {
  pontos.forEach(ponto => {
    const color = getMarkerColor(ponto)
    const isSelected = selectedPointId.value === ponto.id

    // Calcular distância aqui se necessário
    const distance = userLocation.value ? 
      calculateDistance(userLocation.value.lat, userLocation.value.lng, ponto.latitude, ponto.longitude) : null

    const marker = L.marker([ponto.latitude, ponto.longitude], {
      icon: createColoredIcon(color, isSelected)
    }).addTo(map)

    const popupContent = `
      <div class="pi-popup">
        <h4>${ponto.name}</h4>
        ${ponto.description ? `<p><strong>Descrição:</strong> ${ponto.description}</p>` : ''}
        <p><strong>Classificação:</strong> ${ponto.score && ponto.score > 0 ? ponto.score.toFixed(1) + '★' : 'Sem classificação'}</p>
        <p><strong>Data de Criação:</strong> ${new Date(ponto.creationDate).toLocaleDateString('pt-PT')}</p>
        <p><strong>Distância:</strong> ${distance ? distance.toFixed(2) + ' km' : 'N/A'}</p>
        <p><strong>Status:</strong> ${ponto.visited ? 'Visitado' : 'Não visitado'}</p>
        <p><strong>Dificuldade:</strong> ${ponto.difficulty || 'N/A'}</p>
        <p><strong>Coordenadas:</strong> ${ponto.latitude.toFixed(6)}, ${ponto.longitude.toFixed(6)}</p>
        ${ponto.premium ? '<p><strong>Tipo:</strong> Premium 👑</p>' : '<p><strong>Tipo:</strong> Básico</p> '}
        ${ponto.accessibility ? '<p>♿ Acessível</p>' : '<p>♿ Não acessível a cadeira de rodas</p>'}
        
        <div style="margin-top: 10px;">
          <button onclick="window.openPointDetails(${ponto.id})" style="
            background-color: #427F99; 
            color: white; 
            border: none; 
            padding: 8px 16px; 
            border-radius: 4px; 
            cursor: pointer;
            font-size: 0.85rem;
          ">Ver Detalhes</button>
        </div>
      </div>
    `

    
    // Apenas UM event listener de clique
    marker.on('click', () => {
      // Definir o ponto selecionado visualmente
      selectedPointId.value = ponto.id
      
      // Atualizar marcadores visuais
      piMarkers.forEach(({ marker, ponto: markerPonto }) => {
        const color = getMarkerColor(markerPonto)
        const isSelected = selectedPointId.value === markerPonto.id
        marker.setIcon(createColoredIcon(color, isSelected))
      })
      
      // Centralizar no ponto
      map.setView([ponto.latitude, ponto.longitude], 16)
      
      // Abrir modal com detalhes
      fetchPointDetails(ponto.id)
    })
    
    piMarkers.push({ marker, ponto })
  })
}

// Função para buscar detalhes de um ponto específico
const fetchPointDetails = async (pointId) => {
  isLoadingDetails.value = true

  try {
    const token = userStore.authToken
    if (!token) throw new Error('Token não encontrado')

    const response = await fetch(`${API_BASE_URL}/pi/shortdetails/${pointId}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })

if (response.status === 204) {
  errorText.value = 'Este conteúdo é exclusivo para utilizadores premium.'
  showError.value = true
  setTimeout(() => {
    showError.value = false
  }, 3000)
  return
}



    if (!response.ok) {
      const errorText = await response.text()
      throw new Error(`Erro na API: ${response.status} - ${errorText}`)
    }

    const data = await response.json()
    console.log('Detalhes do ponto recebidos:', data)

    if (userLocation.value) {
      data.distanceFromUser = calculateDistance(
        userLocation.value.lat,
        userLocation.value.lng,
        data.latitude,
        data.longitude
      )
    }

    selectedPointDetails.value = data
    showDetailsModal.value = true

  } catch (error) {
    console.error('Erro ao buscar detalhes do ponto:', error)
    showErrorMsg("Erro ao carregar detalhes do Ponto de Interesse. Tente Novamente mais tarde!")
  } finally {
    isLoadingDetails.value = false
  }
}


// Função para selecionar um ponto
const selectPoint = (ponto) => {
  selectedPointId.value = ponto.id
  
  // Atualizar todos os marcadores
  piMarkers.forEach(({ marker, ponto: markerPonto }) => {
    const color = getMarkerColor(markerPonto)
    const isSelected = selectedPointId.value === markerPonto.id
    marker.setIcon(createColoredIcon(color, isSelected))
  })
  
  // Centralizar mapa no ponto
  map.setView([ponto.latitude, ponto.longitude], 16)
  
  // Buscar detalhes do ponto e abrir modal
  fetchPointDetails(ponto.id)
}

// Função para obter localização atual
const getCurrentLocation = () => {
  if (!navigator.geolocation) {
    showErrorMsg('Geolocalização não é suportada neste navegador.')
    return
  }
  
  isLoadingLocation.value = true
  
  navigator.geolocation.getCurrentPosition(
    (position) => {
      const lat = position.coords.latitude
      const lng = position.coords.longitude

      userStore.setLocation({
        'lng': lng,
        'lat': lat,
      })
      
      userLocation.value = { lat, lng }
      
      map.setView([lat, lng], 15)
      
      if (userMarker) {
        map.removeLayer(userMarker)
      }
      
      userMarker = L.marker([lat, lng], {
        icon: createColoredIcon(markerColors.user)
      }).addTo(map)
        .bindPopup(`<b>Sua localização</b><br>Lat: ${lat.toFixed(6)}<br>Lng: ${lng.toFixed(6)}`)
        .openPopup()
      
      L.circle([lat, lng], {
        color: markerColors.user,
        fillColor: markerColors.user,
        fillOpacity: 0.2,
        radius: position.coords.accuracy || 100
      }).addTo(map)
      
      isLoadingLocation.value = false
      
      // Buscar pontos de interesse após obter localização
      fetchPontosInteresse()
    },
    (error) => {
      console.error('Erro ao obter localização:', error)
      let errorMessage = 'Erro ao obter localização.'
      
      switch(error.code) {
        case error.PERMISSION_DENIED:
          errorMessage = 'Permissão de localização negada.'
          break
        case error.POSITION_UNAVAILABLE:
          errorMessage = 'Localização não disponível.'
          break
        case error.TIMEOUT:
          errorMessage = 'Tempo limite para obter localização.'
          break
      }
      showErrorMsg(errorMessage)
      isLoadingLocation.value = false
    },
    {
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 60000
    }
  )
}

// Função para trocar layer do mapa
const changeMapLayer = () => {
  map.eachLayer((layer) => {
    if (layer instanceof L.TileLayer) {
      map.removeLayer(layer)
    }
  })
  
  const selectedLayerConfig = mapLayers[selectedLayer.value]
  L.tileLayer(selectedLayerConfig.url, {
    attribution: selectedLayerConfig.attribution,
    maxZoom: 19
  }).addTo(map)
}

// Funções dos modais
const closeFiltersModal = () => {
  showFiltersModal.value = false
}

const closeSearchModal = () => {
  showSearchModal.value = false
}

const closeDetailsModal = () => {
  showDetailsModal.value = false
  selectedPointDetails.value = null
}

// Função para ir para a página de detalhes
const goToDetailsPage = () => {
  if (selectedPointDetails.value && selectedPointDetails.value.id) {
    router.push(`/pi/details/${selectedPointDetails.value.id}`)
  }
}

// Função para ir para o perfil
const goToProfile = () => {
  router.push('/history')
}

const resetFilters = () => {
  filters.value = {
    maxDistance: 20.0,
    minScore: null,
    minCreationDate: null,
    accessibility: null,
    maxDifficulty: null
  }
  triggerSuccessPopup('Filtros limpos!')
}

const applyFilters = () => {
  closeFiltersModal()
  fetchPontosInteresse()
  triggerSuccessPopup('Filtros aplicados com sucesso!')
}

// Funções de pesquisa por coordenadas
const setSearchCoords = (lat, lng) => {
  searchCoords.value = { lat, lng }
}

const searchByCoordinates = () => {
  if (searchCoords.value.lat && searchCoords.value.lng) {
    map.setView([searchCoords.value.lat, searchCoords.value.lng], 15)
    
    if (clickMarker) {
      map.removeLayer(clickMarker)
    }
    
    clickMarker = L.marker([searchCoords.value.lat, searchCoords.value.lng])
      .addTo(map)
      .bindPopup(`<b>Localização pesquisada</b><br>Lat: ${searchCoords.value.lat}<br>Lng: ${searchCoords.value.lng}`)
      .openPopup()
    
    closeSearchModal()
    searchCoords.value = { lat: null, lng: null }
  }
}

// Lifecycle hooks
onMounted(() => {
  const loadLeaflet = () => {
    return new Promise((resolve) => {
      if (!document.querySelector('link[href*="leaflet"]')) {
        const cssLink = document.createElement('link')
        cssLink.rel = 'stylesheet'
        cssLink.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'
        cssLink.integrity = 'sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY='
        cssLink.crossOrigin = ''
        document.head.appendChild(cssLink)
      }
      
      if (!window.L) {
        const script = document.createElement('script')
        script.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'
        script.integrity = 'sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo='
        script.crossOrigin = ''
        script.onload = resolve
        document.head.appendChild(script)
      } else {
        resolve()
      }
    },
    checkAdmin())
  }
  
  loadLeaflet().then(() => {
    setTimeout(() => {
      initMap()
      getCurrentLocation()
    }, 100)
  })
})

onUnmounted(() => {
  if (map) {
    map.remove()
  }
})

const handleLogoClick = () => {
  if (route.path === '/') {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  } else {
    router.push('/')
  }
}
</script>

<style scoped>
/* Estilos base da página */
.home-page {
  background-color: white;
  min-height: 100vh;
  color: white;
  font-family: sans-serif;
  position: relative;
}

.hero {
  position: relative;
  text-align: center;
  z-index: 1;
}


.hero-image {
  width: 100%;
  max-height: 500px;
  object-fit: cover;
}

.hero-text {
  background-color: white;
  color: black;
  padding: 1rem;
  max-width: 1000px;
  margin: 0 auto;
}

.location-info {
  background-color: #f8f9fa;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  border: 1px solid #e9ecef;
  margin-left: 0.5rem;
}

.location-text {
  font-size: 0.9rem;
  color: #495057;
  font-weight: 500;
}

/* Conteúdo principal */
.content {
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;
}

/* Container do mapa */
.map-container {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  z-index: 0;
}

.map-header {
  padding: 1.5rem;
  border-bottom: 1px solid #e9ecef;
  background-color: #f8f9fa;
}

.map-header h2 {
  margin: 0 0 1rem 0;
  color: #333;
  font-size: 1.5rem;
  font-weight: 600;
}

.map-controls {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
  align-items: center;
}

.location-btn,
.search-btn,
.filters-btn {
  padding: 0.75rem 1rem;
  border: 1px solid #427F99;
  background-color: white;
  color: #427F99;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.location-btn:hover,
.search-btn:hover,
.filters-btn:hover {
  background-color: #427F99;
  color: white;
}

.location-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.filters-btn {
  position: relative;
}

.filter-indicator {
  color: #dc3545;
  font-size: 1.2rem;
  margin-left: 0.25rem;
}

.layer-select {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  background-color: white;
  color: #333;
  font-size: 0.9rem;
  cursor: pointer;
}

/* Legenda */
.legend {
  display: flex;
  gap: 1.5rem;
  padding: 1rem 1.5rem;
  background-color: white;
  border-bottom: 1px solid #e9ecef;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.85rem;
  color: #666;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid white;
  box-shadow: 0 1px 3px rgba(0,0,0,0.2);
}

.legend-color.green { background-color: #28a745; }
.legend-color.yellow { background-color: #ffc107; }
.legend-color.gray { background-color: #6c757d; }
.legend-color.blue { background-color: #007bff; }

/* Estilos da sidebar lateral */
.map-content {
  display: flex;
  height: 600px;
}

.points-sidebar {
  width: 350px;
  border-right: 1px solid #e5e5e5;
  background-color: #f8f9fa;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
}

.points-sidebar.collapsed {
  width: 40px;
}

.sidebar-header {
  padding: 1rem;
  border-bottom: 1px solid #e5e5e5;
  background-color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 1rem;
  color: #333;
}

.toggle-sidebar-btn {
  background: none;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  color: #666;
  padding: 0.25rem;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.toggle-sidebar-btn:hover {
  background-color: #e9ecef;
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
}

.points-list {
  padding: 0.5rem;
}

.point-item {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 1rem;
  margin-bottom: 0.5rem;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 2px solid transparent;
}

.point-item:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transform: translateY(-1px);
}

.point-item.active {
  border-color: #427F99;
  background-color: #f0f8ff;
}

.point-marker {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.2);
  flex-shrink: 0;
  margin-top: 0.25rem;
}

.point-info {
  flex: 1;
  min-width: 0;
}

.point-info h4 {
  margin: 0 0 0.5rem 0;
  font-size: 1rem;
  color: #333;
  font-weight: 600;
}

.point-type,
.point-distance,
.point-rating {
  margin: 0.25rem 0;
  font-size: 0.85rem;
  color: #666;
}

.point-tags {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.5rem;
  flex-wrap: wrap;
}

.tag {
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
}

.tag.visited {
  background-color: #d4edda;
  color: #155724;
}

.tag.premium {
  background-color: #e2e3e5;
  color: #383d41;
}

.tag.accessible {
  background-color: #cce5ff;
  color: #004085;
}

.tag.difficulty {
  background-color: #fff3cd;
  color: #856404;
}

.map-wrapper {
  flex: 1;
  position: relative;
}

.map-wrapper.with-sidebar {
  border-left: 1px solid #e5e5e5;
}

/* Mapa */
.map {
  width: 100%;
  height: 100%;
  border-radius: 0;
}

/* Footer do mapa */
.map-footer {
  padding: 1rem 1.5rem;
  background-color: #f8f9fa;
  border-top: 1px solid #e9ecef;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
}

.stats {
  display: flex;
  gap: 1rem;
  align-items: center;
  flex-wrap: wrap;
}

.stats span {
  color: #666;
  font-size: 0.9rem;
}

.map-info {
  font-size: 0.85rem;
  color: #666;
}

.map-info p {
  margin: 0.25rem 0;
}

/* Modais */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.modal {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.search-modal {
  max-width: 400px;
}

.modal-header {
  padding: 1.5rem;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  color: #333;
  font-size: 1.25rem;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #666;
  padding: 0.25rem;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.close-btn:hover {
  background-color: #f8f9fa;
}

.modal-body {
  padding: 1.5rem;
}

.filter-group,
.search-group {
  margin-bottom: 1.5rem;
}

.filter-group label,
.search-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #333;
}

.filter-group input,
.filter-group select,
.search-group input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 0.9rem;
  transition: border-color 0.2s;
}

.filter-group input:focus,
.filter-group select:focus,
.search-group input:focus,
.nav-button:focus,
.location-btn:focus,
.search-btn:focus,
.filters-btn:focus,
.reset-btn:focus,
.apply-btn:focus {
  outline: none;
  border-color: #427F99;
  box-shadow: 0 0 0 2px rgba(66, 127, 153, 0.2);
}

.search-examples {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #e9ecef;
}

.search-examples p {
  margin: 0 0 0.75rem 0;
  font-size: 0.9rem;
  color: #666;
}

.example-btn {
  padding: 0.5rem 1rem;
  margin: 0.25rem 0.5rem 0.25rem 0;
  border: 1px solid #ddd;
  background-color: white;
  color: #666;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.85rem;
  transition: all 0.2s;
}

.example-btn:hover {
  background-color: #f8f9fa;
  border-color: #427F99;
  color: #427F99;
}

.modal-footer {
  padding: 1.5rem;
  border-top: 1px solid #e9ecef;
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

.reset-btn,
.apply-btn {
  padding: 0.75rem 1.5rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.2s;
}

.reset-btn {
  background-color: white;
  color: #666;
}

.reset-btn:hover {
  background-color: #f8f9fa;
  border-color: #adb5bd;
}

.apply-btn {
  background-color: #427F99;
  color: white;
  border-color: #427F99;
}

.apply-btn:hover {
  background-color: #356b83;
}

.apply-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Estilos para marcadores customizados */
:global(.custom-marker) {
  background: none !important;
  border: none !important;
}

:global(.custom-marker div) {
  transition: all 0.2s ease;
}

:global(.custom-marker:hover div) {
  transform: scale(1.1);
  box-shadow: 0 4px 8px rgba(0,0,0,0.3) !important;
}

/* Estilos para popups */
:global(.pi-popup) {
  font-size: 0.9rem;
  line-height: 1.4;
}

:global(.pi-popup h4) {
  margin: 0 0 0.5rem 0;
  color: #333;
  font-size: 1rem;
}

:global(.pi-popup p) {
  margin: 0.25rem 0;
  color: #666;
}


.legend-info {
  margin-left: auto; /* Empurra para a direita */
  position: relative;
}

.help-button-container {
  position: relative;
}

.help-button {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 1px solid #ccc;
  background: #f8f9fa;
  color: #666;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  padding: 0;
}

.help-button:hover {
  background: #e9ecef;
  border-color: #adb5bd;
  color: #495057;
}

.help-button svg {
  stroke: currentColor;
}

.tooltip {
  position: absolute;
  top: 34px;
  right: 0;
  background: #333;
  color: white;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 12px;
  white-space: nowrap;
  z-index: 1000;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
}

.tooltip::before {
  content: '';
  position: absolute;
  top: -6px;
  right: 8px;
  width: 0;
  height: 0;
  border-left: 6px solid transparent;
  border-right: 6px solid transparent;
  border-bottom: 6px solid #333;
}

.premium-link {
  color: #ffd700;
  text-decoration: none;
  font-weight: bold;
}

.premium-link:hover {
  text-decoration: underline;
}


/* Responsividade */
@media (max-width: 1200px) {
  .content {
    padding: 1rem;
  }
  
  .points-sidebar {
    width: 300px;
  }
  
  .map-content {
    height: 500px;
  }
}

@media (max-width: 768px) {
  .navbar {
    padding: 1rem;
  }
  
  .content {
    padding: 0.5rem;
  }
  
  .map-header {
    padding: 1rem;
  }
  
  .map-controls {
    flex-direction: column;
    align-items: stretch;
  }
  
  .location-btn,
  .search-btn,
  .filters-btn,
  .layer-select {
    width: 100%;
    justify-content: center;
  }
  
  .legend {
    padding: 0.75rem;
    gap: 1rem;
  }
  
  .map-content {
    flex-direction: column;
    height: auto;
  }

  .points-sidebar {
    width: 100%;
    max-height: 200px;
    border-right: none;
    border-bottom: 1px solid #e5e5e5;
  }

  .points-sidebar.collapsed {
    width: 100%;
    max-height: 60px;
  }

  .map-wrapper {
    height: 400px;
  }

  .map-wrapper.with-sidebar {
    border-left: none;
    border-top: 1px solid #e5e5e5;
  }

  .point-item {
    padding: 0.75rem;
  }

  .point-info h4 {
    font-size: 0.9rem;
  }

  .point-type,
  .point-distance,
  .point-rating {
    font-size: 0.8rem;
  }
  
  .map-footer {
    padding: 0.75rem;
    flex-direction: column;
    align-items: flex-start;
  }
  
  .modal {
    width: 95%;
    margin: 1rem;
  }
  
  .modal-header,
  .modal-body,
  .modal-footer {
    padding: 1rem;
  }
}

/* Modal de detalhes */
.details-modal {
  max-width: 600px;
  max-height: 90vh;
}

.loading-content {
  text-align: center;
  padding: 2rem;
  color: #666;
}

.details-content {
  max-height: 60vh;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #f0f0f0;
}

.detail-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.detail-section h4 {
  margin: 0 0 0.75rem 0;
  color: #333;
  font-size: 1rem;
  font-weight: 600;
}

.detail-section p {
  margin: 0.5rem 0;
  color: #666;
  line-height: 1.5;
}

.characteristics {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.char-tag {
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 500;
}

.char-tag.premium {
  background-color: #fff3cd;
  color: #856404;
  border: 1px solid #ffeaa7;
}

.char-tag.accessible {
  background-color: #cce5ff;
  color: #004085;
  border: 1px solid #b3d9ff;
}

.char-tag.free {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.description-content {
  color: #333;
  line-height: 1.6;
  word-wrap: break-word;
}

.description-content img {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 0.5rem 0;
}
.navbar-right {
  display: flex;
  align-items: center;
  gap: 1rem; /* space between Review button and location info */
}


@media (max-width: 480px) {
  .navbar-right .location-info {
    display: none;
  }
  
  .legend {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .map-content {
    height: auto;
  }
  
  .map-wrapper {
    height: 300px;
  }
  
  .modal-footer {
    flex-direction: column;
  }
  
  .reset-btn,
  .apply-btn {
    width: 100%;
  }

  .navbar-right > * {
    display: inline-flex;
    align-items: center;
    white-space: nowrap;
  }
}

.add-point-button {
  background-color: #3cb371; /* red */
  color: white;
  font-size: 1.5rem;
  font-weight: bold;
  border: none;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  margin-right: 1rem;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
  transition: background-color 0.2s ease;
}

.add-point-button:hover {
  background-color: #2e8b57;
}

.plus-icon {
  line-height: 1;
  font-size: 1.8rem;
  margin-top: -2px;
}

</style>