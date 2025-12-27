import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

// Vuetify setup
import 'vuetify/styles'
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'

// vuetify for components
import '@fortawesome/fontawesome-free/css/all.css'
import { aliases, fa } from 'vuetify/iconsets/fa'

// Pinia for global store
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate' // For data persistence within refreshes

const vuetify = createVuetify({
    icons: {
        defaultSet: 'fa',
        aliases,
        sets: {
            fa,
        },
    },
    components,
    directives,
})

const pinia = createPinia()

// Add persistence plugin
pinia.use(piniaPluginPersistedstate)

createApp(App)
    .use(pinia)
    .use(router)
    .use(vuetify)
    .mount('#app')