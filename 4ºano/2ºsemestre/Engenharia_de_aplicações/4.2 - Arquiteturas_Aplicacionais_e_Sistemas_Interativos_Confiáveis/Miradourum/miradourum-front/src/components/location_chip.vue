<script setup>
import { computed } from 'vue'
import {UserStore} from "@/store/userStore.js";

const userStore = UserStore()

// const props = defineProps({
//   location: {
//     type: Object,
//     required: false,
//     default: () => null,
//   }
// })

const formattedLocation = computed(() => {
  if (!userStore.location) return 'Localização Indisponível'
  const { lat, lng } = userStore.location
  return `Lat: ${lat.toFixed(4)} Long:${lng.toFixed(4)}`
})
</script>

<template>
  <v-chip
      class="mr-4"
      base-color="#F8F9FA"
      color="#F8F9FA"
      text-color="#427F99"
      style="font-weight: bold;"
      size="large"
  >
    <template #prepend>
      <v-icon icon="fa-solid fa-location-dot" color="#E34234" style="padding-right: 10px;" />
    </template>
    {{ formattedLocation }}
  </v-chip>
</template>

<style scoped>

</style>