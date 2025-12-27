<template>
    <Transition name="error-popup">
      <div v-if="show && message" class="field-error-popup">
        <div class="error-content">
          <svg class="error-icon" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
          </svg>
          <span class="error-text">{{ message }}</span>
        </div>
        <div class="error-arrow"></div>
      </div>
    </Transition>
  </template>
  
  <script setup>
  import { watch, onMounted } from 'vue'
  
  const props = defineProps({
    show: {
      type: Boolean,
      default: false
    },
    message: {
      type: String,
      default: ''
    },
    autoHide: {
      type: Boolean,
      default: true
    },
    duration: {
      type: Number,
      default: 4000 // 4 segundos
    }
  })
  
  const emit = defineEmits(['hide'])
  
  let hideTimer = null
  
  // Função para esconder o popup
  const hidePopup = () => {
    emit('hide')
  }
  
  // Auto-hide após duração especificada
  watch(() => props.show, (newShow) => {
    if (newShow && props.autoHide) {
      // Limpar timer anterior se existir
      if (hideTimer) {
        clearTimeout(hideTimer)
      }
      
      // Definir novo timer
      hideTimer = setTimeout(() => {
        hidePopup()
      }, props.duration)
    } else if (!newShow && hideTimer) {
      // Limpar timer se popup foi escondido manualmente
      clearTimeout(hideTimer)
      hideTimer = null
    }
  })
  
  // Limpar timer quando componente for desmontado
  onMounted(() => {
    return () => {
      if (hideTimer) {
        clearTimeout(hideTimer)
      }
    }
  })
  </script>
  
  <style scoped>
  .field-error-popup {
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    z-index: 1000;
    margin-top: 8px;
  }
  
  .error-content {
    background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
    color: white;
    padding: 12px 16px;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    font-weight: 500;
    border: 1px solid rgba(255, 255, 255, 0.2);
  }
  
  .error-icon {
    width: 18px;
    height: 18px;
    flex-shrink: 0;
  }
  
  .error-text {
    flex: 1;
  }
  
  .error-arrow {
    position: absolute;
    top: -6px;
    left: 20px;
    width: 0;
    height: 0;
    border-left: 6px solid transparent;
    border-right: 6px solid transparent;
    border-bottom: 6px solid #ef4444;
  }
  
  /* Animações */
  .error-popup-enter-active {
    transition: all 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55);
  }
  
  .error-popup-leave-active {
    transition: all 0.2s ease-in;
  }
  
  .error-popup-enter-from {
    opacity: 0;
    transform: translateY(-10px) scale(0.9);
  }
  
  .error-popup-leave-to {
    opacity: 0;
    transform: translateY(-5px) scale(0.95);
  }
  
  .error-popup-enter-to,
  .error-popup-leave-from {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
  
  /* Efeito de pulso sutil */
  @keyframes subtle-pulse {
    0%, 100% {
      box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
    }
    50% {
      box-shadow: 0 4px 16px rgba(239, 68, 68, 0.4);
    }
  }
  
  .error-content {
    animation: subtle-pulse 2s ease-in-out infinite;
  }
  </style>