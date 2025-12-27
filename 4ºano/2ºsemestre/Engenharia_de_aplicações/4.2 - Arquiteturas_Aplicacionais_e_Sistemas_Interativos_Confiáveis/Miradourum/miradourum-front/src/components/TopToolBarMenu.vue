<script setup>

import LogoButton from "./LogoButton.vue";
import {ref} from "vue";
import router from "../router/index.js";
import Location_chip from "./location_chip.vue";
import {UserStore} from "@/store/userStore.js";
import { useRoute } from 'vue-router';
import { computed } from 'vue';
import GoBackButton from "@/components/goBackButton.vue";

defineProps({
  userId: String,
  avatarUrl: String,
  username: String,
  email: String
})

const userStore = UserStore();

const handleLogout = () => {
  userStore.clearUserStore()
  router.push('/login')                  // Redireciona para a página de login
  window.location.replace()

}
const route = useRoute(); 
const showBackButton = computed(() => route.path !== '/home');
</script>

<template>
  <v-toolbar color="#427F99" class="trips">
    <v-container fluid>
      <v-row align="center" justify="space-between" no-gutters>
        

        <v-col class="d-flex justify-end align-center" cols="auto">
          <!-- Left Side: Back Button + Logo + Location Chip -->
        <v-col class="d-flex align-center" cols="auto">
          <LogoButton to="/home" class="mr-16" />
          <location_chip class="mr-12" />

<!--          <button-->
<!--            v-if="showBackButton"-->
<!--            @click="router.back()"-->
<!--            class="nav-button"-->
<!--          >-->
<!--            Voltar-->
<!--          </button>-->
        </v-col>
          <v-navigation-drawer
              location="right"
              expand-on-hover
              rail
              permanent
          >
            <v-list>
              <v-list-item
                  :prepend-avatar="userStore.avatarUrl"
                  :title="userStore.username"
                  :subtitle="userStore.email"
              ></v-list-item>
            </v-list>

            <v-divider></v-divider>

            <v-list density="compact" nav>
              <v-list-item
                  to="/home"
                  nav
                  value="home"
                  color="#1976D2"
                  link
              >
                <template #prepend>
                  <v-icon icon="fa-solid fa-house" color="#5FB3CE" />
                </template>
                <v-list-item-title class="drawer-text">Página Inicial</v-list-item-title>
              </v-list-item>

              <v-list-item :to="`/profile/${userStore.id}`" class="sub-item" link>
                <template #prepend>
                  <v-icon icon="fa-solid fa-user" color="#5FB3CE"/>
                </template>
                <v-list-item-title class="drawer-text">Perfil de Utilizador</v-list-item-title>
              </v-list-item>

              <v-list-item v-if="userStore.userType === 1" :to="`/subscribe/premium`" class="sub-item" link>
                <template #prepend>
                  <v-icon icon="fa-solid fa-star" color="#5FB3CE"/>
                </template>
                <v-list-item-title class="drawer-text">Subscrever Premium</v-list-item-title>
              </v-list-item>

              <v-list-item
                  :to="`/photoGallery/${userStore.id}`"
                  nav
                  value="galeria"
                  color="#1976D2"
                  link
              >
                <template #prepend>
                  <v-icon icon="fa-solid fa-image" color="#5FB3CE" />
                </template>
                <v-list-item-title class="drawer-text">A Minha Galeria</v-list-item-title>
              </v-list-item>

              <v-list-item
                  v-if="userStore.userType === 2"
                  to="/review"
                  nav
                  value="special"
                  color="#1976D2"
                  link
              >
                <template #prepend>
                  <v-icon icon="fa-solid fa-list-check" color="#5FB3CE" />
                </template>
                <v-list-item-title class="drawer-text">Review Pontos</v-list-item-title>
              </v-list-item>

            </v-list>



            <v-divider></v-divider>
            <v-list density="compact" nav>

              <v-list-item
                  @click="handleLogout"
              >
                <template #prepend>
                  <v-icon icon="fa-solid fa-right-from-bracket" color="#D32F2F" />
                </template>
                <v-list-item-title class="drawer-text">Terminar Sessão</v-list-item-title>
              </v-list-item>

            </v-list>

          </v-navigation-drawer>

        </v-col>


      </v-row>

    </v-container>

      <v-main style="height: 250px"></v-main>

  </v-toolbar>
  <GoBackButton/>
</template>

<style scoped>

.navbar {
  border: none;
  outline: none;
  display: flex;
  justify-content: space-between;
  background-color: #427F99;
  padding: 0.75rem 1.5rem;
  color: white;
}

.nav-button {
  background-color: white;
  color: #6e98db; /* same as your login button */
  border-radius: 8px;
  padding: 0.5rem 1rem;
  margin-left: 0.5rem;
  font-weight: bold;
  border: none;
  cursor: pointer;
  transition: background-color 0.2s ease;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.nav-button:hover {
  background-color: #e0e0e0;
}


.drawer-text {
  color: #427F99;
  font-weight: bold;
}


</style>


