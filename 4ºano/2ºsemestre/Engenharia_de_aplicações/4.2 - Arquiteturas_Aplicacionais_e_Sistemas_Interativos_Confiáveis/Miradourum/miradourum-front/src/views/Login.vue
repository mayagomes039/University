<template>
  <div class="home-page">
    <nav class="navbar">
      <div class="navbar-left">
        <LogoButton to="/" />
      </div>
      <div class="navbar-right">
        
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

      <SuccessPopup v-if="successMessage" :text="successMessage" /> <!-- mostra se successMessage não estiver vazio-->
      <ErrorPopup v-if="errorMessage" :text="errorMessage" /> <!-- mostra se errorMessage não estiver vazio-->

      <!-- Login Popup -->
      <div class="login-popup">
        <h2>Iniciar Sessão</h2>
        <form @submit.prevent="handleLogin" novalidate> <!-- invoca handleLogin quando se faz submit, novalidate para não validar com HTML5-->
          <div class="input-group">
            <div class="input-wrapper">
              <input 
                type="email" 
                v-model="email"
                placeholder="Email" 
                :class="{ 'error': emailError }"
                @input="clearEmailError"
              /> <!-- v-model liga o input à variável email, 
                      quando o user insere algo, apaga o popup de erro, se estiver ativo invocando a clearEmailError-->
              <FieldErrorPopup :show="showEmailError" :message="emailError" @hide="hideEmailError"/> <!-- controla se o popup está ativo-->
            </div>
          </div>
          
          <div class="input-group">
            <div class="input-wrapper">
              <input 
                type="password" 
                v-model="password" 
                placeholder="Password" 
                :class="{ 'error': passwordError }"
                @input="clearPasswordError"
              />
              <FieldErrorPopup 
                :show="showPasswordError" 
                :message="passwordError"
                @hide="hidePasswordError"
              />
            </div>
          </div>
          
          <button type="submit">Entrar</button>
        </form>
        <p class="register-text">
          Ainda não tem conta?
          <RouterLink to="/register">Registe-se</RouterLink>
        </p>
        
       <p class="forgot-password" @click="showResetForm = !showResetForm" style="cursor:pointer; text-decoration: underline; color: #ffffff;">
  Esqueceu a palavra-passe?
</p>

<div v-if="showResetForm" class="reset-password-form" style="margin-top: 1rem; text-align: left;">
  <h3>Redefinir Password</h3>
  <div class="input-group">
    <input 
      type="email" 
      v-model="resetEmail" 
      placeholder="Email" 
      :class="{ 'error': resetEmailError }" 
      @input="resetEmailError = ''"
    />
    <small v-if="resetEmailError" style="color: #ef4444;">{{ resetEmailError }}</small>
  </div>
  <div class="input-group">
    <input 
      type="password" 
      v-model="resetPassword" 
      placeholder="Nova Password" 
      :class="{ 'error': resetPasswordError }" 
      @input="resetPasswordError = ''"
    />
    <small v-if="resetPasswordError" style="color: #ef4444;">{{ resetPasswordError }}</small>
  </div>
  <button @click="handlePasswordReset" style="width: 100%; padding: 0.75rem; border-radius: 8px; font-weight: bold; cursor: pointer; background-color: white; color: #427F99; border: none;">
    Redefinir Password
  </button>
</div>

      </div>

      <img class="corner-image" src="@/assets/hand.png" alt="Decoration" />

    </div>

  </div>
</template>

<script setup>
import {ref} from 'vue'
import {useRouter} from 'vue-router'
import LogoButton from '@/components/LogoButton.vue'
import ErrorPopup from '@/components/ErrorPopup.vue'
import SuccessPopup from '@/components/SuccessPopup.vue'
import FieldErrorPopup from '@/components/FieldErrorPopup.vue'
import {UserStore} from '@/store/userStore'

const userStore = UserStore()

const showResetForm = ref(false)
const resetEmail = ref('')
const resetPassword = ref('')
const resetEmailError = ref('')
const resetPasswordError = ref('')
const clearResetErrors = () => {
  resetEmailError.value = ''
  resetPasswordError.value = ''
}

const validateResetForm = () => {
  resetEmailError.value = ''
  resetPasswordError.value = ''

  let valid = true

  if (!resetEmail.value.trim()) {
    resetEmailError.value = 'Email é obrigatório'
    valid = false
  } else if (!isValidEmail(resetEmail.value)) {
    resetEmailError.value = 'Email deve ser válido'
    valid = false
  }

  if (!resetPassword.value.trim()) {
    resetPasswordError.value = 'Password é obrigatória'
    valid = false
  } else if (resetPassword.value.length < 6) {
    resetPasswordError.value = 'Password deve ter pelo menos 6 caracteres'
    valid = false
  }

  return valid
}

const handlePasswordReset = async () => {
  clearResetErrors()
  if (!validateResetForm()) return

  try {
    const response = await fetch(`${API_BASE_URL}/user/resetpassword`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        email: resetEmail.value,
        newPassword: resetPassword.value,
      }),
    })

    if (!response.ok) {
      // Try to get error message from backend
      const contentType = response.headers.get('content-type')
      let errorMsg = 'Erro ao tentar redefinir a password.'

      if (contentType && contentType.includes('application/json')) {
        const errorData = await response.json()
        errorMsg = errorData.message || errorData.error || errorMsg
      } else {
        const errorText = await response.text()
        if (errorText) {
          errorMsg = errorText
        }
      }

      showError(errorMsg)
      return
    }

    // Success
    showSuccess('Password redefinida com sucesso! Pode agora iniciar sessão.')
    showResetForm.value = false
    resetEmail.value = ''
    resetPassword.value = ''

  } catch (error) {
    showError('Erro de rede. Tente novamente.')
    console.error(error)
  }
}


const router = useRouter()

// Variáveis para armazenar, validar e enviar os campos inseridos pelo utilizador
const email = ref('')
const password = ref('')
// Variáveis para armazenar mensagens de erro e sucesso, '' significa que não há mensagens
const errorMessage = ref('')
const successMessage = ref('')
// Variáveis para armazenar erros específicos de email e password (para mostrar popups específicos)
const emailError = ref('') 
const passwordError = ref('')
// Estados para controlar a visibilidade dos popups de erro
const showEmailError = ref(false)
const showPasswordError = ref(false)
const API_BASE_URL = import.meta.env.VITE_API_URL;

// Funções para limpar os erros quando o utilizador começa a digitar, deixando de mostrar os
const clearEmailError = () => {
  if (emailError.value) {
    emailError.value = ''
    showEmailError.value = false
  }
}
const clearPasswordError = () => {
  if (passwordError.value) {
    passwordError.value = ''
    showPasswordError.value = false
  }
}

// Funções para esconder popups manualmente, necessárias para respeitar o evento @hide emitido pelo componente quando o timeout passa
const hideEmailError = () => {
  showEmailError.value = false
}
const hidePasswordError = () => {
  showPasswordError.value = false
}

// Função de validação, adiciona os erros se houver para que sejam demonstrados ao utilizador
const validateForm = () => {
  emailError.value = ''
  passwordError.value = ''
  showEmailError.value = false
  showPasswordError.value = false
  
  let isValid = true
  
  if (!email.value.trim()) {
    emailError.value = 'Email é obrigatório'
    showEmailError.value = true
    isValid = false
  } else if (!isValidEmail(email.value)) {
    emailError.value = 'Email deve ser válido'
    showEmailError.value = true
    isValid = false
  }
  
  if (!password.value.trim()) {
    passwordError.value = 'Password é obrigatória'
    showPasswordError.value = true
    isValid = false
  } else if (password.value.length < 6) {
    passwordError.value = 'Password deve ter pelo menos 6 caracteres'
    showPasswordError.value = true
    isValid = false
  }
  
  return isValid
}

// Validação de email
const isValidEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

// Ativa o popup de erro no canto esquerdo
function showError(msg) {
  errorMessage.value = msg
  successMessage.value = '' // Limpar mensagem de sucesso
  setTimeout(() => {
    errorMessage.value = ''
  }, 3000)
}

// Ativa o popup de sucesso no canto esquerdo
function showSuccess(msg) {
  successMessage.value = msg
  errorMessage.value = '' // Limpar mensagem de erro
  setTimeout(() => {
    successMessage.value = ''
  }, 3000)
}

// Invocada quando o utilizador clica no botão "Entrar" que submete o formulário
const handleLogin = async () => {
  // Limpar todas as mensagens
  errorMessage.value = ''
  successMessage.value = '' 
  emailError.value = ''
  passwordError.value = ''
  showEmailError.value = false
  showPasswordError.value = false
  
  // Validar formulário
  if (!validateForm()) {
    return
  }
  
  try {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        email: email.value,
        password: password.value,
      }),
    })

    if (response.status === 401) {
      showError('Email ou palavra-passe incorretos.')
      return
    }

    // Verificar se a resposta não é OK (inclui 400, 422, etc.)
    if (!response.ok) {
      // Tentar obter a mensagem de erro do backend
      const contentType = response.headers.get('content-type')
      let errorMsg = 'Erro ao tentar iniciar sessão.'
      
      if (contentType && contentType.includes('application/json')) {
        const errorData = await response.json()
        // Adaptar conforme a estrutura de erro do seu backend
        errorMsg = errorData.message || errorData.error || 'Erro ao tentar iniciar sessão.'
      } else {
        const errorText = await response.text()
        if (errorText) {
          errorMsg = errorText
        }
      }
      
      showError(errorMsg)
      return
    }

    // Login bem-sucedido!
    const token = await response.text()
    // localStorage.setItem('authToken', token)

    userStore.setToken(token)

    const userData = await fetchUserProfile()
    console.log(userData)
    userStore.setUserData(userData)

    // Mostrar mensagem de sucesso
    showSuccess('Login realizado com sucesso! A redirecionar...')
    
    // Pequeno delay antes de redirecionar para o utilizador ver a mensagem
    setTimeout(() => {
      router.push('/home')
    }, 1500)

  } catch (error) {
    showError('Erro de rede. Tente novamente.')
    console.error(error)
  }
}

const fetchUserProfile = async () => {
  if (!userStore.authToken) {
    router.push('/login')
    return
  }

  try {
    const response = await fetch(`${API_BASE_URL}/user`, {
      headers: {
        'Authorization': `Bearer ${userStore.authToken}`
      }
    })

    if (!response.ok) {
      // Se token inválido ou outro erro, redireciona para login
      router.push('/login')
      return
    }

    const data = await response.json()
    console.log(data.email)
    return {
      'username': data.username,
      'avatarUrl': data.profileImage || '/default-profile.png',
      'email': data.email,
      'userType': data.role,
      'id' : data.id
    }

  } catch (error) {
    console.error('Erro a buscar perfil:', error)
    router.push('/login')
  }
}


</script>

<style scoped>

.forgot-password {
  margin-top: 1rem;
}

.forgot-password a {
  color: #ffffff;
  text-decoration: underline;
  font-size: 0.9rem;
}


.home-page {
  background-color: white;
  min-height: 100vh;
  color: white;
  font-family: sans-serif;
  position: relative;
}

.navbar {
  display: flex;
  justify-content: space-between;
  background-color: #427F99;
  padding: 0.75rem 1.5rem;
  color: white;
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

.login-popup {
  position: absolute;
  top: 40%;
  right: 5%;
  transform: translateY(-50%);
  background-color: #427F99;
  color: white;
  padding: 2rem;
  border-radius: 12px;
  width: 400px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
  z-index: 100; /* Higher than corner image */
  text-align: center;
}

.login-popup h2 {
  margin-bottom: 1rem;
}

.login-popup input {
  width: 100%;
  padding: 0.75rem;
  margin: 0.5rem 0;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  background-color: white; /* Add this */
  color: black;            /* Ensures text inside is visible */
}


.login-popup button {
  width: 100%;
  padding: 0.75rem;
  background-color: white;
  color: #427F99;
  border: none;
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
  margin-top: 0.5rem;
}

.login-popup button:hover {
  background-color: #f0f0f0;
}

.register-text {
  font-size: 0.85rem;
  margin-top: 1rem;
}

.register-text a {
  color: #fff;
  text-decoration: underline;
}

/* 📱 Mobile tweaks */
@media (max-width: 768px) {
  .login-popup {
    position: static;
    transform: none;
    width: 90%;
    margin: 2rem auto;
  }



  .navbar {
    flex-direction: column;
    text-align: center;
  }

  .navbar-left,
  .navbar-right {
    width: 100%;
    margin-bottom: 0.5rem;
    display: flex;
    justify-content: center;
    flex-direction: column;
    gap: 0.5rem;
  }

  .nav-button {
    width: 60%;
    margin: 0 auto;
  }
}
.corner-image {
  position: fixed;
  bottom: 0%;
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


/* FieldErrorPopup styles */
.input-wrapper {
  position: relative;
  width: 100%;
}

.input-group {
  margin-bottom: 20px; /* Aumentar espaço para acomodar o popup */
}

/* Remover os estilos antigos dos erros se existirem */
.error-message {
  display: none; /* Esconder as mensagens antigas */
}

/* Opcional: Melhorar o estilo do input com erro */
input.error {
  border-color: #ef4444;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
}
</style>
