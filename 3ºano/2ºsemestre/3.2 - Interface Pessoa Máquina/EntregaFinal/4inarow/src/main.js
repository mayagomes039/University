import './assets/main.css';
import { createApp } from 'vue';
import router from './router';
import App from './App.vue';
import Button from './components/ui/Button.vue';

const app = createApp(App);

app.component('Button', Button);
app.use(router);
app.mount('#app');