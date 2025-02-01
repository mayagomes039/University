<script setup>
    import { defineProps, defineEmits } from 'vue';

    defineProps({
        visible: Boolean,
    });

    const emit = defineEmits(['close']);

    const EditProfile = () => {
        emit('close');
    };

    const Logout = () => {
        emit('close');
    };
</script>


<template>
    <div v-if="visible" class="dropdown-container" @click.stop>
        <ul class="dropdown-menu">
            <li @click="onEditProfile">Edit Profile</li>
            <li @click="onLogout">Logout</li>
        </ul>
    </div>
</template>
  
<style scoped>
    .dropdown-container {
        position: absolute;
        right: 0;
        top: 10%;
        background-color: white;
        border: 1px solid #ddd;
        border-radius: 5px;
        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        z-index: 10;
        margin-top: 1rem;
    }
  
    .dropdown-menu {
        list-style: none;
        padding: 0.5rem 0;
        margin: 0;
    }
    
    .dropdown-menu li {
        padding: 0.5rem 1rem;
        cursor: pointer;
        color: #666;
    }
    
    .dropdown-menu li:hover {
        background-color: #f5f5f5;
    }
</style>
  