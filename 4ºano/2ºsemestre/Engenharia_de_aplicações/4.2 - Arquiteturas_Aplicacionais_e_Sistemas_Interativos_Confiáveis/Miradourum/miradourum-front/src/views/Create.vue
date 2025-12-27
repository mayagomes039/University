<template>
  <TopToolBarMenu />
  

  <div class="home-page">

    <div class="form-container">
      <h1>Criar Ponto de Interesse</h1>

      <SuccessPopup v-if="successMessage" :text="successMessage" />
      <ErrorPopup v-if="errorMessage" :text="errorMessage" />

      <form @submit.prevent="handleSubmit" class="poi-form">
  <label>
    Latitude:
    <input v-model.number="latitude" type="number" step="any" required placeholder="Ex: 41.5598" />
  </label>

  <label>
    Longitude:
    <input v-model.number="longitude" type="number" step="any" required placeholder="Ex: -8.3970" />
  </label>

  <label>
    Nome:
    <input v-model="name" type="text" required placeholder="Nome do ponto" />
  </label>

  <label class="description-label">
    Descrição:
    <div class="textarea-wrapper">
      <textarea
        v-model="description"
        required
        placeholder="Descreva o local e o que o torna especial"
      />
      <span
        class="char-counter"
        :class="{ 'char-limit-exceeded': remainingDescriptionChars < 0 }"
      >
        {{ remainingDescriptionChars }}
      </span>
    </div>
  </label>

  <label>
    Dificuldade:
    <select v-model.number="dificulty" required>
      <option disabled value="">Selecione a dificuldade</option>
      <option :value="1">1 - Muito Fácil</option>
      <option :value="2">2 - Fácil</option>
      <option :value="3">3 - Média</option>
      <option :value="4">4 - Difícil</option>
      <option :value="5">5 - Muito Difícil</option>
    </select>
  </label>

<div class="checkbox-group">
  <label for="accessibility">Acessível</label>
  <input type="checkbox" id="accessibility" v-model="accessibility" />
</div>

<div class="checkbox-group">
  <label for="premium">Premium</label>
  <input type="checkbox" id="premium" v-model="premium" />
</div>


  <label>Imagens:
    <input type="file" multiple accept="image/*" @change="handleImageUpload" />
  </label>

  <div class="preview-container">
    <img v-for="(url, index) in imagePreviews" :key="index" :src="url" class="image-preview" />
  </div>

  <button type="submit">Criar</button>
</form>

    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { S3Client } from '@aws-sdk/client-s3'
import { Upload } from '@aws-sdk/lib-storage'
import LogoButton from '@/components/LogoButton.vue'
import SuccessPopup from '@/components/SuccessPopup.vue'
import ErrorPopup from '@/components/ErrorPopup.vue'
import {UserStore} from "@/store/userStore.js";
import TopToolBarMenu from "@/components/TopToolBarMenu.vue";
const API_BASE_URL = import.meta.env.VITE_API_URL;
const Minio_URL = import.meta.env.VITE_MINIO_URL;
const router = useRouter()
const userStore = UserStore()


const latitude = ref(0)
const longitude = ref(0)
const name = ref('')
const description = ref('')
const dificulty = ref(1)
const accessibility = ref(false)
const premium = ref(false)

const imageFiles = ref([])
const imagePreviews = ref([])
const errorMessage = ref('')
const successMessage = ref('')

const maxDescriptionLength = 500 // ou o valor que quiseres
const remainingDescriptionChars = computed(() => maxDescriptionLength - description.value.length)


// MinIO config
const s3Client = new S3Client({
  region: 'us-east-1',
  endpoint: `${Minio_URL}`,
  credentials: {
    accessKeyId: 'admin',
    secretAccessKey: 'admin123',
  },
  forcePathStyle: true,
})

// Upload multiple images
const uploadImagesToMinIO = async (files) => {
  const urls = []

  for (const file of files) {
    const fileName = `poi-${Date.now()}-${file.name}`

    const upload = new Upload({
      client: s3Client,
      params: {
        Bucket: 'poi-images',
        Key: fileName,
        Body: file,
        ContentType: file.type,
        ACL: 'public-read',
      },
    })

    await upload.done()
    urls.push(`${Minio_URL}/poi-images/${fileName}`)
  }

  return urls
}

const handleImageUpload = (event) => {
  const files = Array.from(event.target.files)
  imageFiles.value = files
  imagePreviews.value = files.map(file => URL.createObjectURL(file))
}
const goBack = () => {
  router.back()
}
const handleSubmit = async () => {
  errorMessage.value = ''
  successMessage.value = ''

  // Validation
const missingFields = []
const email = userStore.email

if (!latitude.value) missingFields.push('Latitude')
if (!longitude.value) missingFields.push('Longitude')
if (!name.value.trim()) missingFields.push('Nome')
if (!description.value.trim()) missingFields.push('Descrição')
if (!email) missingFields.push('Email do utilizador')
if (!dificulty.value) missingFields.push('Dificuldade')
if (imageFiles.value.length === 0) missingFields.push('Imagens')

if (missingFields.length > 0) {
  errorMessage.value = `Por favor preencha os seguintes campos: ${missingFields.join(', ')}.`
  return
}


  try {
    const imageUrls = await uploadImagesToMinIO(imageFiles.value)

    const payload = {
      latitude: latitude.value,
      longitude: longitude.value,
      name: name.value.trim(),
      description: description.value.trim(),
      dificulty: dificulty.value,
      accessibility: accessibility.value,
      premium: premium.value,
      creatorEmail: email,
      imageUrls,
    }

    const token = userStore.authToken;

    const response = await fetch(`${API_BASE_URL}/pi`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
      },
      body: JSON.stringify(payload),
    })

    if (!response.ok) {
      const msg = await response.text()
      errorMessage.value = msg || 'Erro ao criar ponto.'
      return
    }

    successMessage.value = 'Criação de Ponto enviada com sucesso! À espera de aprovação do Revisor.'
    setTimeout(() => router.push('/home'), 2500)
  } catch (err) {
    console.error(err)
    errorMessage.value = 'Erro ao criar ponto. Verifique os dados e tente novamente.'
  }
}

</script>

<style scoped>
.form-container {
  max-width: 600px;
  margin: 2rem auto;
  background: #fff;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  color: black;
}
.checkbox-group {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.poi-form input,
.poi-form select,
.poi-form textarea {
  width: 100%;
  padding: 0.75rem;
  margin-bottom: 1rem;
  border-radius: 8px;
  border: 1px solid #ccc;
  font-size: 1rem;
}

.poi-form textarea {
  min-height: 80px;
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

.poi-form button {
  width: 100%;
  padding: 0.75rem;
  font-weight: bold;
  background-color: #427F99;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
}

.poi-form button:hover {
  background-color: #35677c;
}

.preview-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 1rem;
}

.image-preview {
  width: 100px;
  border-radius: 8px;
  object-fit: cover;
  height: 100px;
}

.textarea-wrapper {
  position: relative;
  display: inline-block;
  width: 100%;
}

.textarea-wrapper textarea {
  width: 100%;
  padding-right: 3rem; /* espaço para o contador */
  box-sizing: border-box;
  resize: vertical;
  min-height: 80px;
}

.char-counter {
  position: absolute;
  bottom: 8px;   /* sobe um pouco mais */
  right: 12px;   /* afasta da borda */
  font-size: 0.85em;
  color: #888;
  pointer-events: none;
  background-color: white; /* opcional, se quiser que o número fique legível sobre o fundo */
  padding: 0 4px;
}


.char-limit-exceeded {
  color: red;
}


</style>
