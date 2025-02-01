import express, { Request, Response } from 'express';
import dotenv from 'dotenv';
import getRouter from './router';
import cors from 'cors';
import cookieParser from 'cookie-parser';

export const setupServer = () => {
    dotenv.config();

    const app = express();
    app.use(cors({
        origin: 'http://localhost:2999',
        credentials: true,              
      }));
    const port = process.env.PORT || 3333;

    app.use(cookieParser());
    app.use(express.json());

    app.use('/api', getRouter());

    app.listen(port, () => {
        console.log(`Servidor rodando em http://localhost:${port}`);
    });
}

setupServer();