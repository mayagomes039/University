import WebSocket, { WebSocketServer } from 'ws';
import express from 'express';
import http from 'http';

const app = express();
const server = http.createServer(app);
const wss = new WebSocketServer({ server });

const port = parseInt(process.env.PORT || '8080', 10);
const clients = new Map<string, WebSocket>();


// Configuração do WebSocket
wss.on('connection', (ws) => {
    console.log('New client has connected ...');

    ws.on('message', (data: any) => {
        try {
            const parsedData = JSON.parse(data); // Assuming the message is JSON
            const { userId } = parsedData;

            if (!userId) {
                ws.send(JSON.stringify({ error: 'userId missing' }));
                return;
            }

            // Registrar cliente no mapa
            if (!clients.has(userId)) {
                clients.set(userId, ws);
                console.log(`Registered client: ${userId}`);
                ws.send(JSON.stringify({ registrationSuccess: true, userId }));
            }
        } catch (error) {
            console.error('Error processing image:', error);
            ws.send(JSON.stringify({ error: 'Invalid message.' }));
        }
    });

    ws.on('close', () => {
        for (const [userId, client] of clients.entries()) {
            if (client === ws) {
                clients.delete(userId);
                console.log(`Unknown client: ${userId}`);
                break;
            }
        }
    });

    ws.on('error', (error) => {
        console.error('Erro no WebSocket:', error.message);
    });
});


// Enviar mensagem para um cliente específico
const sendMessageToUser = (userId: string, message: any) => {
    const client = clients.get(userId);
    if (client && client.readyState === WebSocket.OPEN) {
        console.log("sending message to user "+ userId)
        console.log(JSON.stringify(message))
        client.send(JSON.stringify(message));
        return true
    } else {
        return false
    }
};


app.use(express.json());
app.post('/:userId', (req, res) => {
    const userId = req.params.userId;
    if (!req.body) {
        res.status(400).json({ error: 'Invalid params' });
        return;
    }

    const success = sendMessageToUser(userId, req.body);
    res.status(200).json({ success: success });
});


// Start server
server.listen(port, () => {
    console.log(`WebSocket Gateway running on ${port}`);
});
