<template>
  <div id="app">
    <v-app class="app-scaled">
      <router-view />
    </v-app>
  </div>
</template>

<script>
export default {
  name: 'App',
};
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  width: 100%;
  height: 100%;
  font-family: Avenir, Helvetica, Arial, sans-serif;
  background-color: #f5f5f5; /* Cor de fundo para a área extra */
}

#app {
  display: flex;
  justify-content: center;
  align-items: center;
}

.app-scaled {
  width: 80vw !important;
  height: 80vh !important;
  background-color: white;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1); /* Opcional: sombra para destacar */
  overflow: auto;
}

/* Alternativa com transform se preferir que internamente seja "100%" */
.app-scaled-transform {
  transform: scale(0.8);
  transform-origin: center;
  width: 100vw !important;
  height: 100vh !important;
  background-color: white;
}
</style>