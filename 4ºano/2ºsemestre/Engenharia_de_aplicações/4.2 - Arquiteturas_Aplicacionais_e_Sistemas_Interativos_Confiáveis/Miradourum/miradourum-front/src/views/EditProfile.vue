<template>
  <div class="home-page">
    <!-- Navbar -->
    <top-tool-bar-menu/>

    <SuccessPopup v-if="successMessage" :text="successMessage" /> <!-- mostra se successMessage não estiver vazio-->
    <ErrorPopup v-if="errorMessage" :text="errorMessage" /> <!-- mostra se errorMessage não estiver vazio-->


    <!-- Profile Content -->
    <div class="profile-container">
      <div class="profile-picture-wrapper">
        <img class="profile-picture" :src="profileImage" alt="Foto de Perfil" />
        <button class="change-photo-button" @click="alterarFoto">Alterar Foto de Utilizador</button>

        <!-- Pré-visualização da nova imagem -->
        <div v-if="imagePreview" class="mini-preview">
          <img :src="imagePreview" alt="Pré-visualização" />
        </div>
      </div>

      <h2 class="username-display">{{ username }}</h2>

      <div class="form-group">
        <label for="username">Nome de Utilizador</label>
        <input v-model="username" id="username" type="text" />
      </div>

      <div class="buttons">
        <button class="save-button" @click="guardarPerfil">Guardar</button>
      </div>

      <div class="form-group">
        <label for="password">Nova Palavra Passe (opcional)</label>
        <div class="input-wrapper" style="position: relative;">
          <input
            v-model="novaPassword"
            id="password"
            type="password"
            placeholder="Mínimo 6 caracteres"
            :class="{ error: passwordError }"
            @input="clearPasswordError"
          />
          <FieldErrorPopup
            :show="showPasswordError"
            :message="passwordError"
            @hide="() => showPasswordError = false"
          />
        </div>
      </div>



      <div class="buttons">
        <button class="nav-button" @click="alterarPassword">Alterar Palavra Passe</button>
      </div>


    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { computed } from 'vue'

import TopToolBarMenu from "../components/TopToolBarMenu.vue";
import { S3Client } from '@aws-sdk/client-s3'
import { Upload } from '@aws-sdk/lib-storage'
import FieldErrorPopup from '@/components/FieldErrorPopup.vue'
import {UserStore} from "@/store/userStore.js";
import ErrorPopup from "@/components/ErrorPopup.vue";
import SuccessPopup from "@/components/SuccessPopup.vue";
const API_BASE_URL = import.meta.env.VITE_API_URL;

const imageFile = ref(null)
const imagePreview = ref('')

const userStore = UserStore()
const Minio_URL = import.meta.env.VITE_MINIO_URL;

// MinIO client setup (igual ao registo)
const s3Client = new S3Client({
  region: 'us-east-1',
  endpoint: `${Minio_URL}`,
  credentials: {
    accessKeyId: 'admin',
    secretAccessKey: 'admin123',
  },
  forcePathStyle: true,
})
const novaPassword = ref('')

const alterarPassword = async () => {
  if (novaPassword.value.trim().length < 6) {
    passwordError.value = 'A nova palavra-passe deve ter pelo menos 6 caracteres.'
    showPasswordError.value = true
    return
  }


    try {
      const response = await fetch(`${API_BASE_URL}/user/password`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ newPassword: novaPassword.value }),
      });

      if (response.ok) {
        showSuccess("Palavra-passe alterada com sucesso!")
        novaPassword.value = ''
        passwordError.value = ''
        showPasswordError.value = false
      }
     else {
          const err = await response.text()
          console.log("Erro ao atualizar palavra-passe: " + err)
          showError("Não foi possível atualizar a palavra-passe. Tente novamente mais tarde.!")
        }
    } catch (error) {
      console.error("Erro ao atualizar a palavra-passe:", error)
      showError("Ocorreu um erro. Tente novamente mais tarde.")
    }
}

const passwordError = ref('')
const showPasswordError = ref(false)

const clearPasswordError = () => {
  if (passwordError.value) {
    passwordError.value = ''
    showPasswordError.value = false
  }
}




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
  return `${Minio_URL}/profile-images/${fileName}`
}


//alterar foto
const alterarFoto = async () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'

  input.onchange = async (event) => {
    const file = event.target.files[0]
    if (!file) return

    imageFile.value = file
    imagePreview.value = URL.createObjectURL(file)
  }

  input.click()
}


// Estado do perfil
const username = ref(userStore.username)
const profileImage = ref(userStore.avatarUrl)

const token = userStore.authToken


// Parte do plano, premium ou não
const isPremium = ref(false)
const isPremiumStatus = computed(() => isPremium.value)

const checkPremiumStatus = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/user/premium`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })

    if (response.ok) {
      const result = await response.json()
      isPremium.value = result
    } else {
      console.warn('Erro ao verificar status premium.')
    }
  } catch (err) {
    console.error('Erro na verificação de premium:', err)
  }
}

onMounted(() => {
  checkPremiumStatus()
})

// Funções para guardar alterações, alterar foto, logout etc aqui
const guardarPerfil = async () => {
  try {
    let uploadedUrl = profileImage.value

    // Se o utilizador escolheu uma nova imagem, faz upload primeiro
    if (imageFile.value) {
      uploadedUrl = await uploadImageToMinIO(imageFile.value)
    }

    const response = await fetch(`${API_BASE_URL}/user/profile`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username: username.value,
        profileImage: uploadedUrl,
      }),
    })

    if (response.ok) {
      profileImage.value = uploadedUrl
      imagePreview.value = ''       // limpa preview
      imageFile.value = null        // limpa ficheiro

      const userData = {
        'username': username.value,
        'avatarUrl': uploadedUrl,
      }

      userStore.setUserData(userData)

      showSuccess('Perfil atualizado com sucesso!')
    } else {
      showError('Não foi possível atualizar dados do Perfil. Tente novamente mais tarde.')
    }
  } catch (error) {
    console.error('Erro ao guardar perfil:', error)
    showError('Não foi possível guardar as alterações.\nTente novamente mais tarde.')
  }
}

const errorMessage = ref('')
const successMessage = ref('')

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


.nav-button {
  background-color: white;
  color: #427F99;
  border-radius: 8px;
  padding: 0.5rem 1rem;
  margin-left: 0.5rem;
  text-decoration: none;
  font-weight: bold;
  cursor: pointer;
  border: none;
}

.nav-button:hover {
  background-color: #e0e0e0;
}

.profile-container {
  max-width: 500px;
  background-color: #427F99;
  color: white;
  margin: 2rem auto;
  padding: 2rem;
  border-radius: 16px;
  text-align: center;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.profile-picture-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
}
input[type="password"] {
  background-color: white; /* White background for password input */
  color: #333;
  font-family: sans-serif;
  letter-spacing: 0.05em;
  border-radius: 8px;
  border: none;
  padding: 0.6rem;
}

.profile-picture {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid white;
  margin-bottom: 1rem;
}

.change-photo-button {
  background-color: white;
  color: #427F99;
  border: none;
  border-radius: 8px;
  padding: 0.4rem 1rem;
  font-weight: bold;
  cursor: pointer;
}

.change-photo-button:hover {
  background-color: #f0f0f0;
}
input.error {
  border-color: #ef4444;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
}
.input-wrapper {
  position: relative;
}


.username-display {
  margin: 1rem 0;
}

.form-group {
  margin-bottom: 1.5rem;
  text-align: left;
}

.form-group label {
  display: block;
  margin-bottom: 0.25rem;
  font-weight: bold;
}

.form-group input {
  background-color: white; /* Make the background white */
  color: #333; /* Optional: change text color for contrast */
  border: none;
  border-radius: 8px;
  padding: 0.6rem;
  font-size: 1rem;
  width: 100%;
  box-sizing: border-box; /* prevent width issues */
}

.buttons {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.save-button {
  background-color: #3cb371;
  color: white;
  padding: 0.75rem;
  border: none;
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
    margin-bottom: 1rem; /* add this line */

  }

.save-button:hover {
  background-color: #2e8b57;
}

@media (max-width: 768px) {
  .profile-container {
    width: 90%;
    margin: 1.5rem auto;
  }

  .navbar {
    flex-direction: column;
    text-align: center;
  }

  .nav-button {
    margin: 0.5rem auto;
  }
  .mini-preview {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-top: 1rem;
  }

  .mini-preview img {
    width: 60px;
    height: 60px;
    object-fit: cover;
    border-radius: 50%;
    border: 2px dashed white;
    margin-top: 0.5rem;
  }
}
</style>
