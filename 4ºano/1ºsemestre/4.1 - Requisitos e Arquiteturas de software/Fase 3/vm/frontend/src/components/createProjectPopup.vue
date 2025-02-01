<script setup>
    import { ref } from 'vue';

    const emits = defineEmits(['close', 'create']);

    const projectName = ref('');
    const showError = ref(false);

    const confirm = () => {
        // make sure at least 1 char
        if (projectName.value.trim()){
            showError.value = false;
            emits('create', projectName.value.trim());
            projectName.value = ''
            emits('close');
        }
        else  {
            showError.value = true;
        }
    }

    const cancel = () => {
        projectName.value = '';
        showError.value = false;
        emits('close');
    }
</script>

<template>
    <div class="popup">
        <div class="create-project-container">
            <h2> Criar Projeto </h2>
            <input
                type="text"
                v-model="projectName"
                placeholder="Nome do Projeto"
                class="project-name"
            >
            <p v-if="showError" class="error-message">
                Insere um nome para o projeto.
            </p>
            <div class="button-group">
                <button @click="confirm" class="confirm-button">Confirmar</button>
                <button @click="cancel" class="cancel-button">Cancelar</button>
            </div>
        </div> 
    </div>
</template>

<style scoped>

    .popup {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: rgba(0, 0, 0, 0.5);
        display: flex;
        justify-content: center;
        align-items: center;
    }

    .create-project-container {
        background: white;
        padding: 2rem;
        border-radius: 10px;
        text-align: center;
        box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
        display: flex;
        flex-direction: column;
        gap: 1.5rem;
        width: 25%;
        min-width: 300px;
    }

    .project-input {
        width: 80%;
        padding: 0.5rem;
        margin: 1rem 0;
        border: 1px solid #ccc;
        border-radius: 5px;
    }

    .error-message {
        color: red;
        font-size: 0.9rem;
        margin-top: -0.5rem;
        margin-bottom: 1rem;
    }

    .button-group {
        display: flex;
        gap: 1rem;
        justify-content: center;
    }

    .confirm-button {
        background-color: #1e40af;
        color: white;
        padding: 0.5rem 1rem;
        border: none;
        border-radius: 5px;
        cursor: pointer;
    }

    .cancel-button {
        background-color: #ccc;
        color: black;
        padding: 0.5rem 1rem;
        border: none;
        border-radius: 5px;
        cursor: pointer;
    }

    .error-message {
        color: red;
        font-size: 0.9rem;
        margin-top: -0.5rem;
        margin-bottom: 1rem;
    }

    h2 {
        color: black;
    }

    input {
        padding: 0.8rem;
        font-size: 1rem;
        border: 1px solid #ccc;
        border-radius: 5px;
        background-color: white; /* Fundo branco para as caixas de texto */
        color: #666;
    }
  
    input:focus, select:focus {
        outline: none;
        border-color: #457b9d;
    }
</style>