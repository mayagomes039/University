<script setup>

import router from "@/router/index.js";
import {computed} from "vue";
import {useRoute} from "vue-router";

const route = useRoute();
const showBackButton = computed(() => route.path !== '/home');

</script>



<template>
  <v-container fluid>
    <v-btn
        v-if="showBackButton"
        color="#427F99"
        @click="router.back()"
        prepend-icon="fa-solid fa-arrow-left"
        class="font-weight-bold"
        variant="text">
      Voltar atrás
    </v-btn>
  </v-container>


</template>

<style scoped>

</style>