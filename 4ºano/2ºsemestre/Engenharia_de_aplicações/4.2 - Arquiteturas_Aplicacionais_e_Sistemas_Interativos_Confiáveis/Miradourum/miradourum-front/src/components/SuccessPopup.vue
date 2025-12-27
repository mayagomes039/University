<template>
  <div class="error-popup">
    {{ text }}
  </div>
</template>

<script setup>
defineProps({
  text: {
    type: String,
    required: true
  }
})
</script>

<style scoped>
.error-popup {
  position: fixed;
  top: 20px;
  right: 20px;
  background-color: #2ecc71;
  color: white;
  padding: 1rem 1.5rem;
  border-radius: 8px;
  font-weight: bold;
  z-index: 9999;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  animation: fadeInSlide 0.3s ease-out;
}

@keyframes fadeInSlide {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}
</style>
