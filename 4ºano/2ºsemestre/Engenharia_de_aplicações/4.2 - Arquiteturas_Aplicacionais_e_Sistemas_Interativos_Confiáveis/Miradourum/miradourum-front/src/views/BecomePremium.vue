<template>
  <div class="premium-page">
    <!-- Navbar -->
    <top-tool-bar-menu/>

    <!-- Premium Form -->
    <h1>Torne-se Premium</h1>


    <form class="premium-form" @submit.prevent="confirmarUpgrade">
      <v-card color="#e6f0f5">
        <div class="promo-message" style="font-size: 0.95rem; line-height: 1.5; text-align: justify; color: #427F99; margin: 0.5rem;">
          <p>
            Por apenas <strong>5€ mensais</strong>, torna-te um membro Premium e ganha a liberdade de partilhar fotos e deixar comentários sobre os locais que visitas, enriquecendo a comunidade com as tuas experiências.
          </p>
          <p>
            Aproveita todas as funcionalidades para tornar a tua experiência ainda mais completa e interativa!
          </p>
        </div>
      </v-card>

      <h2 style="text-align:center; color:white; margin-bottom: 1rem;">Dados de Pagamento</h2>
      <div class="form-group">
        <div class="input-wrapper">
          <label for="cardName">Nome do Titular</label>
          <input 
            type="text" 
            id="cardName" 
            v-model="cardName" 
            :class="{ 'error': cardNameError }"
            @input="clearCardNameError"
            required 
          />
          <FieldErrorPopup 
            :show="showCardNameError" 
            :message="cardNameError"
            @hide="hideCardNameError"
          />
        </div>
      </div>

      <div class="form-group">
        <div class="input-wrapper">
          <label for="cardNumber">Número do Cartão</label>
          <input 
            type="text" 
            id="cardNumber" 
            v-model="cardNumber" 
            :class="{ 'error': cardNumberError }"
            @input="clearCardNumberError"
            maxlength="19"
            placeholder="1234 5678 9012 3456"
            required 
          />
          <FieldErrorPopup 
            :show="showCardNumberError" 
            :message="cardNumberError"
            @hide="hideCardNumberError"
          />
        </div>
      </div>

      <div class="form-group">
        <div class="input-wrapper">
          <label for="expiry">Data de Validade</label>
          <input 
            type="text" 
            id="expiry" 
            v-model="expiry" 
            :class="{ 'error': expiryError }"
            @input="clearExpiryError"
            placeholder="MM/AA" 
            maxlength="5"
            required 
          />
          <FieldErrorPopup 
            :show="showExpiryError" 
            :message="expiryError"
            @hide="hideExpiryError"
          />
        </div>
      </div>

      <div class="form-group">
        <div class="input-wrapper">
          <label for="cvc">CVC</label>
          <input 
            type="text" 
            id="cvc" 
            v-model="ccv" 
            :class="{ 'error': ccvError }"
            @input="clearCcvError"
            maxlength="4"
            placeholder="123"
            required 
          />
          <FieldErrorPopup 
            :show="showCcvError" 
            :message="ccvError"
            @hide="hideCcvError"
          />
        </div>
      </div>

      <button class="confirm-button" type="submit">Confirmar</button>
    </form>

    <SuccessPopup 
      v-if="showSuccess" 
      :text="successMessage" 
      @close="showSuccess = false" 
    />

    <ErrorPopup v-if="errorMessage" :text="errorMessage" />

  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import LogoButton from '@/components/LogoButton.vue'
import {UserStore} from "@/store/userStore.js";
const API_BASE_URL = import.meta.env.VITE_API_URL;
import TopToolBarMenu from "../components/TopToolBarMenu.vue";
import SuccessPopup from "@/components/SuccessPopup.vue" 
import ErrorPopup from '@/components/ErrorPopup.vue'
import FieldErrorPopup from '@/components/FieldErrorPopup.vue'

const showSuccess = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

const router = useRouter()
const cardName = ref('')
const cardNumber = ref('')
const expiry = ref('')
const ccv = ref('')
const userStore = UserStore()

// Estados dos erros de validação
const cardNameError = ref('')
const cardNumberError = ref('')
const expiryError = ref('')
const ccvError = ref('')

// Estados de visibilidade dos popups
const showCardNameError = ref(false)
const showCardNumberError = ref(false)
const showExpiryError = ref(false)
const showCcvError = ref(false)

const token = userStore.authToken

// Funções para limpar erros quando utilizador digita/interage
const clearCardNameError = () => {
  if (cardNameError.value) {
    cardNameError.value = ''
    showCardNameError.value = false
  }
}

const clearCardNumberError = () => {
  if (cardNumberError.value) {
    cardNumberError.value = ''
    showCardNumberError.value = false
  }
  // Formatação automática do número do cartão
  cardNumber.value = cardNumber.value.replace(/\D/g, '').replace(/(\d{4})(?=\d)/g, '$1 ')
}

const clearExpiryError = () => {
  if (expiryError.value) {
    expiryError.value = ''
    showExpiryError.value = false
  }
  // Formatação automática da data MM/AA
  expiry.value = expiry.value.replace(/\D/g, '').replace(/(\d{2})(\d)/, '$1/$2')
}

const clearCcvError = () => {
  if (ccvError.value) {
    ccvError.value = ''
    showCcvError.value = false
  }
  // Permitir apenas números
  ccv.value = ccv.value.replace(/\D/g, '')
}

// Funções para esconder popups manualmente
const hideCardNameError = () => {
  showCardNameError.value = false
}

const hideCardNumberError = () => {
  showCardNumberError.value = false
}

const hideExpiryError = () => {
  showExpiryError.value = false
}

const hideCcvError = () => {
  showCcvError.value = false
}

// Validações específicas
const isValidCardNumber = (number) => {
  const cleanNumber = number.replace(/\s/g, '')
  return /^\d{13,19}$/.test(cleanNumber) // Cartões têm entre 13-19 dígitos
}

const isValidExpiry = (expiry) => {
  const regex = /^(0[1-9]|1[0-2])\/\d{2}$/
  if (!regex.test(expiry)) return false
  
  const [month, year] = expiry.split('/')
  const currentDate = new Date()
  const currentYear = currentDate.getFullYear() % 100
  const currentMonth = currentDate.getMonth() + 1
  
  const cardYear = parseInt(year)
  const cardMonth = parseInt(month)
  
  // Verificar se não está expirado
  if (cardYear < currentYear || (cardYear === currentYear && cardMonth < currentMonth)) {
    return false
  }
  
  return true
}

const isValidCCV = (ccv) => {
  return /^\d{3,4}$/.test(ccv)
}

// Função de validação do formulário
const validateForm = () => {
  // Limpar todos os erros
  cardNameError.value = ''
  cardNumberError.value = ''
  expiryError.value = ''
  ccvError.value = ''
  showCardNameError.value = false
  showCardNumberError.value = false
  showExpiryError.value = false
  showCcvError.value = false
  
  let isValid = true
  
  // Validar nome do titular
  if (!cardName.value.trim()) {
    cardNameError.value = 'Nome do titular é obrigatório'
    showCardNameError.value = true
    isValid = false
  } else if (cardName.value.trim().length < 2) {
    cardNameError.value = 'Nome deve ter pelo menos 2 caracteres'
    showCardNameError.value = true
    isValid = false
  }
  
  // Validar número do cartão
  if (!cardNumber.value.trim()) {
    cardNumberError.value = 'Número do cartão é obrigatório'
    showCardNumberError.value = true
    isValid = false
  } else if (!isValidCardNumber(cardNumber.value)) {
    cardNumberError.value = 'Número do cartão deve ter entre 13-16 dígitos'
    showCardNumberError.value = true
    isValid = false
  }
  
  // Validar data de validade
  if (!expiry.value.trim()) {
    expiryError.value = 'Data de validade é obrigatória'
    showExpiryError.value = true
    isValid = false
  } else if (!isValidExpiry(expiry.value)) {
    expiryError.value = 'Data de validade inválida ou expirada'
    showExpiryError.value = true
    isValid = false
  }
  
  // Validar CCV
  if (!ccv.value.trim()) {
    ccvError.value = 'CVC é obrigatório'
    showCcvError.value = true
    isValid = false
  } else if (!isValidCCV(ccv.value)) {
    ccvError.value = 'CVC deve ter 3 ou 4 dígitos'
    showCcvError.value = true
    isValid = false
  }
  
  return isValid
}

function showError(msg) {
  errorMessage.value = msg
  successMessage.value = ''
  setTimeout(() => {
    errorMessage.value = ''
  }, 3000)
}

const goBack = () => {
  router.push('/editProfile')
}

const handleLogout = () => {
  localStorage.removeItem('authToken')
  router.push('/login')
}

const confirmarUpgrade = async () => {
  errorMessage.value = ''
  successMessage.value = ''

  if (!validateForm()) return

  try {
    const response = await fetch(`${API_BASE_URL}/user/upgrade-premium`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        cardName: cardName.value,
        cardNumber: cardNumber.value.replace(/\s/g, ''), // Remover espaços
        ccv: ccv.value,
        expiry: expiry.value,
      })
    })

    if (response.ok) {
      userStore.userType = 3
      successMessage.value = 'Upgrade Premium realizado com sucesso!'
      showSuccess.value = true
      setTimeout(() => {
        router.push('/home')
      }, 2000)
    } else {
      const errorText = await response.text()
      showError(errorText || 'Erro ao fazer upgrade.')
    }
  } catch (err) {
    console.error('Erro ao fazer upgrade:', err)
    showError('Erro de rede.')
  }
}
</script>

<style scoped>
.input-wrapper {
  position: relative;
  width: 100%;
}

.premium-page {
  background-color: white;
  min-height: 100vh;
  color: white;
  font-family: sans-serif;
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
  flex-direction: column;
  align-items: flex-start;
  gap: 0.5rem;
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 1rem;
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

h1 {
  text-align: center;
  margin: 2rem 0 1rem;
  color: #427F99;
}

.premium-form {
  max-width: 500px;
  margin: 0 auto;
  background-color: #427F99;
  padding: 2rem;
  border-radius: 16px;
  color: white;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.form-group {
  text-align: left;
}

label {
  display: block;
  font-weight: bold;
  margin-bottom: 0.5rem;
}

input {
  width: 100%;
  padding: 0.75rem;
  border-radius: 8px;
  border: 2px solid transparent;
  font-size: 1rem;
  background-color: white;
  color: black;
  box-sizing: border-box;
  transition: all 0.2s ease;
}

input.error {
  border-color: #ef4444;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
}

.confirm-button {
  background-color: #3cb371;
  color: white;
  padding: 0.75rem;
  border: none;
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
}

.confirm-button:hover {
  background-color: #2e8b57;
}

@media (max-width: 768px) {
  .navbar {
    flex-direction: column;
    text-align: center;
  }

  .nav-button {
    margin: 0.5rem auto;
  }

  .premium-form {
    width: 90%;
    margin: 1rem auto;
  }
}
</style>