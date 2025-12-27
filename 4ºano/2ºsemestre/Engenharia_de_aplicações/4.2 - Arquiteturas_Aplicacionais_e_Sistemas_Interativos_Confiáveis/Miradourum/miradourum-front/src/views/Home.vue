<template>
  <div class="home-page">
    <nav class="navbar">
      <div class="navbar-left">
        <LogoButton to="/">
        </LogoButton>

      </div>
      <div class="navbar-right">
        <RouterLink to="/login" class="nav-button">Iniciar Sessão</RouterLink>
        <RouterLink to="/register" class="nav-button">Registe-se</RouterLink>
      </div>
    </nav>

    <div class="hero">
      <img class="hero-image" src="@/assets/baloicoog.jpg" alt="Miradouro" />
      <div class="hero-text">
        <h1><strong>MiradourUM</strong></h1>
        <p>Descobre os melhores miradouros e baloiços ao teu alcance!</p>
        <p>
          Com a nossa app, podes explorar a beleza das paisagens.
          A sensação de liberdade nunca foi tão boa!
          Encontra miradouros espetaculares e baloiços escondidos em locais únicos,
          planeia as tuas aventuras ao ar livre e aproveita cada momento.
        </p>
      </div>
    </div>
    <img class="corner-image" src="@/assets/hand.png" alt="Decoration" />
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import LogoButton from '@/components/LogoButton.vue'

const router = useRouter()
const route = useRoute()
const API_BASE_URL = import.meta.env.VITE_API_URL;

const handleLogoClick = () => {
  if (route.path === '/') {
    // Do something special if on homepage (e.g., scroll to top)
    window.scrollTo({ top: 0, behavior: 'smooth' })
  } else {
    router.push('/')
  }
}
</script>

<style scoped>
.home-page {
  background-color: white;
  min-height: 100vh;
  color: white;
  font-family: sans-serif;
}

.navbar {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  background-color: #427F99;
  padding: 0.75rem 1.5rem;
  color: white;
}

@media (max-width: 768px) {
  .navbar {
    flex-direction: column;
    align-items: stretch;
    text-align: center;
    gap: 1rem;
  }

  .navbar-left,
  .navbar-right {
    width: 100%;
    display: flex;
    justify-content: center;
    flex-direction: column;
    gap: 0.5rem;
  }

  .navbar-right .nav-button {
    width: 60%;
    margin: 0 auto;
  }

  .navbar-left .logo-button {
    justify-content: center;
  }
}


.navbar-left .logo-button {
  display: flex;
  align-items: center;
  background: none;
  border: none;
  color: white;
  font-size: 1.25rem;
  cursor: pointer;
}

.logo-button img {
  height: 32px;
  margin-right: 0.5rem;
}

.nav-button {
  background-color: white;
  color: #6e98db;
  border-radius: 8px;
  padding: 0.5rem 1rem;
  margin-left: 0.5rem;
  text-decoration: none;
  font-weight: bold;
  transition: background-color 0.2s ease;
}

.nav-button:hover {
  background-color: #e0e0e0;
}

.hero {
  position: relative;
  text-align: center;
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
  text-align: center;
  max-width: 600px;        /* Limit width */
  margin: 0 auto;          /* Center horizontally */
}
.corner-image {
  position: fixed;
  bottom: 0;
  right: 20px;
  width: 300px;
  height: auto;
  z-index: 10;
  pointer-events: none;
}

/* Responsive override */
@media (max-width: 768px) {
  .corner-image {
    position: static;
    display: block;
    margin: 2rem auto 1rem auto;
    width: 150px; /* smaller on small screens */
  }
}


</style>
