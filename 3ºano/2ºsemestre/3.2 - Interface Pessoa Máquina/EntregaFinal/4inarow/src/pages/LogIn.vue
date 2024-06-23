<template>
  <div>
    <nav id="nav">
      <div class="logo-container">
        <img src="../../public/image.png" alt="Logo" width="80" height="60">
        <div id="hora-atual" class="hora-atual"></div>
      </div>
      <h3></h3>
      <div class="nav-items">

      </div>
    </nav>
    <div class="container">
      <div class="login-box">
        <h2 class="login-title">Login</h2>
        <div class="input-group">
          <label for="email">Email:</label>
          <input type="text" id="email" v-model="email" class="input-field">
        </div>
        <div class="input-group">
          <label for="password">Password:</label>
          <input type="password" id="password" v-model="password" class="input-field">
        </div>
        <button @click="submit" class="submit-button">Submeter</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      email: '',
      password: ''
    }
  },
  methods: {
  async submit() {
    try {
      const response = await fetch('http://localhost:3000/mecanicos/', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        throw new Error('Erro ao buscar mecânicos');
      }

      const mechanics = await response.json();

      const mechanic = mechanics.find(mechanic => mechanic.email === this.email && mechanic.palavraPasse === this.password);

      if (mechanic) {
        // Armazenar o posto no localStorage
        localStorage.setItem('loggedInUserPosto', mechanic.posto);
        new Promise(resolve => setTimeout(resolve, 500));
        // Redirecionar para a página apropriada
        if (mechanic.posto == "1") this.$router.push('/servicos-combustao');
        else if (mechanic.posto == "2") this.$router.push('/servicos-eletricos');
        else if (mechanic.posto == "3") this.$router.push('/servicos-hibridos');
        else if (mechanic.posto == "4") this.$router.push('/servicos-gerais');
        
      } else {
        alert('Email ou senha inválidos');
      }
    } catch (error) {
      console.error('Erro:', error.message);
      alert('Erro ao buscar mecânicos');
    }
  }
}
}
</script>


<style scoped>
#nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background-color: rgb(124, 150, 167);
}

a {
  text-decoration: none;
  padding: 0.5rem 1.5rem;
  font-weight: 600;
  margin-left: 10px;
  border: 1px solid var(--color-accent);
  border-radius: 5px;
  color: var(--color-accent);
  transition: all 0.3s ease-in;
}

a.router-link-active {
  background-color: var(--color-accent);
  color: var(--color-light);
}

.login-title {
  color: white;
}

.container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}

.login-box {
  width: 400px;
  background-color: rgb(124, 150, 167);
  padding: 20px;
  border-radius: 10px;
  text-align: center;
}

.input-group {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 5px;
  color: white;
}

.input-field {
  padding: 8px;
  background-color: #AE925B;
  border-radius: 5px;
  width: 100%;
  box-sizing: border-box;
}

.submit-button {
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
  background-color: var(--color-accent);
  color: var(--color-light);
  cursor: pointer;
  transition: all 0.3s ease-in;
}

.submit-button:hover {
  background-color: #AE925B;
}

.logo-container {
  display: flex;
  align-items: center;
}

.hora-atual {
  margin-left: 20px;
}
</style>
