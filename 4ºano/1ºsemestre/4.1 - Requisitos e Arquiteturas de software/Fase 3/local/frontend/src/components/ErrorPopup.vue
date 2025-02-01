<script setup>
import { defineProps, defineEmits } from 'vue';

const props = defineProps({
    title: {
        type: String,
        required: true,
    },
    message: {
        type: String,
        required: true,
    },
    fileName: {
        type: String,
        required: false
    },
});

const emit = defineEmits(['close']);

const closePopup = () => {
    emit('close');
};
</script>

<template>
    <div class="popup">
        <div class="popup-content">
            <button class="close-popup" @click="closePopup">X</button>
            <h3>{{ title }}</h3>
            <ul v-if="fileName">
                <li>{{ fileName }}</li>
            </ul>
            <p>{{ message }}</p>
            <button class="confirm-button"@click="closePopup">
                OK
            </button>
        </div>
    </div>
</template>

<style scoped>
    .popup {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background-color: rgba(0, 0, 0, 0.5);
        display: flex;
        justify-content: center;
        align-items: center;
        z-index: 100;
    }

    .popup-content {
        width: 36rem;
        background-color: white;
        padding: 20px;
        border-radius: 8px;
        text-align: center;
        box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
        position: relative;
        border: 3px solid #1e40af;
    }

    .close-popup {
        position: absolute;
        top: 10px;
        right: 10px;
        background: none;
        border: none;
        font-size: 18px;
        cursor: pointer;
    }

    .confirm-button {
        margin-top: 20px;
        padding: 10px 20px;
        background-color: #e53935;
        color: white;
        border: none;
        border-radius: 4px;
        cursor: pointer;
    }

    .confirm-button:hover {
        background-color: #d32f2f;
    }

    h3 {
        margin-top: 0;
        font-size: 16px;
        color: #333;
        font-weight: bold;
    }

    p {
        font-size: 14px;
        color: #555;
        margin: 10px 0;
    }

    ul {
        list-style: none;
    }

    li {
        color: #333;
        font-weight: bold;
    }
</style>
