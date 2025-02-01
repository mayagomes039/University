import { Router } from "express";
import userRouter from "./Users/user";
import dayRouter from "./Users/day";
import paymentRouter from "./Subscriptions/payment";
import subscriptionRouter from "./Subscriptions/subscription";
import projectRouter from "./Projects/project";
import toolRouter from "./Projects/tool";
import imageRouter from "./Projects/image";

const getRouter = () => {
    const router = Router();

    router.use("/projects/project", projectRouter);
    router.use("/projects/tool", toolRouter);
    router.use("/projects/image", imageRouter);

    router.use("/subscriptions/subscription", subscriptionRouter);
    router.use("/subscriptions/payment", paymentRouter);
    
    router.use("/users/user", userRouter);
    router.use("/users/day", dayRouter);

    return router;
}

export default getRouter;