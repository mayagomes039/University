import express from 'express';
import dotenv from 'dotenv';
import getRouter from './router';
import { AppDataSource } from './db/data-source';
import cors from 'cors';

export const setupServer = async() => {
    dotenv.config();

    const app = express();
    const port = process.env.PORT || 3333;
    app.use(cors());

    app.use(express.json());

    app.use('/api', getRouter());

    await AppDataSource.initialize();
    app.listen(port, () => {
        console.log(`Servidor rodando em http://localhost:${port}`);
    });
}

setupServer();