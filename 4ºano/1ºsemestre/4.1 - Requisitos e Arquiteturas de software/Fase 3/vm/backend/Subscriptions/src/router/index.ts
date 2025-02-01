import { Router } from "express";
import subscriptionRouter from "./subscription";
import paymentRouter from "./payment";

const getRouter = () => {
    const router = Router();
    router.use("/subscription", subscriptionRouter);
    router.use("/payment", paymentRouter);

    return router;
}

export default getRouter;