import { Router } from "express";
import imageRouter from './image';
import projectRouter from "./project";
import toolRouter from "./tool";

const getRouter = () => {
    const router = Router();

    router.use('/project', projectRouter);
    router.use('/image', imageRouter);
    router.use('/tool', toolRouter);

    return router;
}

export default getRouter;
