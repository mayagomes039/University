import { Router } from "express";
import axios from "axios";
import { checkAuthMiddleware } from "../../middleware";

const dayRouter = Router();

dayRouter.post('/:id', checkAuthMiddleware, async(req, res) => {
    try {
        const response = await axios.post(`${process.env.USERS_API}/api/day/${req.params.id}`);
        res.json(response.data);
    } catch (error: any) {
        res.status(400).json({ error: error.response?.data?.message });
    }
});


export default dayRouter;