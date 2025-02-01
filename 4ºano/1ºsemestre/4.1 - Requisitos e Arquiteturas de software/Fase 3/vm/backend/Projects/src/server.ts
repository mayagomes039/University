import express from 'express';
import dotenv from 'dotenv';
import getRouter from './router';
import { AppDataSource } from './db/data-source';
import cors from 'cors';
import { connectToRabbitMQ, listenToResultsQueue } from './utils/rabbitMq';


export const setupServer = async() => {
    dotenv.config();

    // Connect to RabbitMQ
    await connectToRabbitMQ();
    listenToResultsQueue()

    const app = express();
    app.use(cors());
    const port = process.env.PORT || 3333;

    app.use(express.json());

    app.use('/api', getRouter());

    await AppDataSource.initialize();
    app.listen(port, () => {
        console.log(`Servidor rodando em http://localhost:${port}`);
    });
}

setupServer();