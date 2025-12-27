<script setup>
import { useRouter } from 'vue-router'
import { defineProps } from 'vue'
import logo from '@/assets/logo.png'

const props = defineProps({
  to: {
    type: String,
    required: true
  }
})

const router = useRouter()
const go = () => {
  router.push(props.to)
}
</script>

<style scoped>
.logo-button {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0 0 0 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.logo-button img {
  height: 40px; /* or whatever size you want */
  width: auto;
  display: block;
}
</style>


<template>
  <button @click="go" class="logo-button">
    <img :src="logo" alt="MiradourUM Logo" />
  </button>
</template>