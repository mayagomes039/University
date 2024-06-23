<template>
    <nav id="nav">
      <div class="logo-container">
        <img src="../../public/image.png" alt="Logo" width="80" height="60">
        <div id="hora-atual" class="hora-atual"></div>
      </div>
      <h1>{{ title }}</h1>
      <div class="nav-items">
        <div class="nav-items">
          <router-link to="/logOut">LogOut</router-link>
        </div>
      </div>
    </nav>
  </template>
  
  <script>
  export default {
    props: {
      title: String
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
  
  .logo-container {
    display: flex;
    align-items: center;
  }
  
  .hora-atual {
    margin-left: 20px;
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
  </style>