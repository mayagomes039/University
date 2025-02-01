import express, { Request, Response, NextFunction } from 'express';
import dotenv from 'dotenv';
import getRouter from './router';
import cors from 'cors';
import cookieParser from 'cookie-parser';

export const setupServer = () => {
    dotenv.config();
    const app = express();
    // CORS configuration
    app.use(cors({
        origin: true, // Allow all origins
        credentials: true, // Allow cookies and other credentials
    }));

    const port = process.env.PORT || 3333;

    app.use(cookieParser());
    app.use(express.json());

    app.use('/api', getRouter());

    app.use((err: any, req: Request, res: Response, next: NextFunction) => {
        console.error('Unhandled error:', err.message);
        res.status(err.status || 500).json({ error: err.message || 'Internal Server Error' });
    });

    app.listen(port, () => {
        console.log(`Servidor rodando em http://localhost:${port}`);
    });
}

setupServer();
