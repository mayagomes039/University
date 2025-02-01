import { Router } from "express";
import userRouter from "./user";
import dayRouter from "./day";

const getRouter = () => {
    const router = Router();
    router.use("/user", userRouter);
    router.use("/day", dayRouter);

    return router;
}

export default getRouter;