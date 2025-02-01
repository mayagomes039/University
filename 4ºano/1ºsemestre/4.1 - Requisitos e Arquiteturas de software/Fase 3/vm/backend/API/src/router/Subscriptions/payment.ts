import { Router } from "express";
import axios from "axios";
import { checkAuthMiddleware } from "../../middleware";

const paymentRouter = Router();

paymentRouter.post('/', checkAuthMiddleware, async(req, res) => {
    try {
        const response = await axios.post(`${process.env.SUBSCRIPTIONS_API}/api/payment`, req.body);
        res.json(response.data);
    } catch (error: any) {
        res.status(400).json({ error: error.response?.data?.message });
    }
});


export default paymentRouter;