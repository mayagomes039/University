<template>
    <div class="logout-container">
      <h1>Logout</h1>
      <p>Tens a certeza que queres efetuar log out?</p>
        <div class="button-container">
            <button @click="sair()">sim</button>
            <button @click="$router.go(-1)">não</button>
        </div>
    </div>
</template>
  
<script>
  export default {
    methods: {
      sair() {
        localStorage.removeItem('loggedInUserPosto');
        new Promise(resolve => setTimeout(resolve, 500));
        this.$router.push('/'); 
      },
    },
  };
</script>
  
  <style scoped>
  .logout-container {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    height: 100vh;
  }
  
  .button-container {
    display: flex;
    justify-content: space-between;
    width: 200px;
    margin-top: 20px;
  }
  
  button {
    padding: 10px 20px;
    border: none;
    border-radius: 5px;
    cursor: pointer;
    transition: all 0.3s ease-in;
  }
  
  button:hover {
    opacity: 0.8;
  }
  
  .logout-container button:first-child { 
    background-color: var(--color-accent); 
    color: var(--color-light);
  }
  
  .logout-container button:last-child { 
    background-color: #ddd;
    color: #333;
  }
  </style>
  