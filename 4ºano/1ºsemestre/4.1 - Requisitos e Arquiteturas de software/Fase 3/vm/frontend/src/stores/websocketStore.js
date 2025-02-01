// websocketStore.js
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useWebSocketStore = defineStore('websocket', () => {
    const socket = ref(null);
    const isConnected = ref(false);
    let reconnectTimeout = null;
  
    const connectWebSocket = (userId) => {
        if (socket.value && socket.value.readyState === WebSocket.OPEN) {
            console.warn("WebSocket is already connected.");
            return;
        }
  
        const ws = new WebSocket('wss://l.primecog.com/ws/');

        ws.onopen = () => {
            console.log("WebSocket connection opened");
            socket.value = ws;
            isConnected.value = true;
            console.log("socket.value set:", socket.value);
            const payload = { userId };
            ws.send(JSON.stringify(payload));
        };


        ws.onmessage = (event) => {
            try {
                const message = JSON.parse(event.data);

                if (message.registrationSuccess) {
                    console.log(`reg successful for user: ${message.userId}`);
                } else if (message.error) {
                    console.error(`Error: ${message.error}`);
                }
            } catch (error) {
                console.error("err:", error);
            }
        };
  
        ws.onerror = (error) => {
            console.error("WebSocket error:", error);
            isConnected.value = false;
        };
  
        ws.onclose = (event) => {
            console.log("WebSocket connection closed:", event.code);
            isConnected.value = false;
            socket.value = null;

            if (!event.wasClean) {
                console.log("Attempting to reconnect...");
                reconnectTimeout = setTimeout(() => connectWebSocket(userId), 5000);
            }
        };
    };
  
    const closeWebSocket = () => {
        if (socket.value) {
            clearTimeout(reconnectTimeout);
            socket.value.close();
            isConnected.value = false;
            socket.value = null;
        }
    };
  
    return {
        socket,
        isConnected,
        connectWebSocket,
        closeWebSocket,
    };
});
