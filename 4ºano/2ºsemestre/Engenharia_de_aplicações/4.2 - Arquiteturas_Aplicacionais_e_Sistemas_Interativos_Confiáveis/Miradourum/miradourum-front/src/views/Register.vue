<template>
  <div class="home-page">
    <nav class="navbar">
      <div class="navbar-left">
        <LogoButton to="/" />
      </div>
      <div class="navbar-right"></div>
    </nav>

    <div class="hero">
      <img class="hero-image" src="@/assets/baloicoog.jpg" alt="Miradouro" />
      <div class="hero-text">
        <h1><strong>MiradourUM</strong></h1>
        <p>Cria a tua conta e junta-te à comunidade de exploradores!</p>
      </div>

      <SuccessPopup v-if="successMessage" :text="successMessage" />
      <ErrorPopup v-if="errorMessage" :text="errorMessage" />

      <div class="login-popup">
        <h2>Registar</h2>
        <form @submit.prevent="handleRegister" novalidate>
          <div class="input-group">
            <div class="input-wrapper">
              <input 
                type="text" 
                v-model="username" 
                placeholder="Username" 
                :class="{ 'error': usernameError }"
                @input="clearUsernameError"
              />
              <FieldErrorPopup 
                :show="showUsernameError" 
                :message="usernameError"
                @hide="hideUsernameError"
              />
            </div>
          </div>

          <div class="input-group">
            <div class="input-wrapper">
              <input 
                type="email" 
                v-model="email" 
                placeholder="Email" 
                :class="{ 'error': emailError }"
                @input="clearEmailError"
              />
              <FieldErrorPopup 
                :show="showEmailError" 
                :message="emailError"
                @hide="hideEmailError"
              />
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

          <div class="input-group">
            <div class="input-wrapper">
              <label class="file-upload-label" :class="{ 'error': imageError }">
                Escolher imagem de perfil
                <input type="file" accept="image/*" @change="handleImageUpload" />
              </label>
              <FieldErrorPopup 
                :show="showImageError" 
                :message="imageError"
                @hide="hideImageError"
              />
            </div>
          </div>

          <div v-if="imagePreview" class="preview">
            <img :src="imagePreview" alt="Preview" />
          </div>

          <button type="submit">Criar Conta</button>
        </form>
        <p class="register-text">
          Já tem conta?
          <RouterLink to="/login">Inicie Sessão</RouterLink>
        </p>
      </div>

      <img class="corner-image" src="@/assets/hand.png" alt="Decoration" />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import LogoButton from '@/components/LogoButton.vue'
import ErrorPopup from '@/components/ErrorPopup.vue'
import SuccessPopup from '@/components/SuccessPopup.vue'
import FieldErrorPopup from '@/components/FieldErrorPopup.vue'
import { S3Client } from '@aws-sdk/client-s3'
import { Upload } from '@aws-sdk/lib-storage'

const email = ref('')
const password = ref('')
const username = ref('')
const errorMessage = ref('')
const successMessage = ref('')
const imageFile = ref(null)
const imagePreview = ref('')

// Estados dos erros de validação
const usernameError = ref('')
const emailError = ref('')
const passwordError = ref('')
const imageError = ref('')

// Estados de visibilidade dos popups
const showUsernameError = ref(false)
const showEmailError = ref(false)
const showPasswordError = ref(false)
const showImageError = ref(false)
const API_BASE_URL = import.meta.env.VITE_API_URL;
const Minio_URL = import.meta.env.VITE_MINIO_URL;
const router = useRouter()


const handleImageUpload = (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  // Limpar erro de imagem quando utilizador seleciona uma
  clearImageError()
  
  imageFile.value = file
  imagePreview.value = URL.createObjectURL(file)
}



// MinIO client setup
const s3Client = new S3Client({
  region: 'us-east-1', // Can be anything
  endpoint: `${Minio_URL}`,
  credentials: {
    accessKeyId: 'admin',
    secretAccessKey: 'admin123',
  },
  forcePathStyle: true, // Required for MinIO
})
const uploadImageToMinIO = async (file) => {
  const fileName = `profile-${Date.now()}-${file.name}`

  const upload = new Upload({
    client: s3Client,
    params: {
      Bucket: 'profile-images',
      Key: fileName,
      Body: file,
      ContentType: file.type,
      ACL: 'public-read',
    },
  })

  const result = await upload.done()
  return `${Minio_URL}/profile-images/${fileName}` // Public URL
}


// Funções para limpar erros quando utilizador digita/interage
const clearUsernameError = () => {
  if (usernameError.value) {
    usernameError.value = ''
    showUsernameError.value = false
  }
}

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

const clearImageError = () => {
  if (imageError.value) {
    imageError.value = ''
    showImageError.value = false
  }
}

// Funções para esconder popups manualmente
const hideUsernameError = () => {
  showUsernameError.value = false
}

const hideEmailError = () => {
  showEmailError.value = false
}

const hidePasswordError = () => {
  showPasswordError.value = false
}

const hideImageError = () => {
  showImageError.value = false
}

// Validação de email
const isValidEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

// Função de validação do formulário
const validateForm = () => {
  // Limpar todos os erros
  usernameError.value = ''
  emailError.value = ''
  passwordError.value = ''
  imageError.value = ''
  showUsernameError.value = false
  showEmailError.value = false
  showPasswordError.value = false
  showImageError.value = false
  
  let isValid = true
  
  // Validar username
  if (!username.value.trim()) {
    usernameError.value = 'Username é obrigatório'
    showUsernameError.value = true
    isValid = false
  } else if (username.value.trim().length < 3) {
    usernameError.value = 'Username deve ter pelo menos 3 caracteres'
    showUsernameError.value = true
    isValid = false
  }
  
  // Validar email
  if (!email.value.trim()) {
    emailError.value = 'Email é obrigatório'
    showEmailError.value = true
    isValid = false
  } else if (!isValidEmail(email.value)) {
    emailError.value = 'Email deve ser válido'
    showEmailError.value = true
    isValid = false
  }
  
  // Validar password
  if (!password.value.trim()) {
    passwordError.value = 'Password é obrigatória'
    showPasswordError.value = true
    isValid = false
  } else if (password.value.length < 6) {
    passwordError.value = 'Password deve ter pelo menos 6 caracteres'
    showPasswordError.value = true
    isValid = false
  }
  
  // Validar imagem
  if (!imageFile.value) {
    imageError.value = 'Imagem de perfil é obrigatória'
    showImageError.value = true
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

function showSuccess(msg) {
  successMessage.value = msg
  errorMessage.value = ''
  setTimeout(() => {
    successMessage.value = ''
  }, 3000)
}

const handleRegister = async () => {
  errorMessage.value = ''
  successMessage.value = ''

  if (!validateForm()) return

  try {
    // 1. Upload image to MinIO
    const uploadedImageUrl = await uploadImageToMinIO(imageFile.value)

    // 2. Prepare payload for backend
    const payload = {
      email: email.value,
      password: password.value,
      username: username.value,
      profileimage: uploadedImageUrl, // <- URL to MinIO
    }

    const response = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })

    if (!response.ok) {
      const errorText = await response.text()
      showError(errorText || 'Erro ao criar conta')
      return
    }

    showSuccess('Conta criada com sucesso! A redirecionar para o login...')
    setTimeout(() => {
      router.push('/login')
    }, 1000)
  } catch (err) {
    console.error('Erro:', err)
    showError('Erro ao carregar imagem ou criar conta.')
  }
}

</script>

<style scoped>
.input-wrapper {
  position: relative;
  width: 100%;
}

.input-group {
  margin-bottom: 20px;
}

.file-upload-label {
  display: block;
  margin-top: 1rem;
  background-color: white;
  color: #427F99;
  padding: 0.75rem;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
  text-align: center;
  border: 2px solid transparent;
  transition: all 0.2s ease;
  width: 100%;
  box-sizing: border-box;
}

.file-upload-label:hover {
  background-color: #f8f9fa;
}

.file-upload-label.error {
  border-color: #ef4444;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
}

.file-upload-label input {
  display: none;
}

.preview img {
  margin-top: 1rem;
  max-width: 100px;
  border-radius: 8px;
  border: 2px solid white;
}

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
  border: none;
  outline: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  white-space: nowrap;
  background-color: white;
  color: #427F99;
  border-radius: 8px;
  padding: 0.5rem 1rem;
  margin-left: 0.5rem;
  text-decoration: none;
  font-weight: bold;
  transition: background-color 0.2s ease;
}


.nav-button:hover {
  border: none;
  outline: none;
  background-color: #e0e0e0;
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
  max-width: 600px;
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
  z-index: 100;
  text-align: center;
}

.login-popup h2 {
  margin-bottom: 1rem;
}

.login-popup input {
  width: 100%;
  padding: 0.75rem;
  margin: 0;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  box-sizing: border-box;
  border: 2px solid transparent;
  transition: all 0.2s ease;
  background-color: white; 
  color: black;             
}


.login-popup input.error {
  border-color: #ef4444;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
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
    width: 150px;
  }
}
</style>