<template>
  <div class="gallery-page">
    <!-- Navbar -->
<!--    <nav class="navbar">-->
<!--      <div class="navbar-left">-->
<!--        <LogoButton to="/" />-->
<!--        <button class="nav-button" @click="goBack">← Anterior</button>-->
<!--      </div>-->

<!--      <div class="navbar-right">-->
<!--        <button class="nav-button" @click="handleLogout">Terminar Sessão ⎋</button>-->
<!--      </div>-->
<!--    </nav>-->
    <top-tool-bar-menu />

    <!-- Conteúdo da galeria -->
    <div class="gallery-container">
      <h2>Galeria de Fotografias</h2>
      <div class="gallery-grid">
       <div
  v-for="img in images"
  :key="img.id"
  class="gallery-item"
  @click="openImage(img.url)"
>
  <img :src="img.url" :alt="'Foto ' + img.id" class="gallery-image" />
  <p class="photo-caption">{{ img.pontoInteresseName || 'Miradouro desconhecido' }}</p>
</div>

        <p v-if="images.length === 0">Sem imagens disponíveis.</p>
      </div>
    </div>
  <div v-if="enlargedImage" class="modal" @click="closeImage">
  <img :src="enlargedImage" alt="Imagem ampliada" class="modal-image" />
</div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import TopToolBarMenu from "@/components/TopToolBarMenu.vue";
import {UserStore} from "@/store/userStore.js";



const router = useRouter()
const route = useRoute()
const userStore = UserStore()
const token = userStore.authToken
const images = ref([])
const API_BASE_URL = import.meta.env.VITE_API_URL;
const userId = ref(route.params.id)
const goBack = () => router.push('/home')
const handleLogout = () => {
  localStorage.removeItem('authToken')
  router.push('/login')
}
const enlargedImage = ref(null);
const openImage = (url) => {
  enlargedImage.value = url;
};
const closeImage = () => {
  enlargedImage.value = null;
};

const fetchImages = async () => {
  try {
    const res = await fetch(`${API_BASE_URL}/user/images/${userId.value}`, {
      headers: { Authorization: `Bearer ${token}` }
    })

    if (!res.ok) {
      console.warn('Erro a carregar imagens')
      return
    }

    const rawImages = await res.json()

  
    console.log('Imagens enriquecidas:', rawImages)
    images.value = rawImages.sort((a, b) => new Date(b.date) - new Date(a.date))
  } catch (err) {
    console.error('Erro fetch imagens:', err)
  }
}


onMounted(fetchImages)
</script>

<style scoped>
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
  cursor: zoom-out;
}

.modal-image {
  max-width: 90%;
  max-height: 90%;
  border-radius: 8px;
  box-shadow: 0 0 15px rgba(255, 255, 255, 0.5);
}

.gallery-page {
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

.navbar-left,
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

.gallery-container {
  max-width: 1000px;
  margin: 2rem auto;
  padding: 0 1rem;
  text-align: center;
}

.gallery-container h2 {
  color: #427F99;
  margin-bottom: 1.5rem;
}

.gallery-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  justify-content: center;
}

.gallery-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 140px;
  background-color: #e6f0f5;
  border-radius: 12px;
  padding: 0.5rem;
}

.gallery-image {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
  border: 2px solid #427F99;
  margin-bottom: 0.5rem;
}

.photo-caption {
  font-size: 0.9rem;
  color: #427F99;
  text-align: center;
}

/* Responsivo */
@media (max-width: 600px) {
  .gallery-item {
    width: 100px;
  }

  .gallery-image {
    width: 90px;
    height: 90px;
  }

  .photo-caption {
    font-size: 0.8rem;
  }
}
</style>
