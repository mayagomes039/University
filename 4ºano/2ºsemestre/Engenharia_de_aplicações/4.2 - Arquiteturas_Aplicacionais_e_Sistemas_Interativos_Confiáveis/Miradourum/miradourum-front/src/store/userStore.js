import { defineStore } from "pinia";
import { ref } from 'vue';

export const UserStore = defineStore("userStore", () => {
    const id = ref("");
    const username = ref("");
    const authToken = ref("");
    const avatarUrl = ref("");
    const email = ref("");
    const userType = ref("");
    const location = ref(null);

    function setUserData(data) {
        if (data.id) {
            id.value = data.id;
        }

        if (data.username) {
            username.value = data.username;
        }

        if (data.avatarUrl) {
            avatarUrl.value = data.avatarUrl || '/default-profile.png';
        }

        if (data.email) {
            email.value = data.email;
        }

        if (data.userType) {
            userType.value = data.userType;
        }
    }

    function setToken(token) {
        authToken.value = token;
    }

    function setLocation(locationInput) {
        location.value = {
            'lng': locationInput.lng,
            'lat': locationInput.lat,
        }
    }

    function clearUserStore() {
        id.value = "";
        username.value = "";
        avatarUrl.value = "/default-profile.png";
        authToken.value = "";
        email.value = "";
        userType.value = "";
        location.value = null;
    }

    return {
        id,
        username,
        authToken,
        avatarUrl,
        email,
        userType,
        location,
        setToken,
        setLocation,
        setUserData,
        clearUserStore,
    };
}, {
    // Enable persistence
    persist: {
        key: 'userStore',
        storage: localStorage,
        paths: ['id', 'username', 'authToken', 'email', 'userType', 'avatarUrl', 'location']
    }
});
