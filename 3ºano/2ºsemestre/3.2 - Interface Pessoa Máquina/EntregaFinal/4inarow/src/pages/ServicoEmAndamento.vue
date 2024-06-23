<template>
    <nav id="nav">
        <div class="logo-container">
            <img src="../../public/image.png" alt="Logo" width="80" height="60">
            <div id="hora-atual" class="hora-atual"></div>
        </div>

    </nav>
    <div class="box">
        <h1>Detalhes do Serviço: {{ serviceDefinition ? serviceDefinition.descr : '' }}</h1>
        <div v-if="service" class="details-box">
            <div v-if="showOrderPopup" class="order-popup">
                <span @click="closeOrderPopup" class="close-btn">&times;</span>
                <h2>Encomendar Peças</h2>
                <textarea v-model="orderedParts" placeholder="Digite as peças que deseja encomendar..."></textarea>
                <button @click="submitOrder">Submeter</button>
            </div>

            <div v-if="showPreFinalPopup" class="order-popup">
                <span @click="closePreFinalPopup" class="close-btn">&times;</span>
                <h2>Escolha uma opção:</h2>
                <button @click="openFinalPopup">Adicionar comentarios</button>
                <button @click="endService">Finalizar serviço</button>
            </div>

            <div v-if="showFinalPopup" class="order-popup">
                <span @click="closeFinalPopup" class="close-btn">&times;</span>
                <h2>Recomendações:</h2>
                <textarea v-model="orderedServices" placeholder="Digite as suas recomendações..."></textarea>
                <button @click="finalsubmit">Submeter e finalizar serviço</button>
            </div>

            <div class="service-details-container">
                <div class="service-details">
                    <p><strong>ID:</strong> {{ service.id }}</p>
                    <p><strong>Veículo:</strong> {{ service.vehicleId }}</p>
                    <p><strong>Estado:</strong> {{ service.estado }}</p>
                    <p><strong>Agendamento:</strong> {{ service.agendamento }}</p>
                    <p><strong>Data:</strong> {{ formatDate(service.data) }}</p>
                    <p><strong>Descrição:</strong> {{ getDescription() }}</p>
                    <p v-if="service.startTime" class="started-at"><strong>Serviço iniciado às:</strong> {{
                        formatTime(service.startTime) }}</p>
                </div>

                <div class="client-details" v-if="client">
                    <p><strong>Nome do Cliente:</strong> {{ client.nome }}</p>
                    <p><strong>Telefone:</strong> {{ client.telefone }}</p>
                    <p v-if="service.orderedParts"><strong>Peças Encomendadas:</strong> {{ service.orderedParts }}</p>
                </div>
            </div>
            <div class="actions">
                <button v-if="!encomenda" @click="openOrderPopup">Encomendar Peças</button>
                <button v-if="!encomenda" @click="openPreFinalPopup">Finalizar Serviço</button>
                <button v-if="!encomenda" @click="goBack">Voltar Atrás</button>
                <button v-if="encomenda" @click="openOrderPopup">Encomendar Peças</button>
                <button v-if="encomenda" @click="concluir">Concluir</button>
                <button v-if="encomenda" @click="goBack2">Voltar Atrás</button>

            </div>
        </div>
    </div>
</template>

<script>
export default {
    data() {
        return {
            service: null,
            client: null,
            vehicle: null,
            startTime: null,
            finalTime: null,
            serviceStarted: true,
            previousState: null,
            showOrderPopup: false,
            orderedParts: '',
            showFinalPopup: false,
            orderedServices: '',
            showPreFinalPopup: false,
            encomenda: false,
            serviceDefinition: null
        };
    },
    mounted() {
    // Atualiza as horas a cada segundo
    setInterval(this.atualizarHoras, 1000);
    },
    methods: {
        getServiceDetails() {
            const serviceId = this.$route.params.id;
            fetch('http://localhost:3000/services/' + serviceId)
                .then(response => {
                    if (response.ok) {
                        return response.json();
                    } else {
                        throw new Error('Something went wrong');
                    }
                })
                .then(data => {
                    this.service = data;
                    this.fetchClientDetails(this.service.vehicleId);
                    this.fetchServiceDefinition(this.service['service-definitionId']);
                })
                .catch(error => console.error('Erro na requisição:', error));
        },
        atualizarHoras() {
        const agora = new Date();
        const horas = agora.getHours().toString().padStart(2, '0');
        const minutos = agora.getMinutes().toString().padStart(2, '0');
        const segundos = agora.getSeconds().toString().padStart(2, '0');
        const horaAtual = `${horas}:${minutos}:${segundos}`;

        // Atualiza o elemento HTML com as horas atuais
        document.getElementById('hora-atual').textContent = horaAtual;
        },
        fetchClientDetails(vehicleId) {
            fetch('http://localhost:3000/vehicles/' + vehicleId)
                .then(response => {
                    if (response.ok) {
                        return response.json();
                    } else {
                        throw new Error('Something went wrong');
                    }
                })
                .then(data => {
                    this.vehicle = data;
                    const clientId = this.vehicle.clientId;
                    return fetch('http://localhost:3000/clients/' + clientId);
                })
                .then(response => {
                    if (response.ok) {
                        return response.json();
                    } else {
                        throw new Error('Something went wrong');
                    }
                })
                .then(data => {
                    this.client = data;
                })
                .catch(error => console.error('Erro na requisição dos detalhes do cliente:', error));
        },
        formatDate(date) {
            if (!date) return 'sem data';
            const { dia, mes, ano, hora, minutos } = date;
            return `${dia}/${mes}/${ano} ${hora}:${minutos}`;
        },
        fetchServiceDefinition(definitionId) {
        fetch('http://localhost:3000/service-definitions/' + definitionId)
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                throw new Error('Something went wrong');
            }
        })
        .then(data => {
            this.serviceDefinition = data;
        })
        .catch(error => console.error('Error fetching service definition:', error));
        },
        getDescription() {
            if (!this.service || !this.service.descrição) return 'sem descrição';
            return this.service.descrição;
        },
        formatTime(time) {
            if (!time) return 'sem hora';
            const { hora, minutos } = time;
            return `${hora}:${minutos}`;
        },
        endService() {
            this.service.estado = 'realizado';
            this.finalTime = { dia: new Date().getDate(), mes: new Date().getMonth() + 1, ano: new Date().getFullYear(), hora: new Date().getHours(), minutos: new Date().getMinutes() };
            this.service.finalTime = this.finalTime;
            fetch('http://localhost:3000/services/' + this.service.id, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    estado: this.service.estado,
                    finalTime: this.finalTime
                }),
            })
                .then(response => {
                    if (response.ok) {
                        console.log('Serviço finalizado com sucesso.');
                        this.$router.go(-2);
                    } else {
                        throw new Error('Erro ao finalizar o serviço.');
                    }
                })
                .catch(error => {
                    console.error('Erro ao finalizar o serviço:', error);
                });
        },
        concluir() {
            this.service.estado = 'suspenso';
            this.finalTime = null;
            this.service.finalTime = null;
            this.startTime = null;
            this.service.startTime = null;
            fetch('http://localhost:3000/services/' + this.service.id, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    estado: this.service.estado,
                    startTime: this.startTime,
                    finalTime: this.finalTime
                }),
            })
                .then(response => {
                    if (response.ok) {
                        console.log('Serviço concluido (com encomenda) com sucesso.');
                        this.$router.go(-2);
                    } else {
                        throw new Error('Erro ao finalizar o serviço.');
                    }
                })
                .catch(error => {
                    console.error('Erro ao finalizar o serviço:', error);
                });
        },
        goBack() {
            this.service.estado = "por iniciar",
                this.service.startTime = null;
            this.serviceStarted = false;
            fetch('http://localhost:3000/services/' + this.service.id, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    estado: this.service.estado,
                    startTime: null
                }),
            })
                .then(response => {
                    if (response.ok) {
                        console.log('Serviço interrompido com sucesso.');
                        this.$router.go(-2);
                    } else {
                        throw new Error('Erro ao finalizar o serviço.');
                    }
                })
                .catch(error => {
                    console.error('Erro ao voltar atras:', error);
                });
        },
        goBack2() {
            this.service.estado = "por iniciar",
                this.service.startTime = null;
            this.serviceStarted = false;
            fetch('http://localhost:3000/services/' + this.service.id, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    estado: this.service.estado,
                    startTime: null,
                    orderedParts: null
                }),
            })
                .then(response => {
                    if (response.ok) {
                        console.log('Serviço interrompido com sucesso.');
                        this.$router.go(-2);
                    } else {
                        throw new Error('Erro ao finalizar o serviço.');
                    }
                })
                .catch(error => {
                    console.error('Erro ao voltar atras:', error);
                });
        },
        goBack1() {
            this.$router.go(-2);
        },
        openOrderPopup() {
            this.showOrderPopup = true;
        },

        closeOrderPopup() {
            this.showOrderPopup = false;
            this.fetchClientDetails(this.service.vehicleId);
        },

        submitOrder() {
            if (!this.orderedParts) {
                alert('Por favor, insira as peças que deseja encomendar.');
                return;
            }
            this.orderedParts = this.orderedParts.trim();
            fetch('http://localhost:3000/services/' + this.service.id, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    orderedParts: this.orderedParts
                }),
            })
                .then(response => {
                    if (response.ok) {
                        console.log('Encomenda realizada com sucesso.');
                        this.showOrderPopup = false;
                        alert('Encomenda realizada com sucesso.');
                        this.encomenda = true;
                        this.startTime = null;
                        return response.json();
                    } else {
                        throw new Error('Erro ao iniciar o serviço.');
                    }
                })
                .then(data => {
                    this.service = data;
                })
                .catch(error => {
                    console.error('Erro ao encomendar peças:', error);
                    alert('Erro ao encomendar peças.');
                });
        },
        openPreFinalPopup() {
            this.showPreFinalPopup = true;
        },
        closePreFinalPopup() {
            this.showPreFinalPopup = false;
        },
        openFinalPopup() {
            this.showPreFinalPopup = false;
            this.showFinalPopup = true;
        },
        closeFinalPopup() {
            this.showFinalPopup = false;
        },
        finalsubmit() {
            if (!this.orderedServices) {
                alert('Por favor, insira as recomendações.');
                return;
            }
            this.orderedServices = this.orderedServices.trim();
            fetch('http://localhost:3000/services/' + this.service.id, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    orderedServices: this.orderedServices
                }),
            })
                .then(response => {
                    if (response.ok) {
                        console.log('Recomendações realizadas com sucesso.');
                        this.showFinalPopup = false;
                        this.endService();
                        alert('Recomendações realizadas com sucesso.');
                        return response.json();
                    } else {
                        throw new Error('Erro ao iniciar o serviço.');
                    }
                })
                .then(data => {
                    this.service = data;
                })
                .catch(error => {
                    console.error('Erro ao encomendar peças:', error);
                    alert('Erro ao encomendar peças.');
                });
        }

    },
    created() {
        const now = new Date();
        this.startTime = { hora: now.getHours(), minutos: now.getMinutes() };
        this.getServiceDetails();
    }
}
</script>

<style>
.details-box {
    background-color: rgb(124, 150, 167);
    color: white;
    padding: 20px;
    border-radius: 5px;
    position: relative;
}

.details-box p {
    margin: 0;
    margin-bottom: 10px;
}

.details-box strong {
    font-weight: bold;
}

.actions {
    position: absolute;
    bottom: 10px;
    right: 10px;
    flex-direction: row-reverse;
}

.actions button {
    margin-left: 10px;
}

.service-details-container {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.service-details {
    flex: 1;
    margin-right: 20px;
}

.client-details {
    flex: 1;
}

.started-at {
    color: black;
}

.order-popup {
    background-color: #BCA576;
    position: fixed;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    padding: 20px;
    border-radius: 5px;
    z-index: 999;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
}

.order-popup textarea {
    width: 100%;
    height: 100px;
    margin-bottom: 10px;
}

.order-popup button {
    float: right;
}

.pre-final-popup {
    background-color: #BCA576;
    position: fixed;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    padding: 20px;
    border-radius: 5px;
    z-index: 999;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
}

.pre-final-popup button {
    float: right;
}

.final-popup {
    background-color: #BCA576;
    position: fixed;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    padding: 20px;
    border-radius: 5px;
    z-index: 999;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
}

.final-popup textarea {
    width: 100%;
    height: 100px;
    margin-bottom: 10px;
}

.final-popup button {
    float: right;
}

.logo-container {
    display: flex;
    align-items: center;
}

.hora-atual {
    margin-left: 20px;
}

.close-btn {
    position: absolute;
    top: 5px;
    right: 10px;
    cursor: pointer;
    font-size: 24px;
    color: white;
}

#nav {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 20px;
    background-color: rgb(124, 150, 167);
}

.box {
    border: 1px solid #ccc;
    border-radius: 5px;
    padding: 20px;
    margin-top: 20px;
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

.custom-button {
    background-color: #AE925B;
}
</style>