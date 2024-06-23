<template>
    <nav id="nav">
        <div class="logo-container">
            <img src="../../public/image.png" alt="Logo" width="80" height="60">
            <div id="hora-atual" class="hora-atual"></div>
        </div>
        <div class="nav-items">
            <div class="nav-items">
                <router-link to="/logOut">LogOut</router-link>
            </div>
        </div>
    </nav>
    <div class="box">
        <h1>Detalhes do Serviço: {{ serviceDefinition ? serviceDefinition.descr : '' }}</h1>
        <div v-if="service" class="details-box">

            <div class="service-details-container">
                <div class="service-details">
                    <p><strong>ID:</strong> {{ service.id }}</p>
                    <p><strong>Veículo:</strong> {{ service.vehicleId }}</p>
                    <p><strong>Estado:</strong> {{ service.estado }}</p>
                    <p><strong>Agendamento:</strong> {{ service.agendamento }}</p>
                    <p><strong>Data:</strong> {{ formatDate(service.data) }}</p>
                    <p><strong>Descrição:</strong> {{ getDescription() }}</p>
                </div>

                <div class="client-details" v-if="client">
                    <p><strong>Nome do Cliente:</strong> {{ client.nome }}</p>
                    <p><strong>Telefone:</strong> {{ client.telefone }}</p>
                    <p v-if="service.orderedParts"><strong>Peças Encomendadas:</strong> {{ service.orderedParts }}</p>
                </div>
            </div>
            <div class="actions">
                <button v-if="!serviceStarted" @click="startService">Iniciar Serviço</button>
                <button v-if="!serviceStarted" @click="goBack1">Voltar Atrás</button>
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
            serviceStarted: false,
            showOrderPopup: false,
            orderedParts: '',
            showFinalPopup: false,
            orderedServices: '',
            showPreFinalPopup: false,
            serviceDefinition: null
        };
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
        formatDate(date) {
            if (!date) return 'sem data';
            const { dia, mes, ano, hora, minutos } = date;
            return `${dia}/${mes}/${ano} ${hora}:${minutos}`;
        },
        getDescription() {
            if (!this.service || !this.service.descrição) return 'sem descrição';
            return this.service.descrição;
        },
        startService() {
            this.service.estado = 'a decorrer';
            this.startTime = { hora: new Date().getHours(), minutos: new Date().getMinutes() };
            this.service.startTime = this.startTime;
            this.serviceStarted = true;
            fetch('http://localhost:3000/services/' + this.service.id, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    estado: this.service.estado,
                    startTime: this.startTime
                }),
            })
                .then(response => {
                    if (response.ok) {
                        console.log('Serviço iniciado com sucesso.');
                    } else {
                        throw new Error('Erro ao iniciar o serviço.');
                    }
                })
                .catch(error => {
                    console.error('Erro ao iniciar o serviço:', error);
                });
            this.$router.push('/servico-em-andamento/' + this.service.id);
        },

        goBack1() {
            this.$router.go(-1);
        }

    },
    created() {
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

.logo-container {
    display: flex;
    align-items: center;
}

.hora-atual {
    margin-left: 20px;
}
</style>