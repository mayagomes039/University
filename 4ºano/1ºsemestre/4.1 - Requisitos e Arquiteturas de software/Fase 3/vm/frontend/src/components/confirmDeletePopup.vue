<script setup>
import { defineProps, defineEmits } from 'vue';

const emit = defineEmits(['confirm', 'close']);

const handleConfirm = () => emit('confirm');
const handleCancel = () => emit('close');
</script>

<template>
    <div class="popup">
        <div class="popup-content">
            <button class="close-popup" @click="handleCancel">×</button>
            <h2>Tem a certeza que remover este projeto?</h2>>
            <div class="button-section">
                <button class="yes-button" @click="handleConfirm">Sim</button>
                <button class="no-button" @click="handleCancel">Não</button>
            </div>
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
        width: 32rem;
        background-color: white;
        padding: 20px;
        border-radius: 8px;
        text-align: center;
        box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
        border: 3px solid #1e40af;
        position: relative;
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

    h2 {
        color: #555;
        margin: 20px 0;
    }

    .button-section {
        display: flex;
        justify-content: center;
        gap: 10px;
        margin-top: 20px;
    }

    .yes-button {
        padding: 10px 20px;
        background-color: #1e88e5;
        color: white;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        font-weight: bold;
    }

    .yes-button:hover {
        background-color: #1565c0;
    }

    .no-button {
        padding: 10px 20px;
        background-color: #e53935;
        color: white;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        font-weight: bold;
    }

    .no-button:hover {
        background-color: #d32f2f;
    }
</style>