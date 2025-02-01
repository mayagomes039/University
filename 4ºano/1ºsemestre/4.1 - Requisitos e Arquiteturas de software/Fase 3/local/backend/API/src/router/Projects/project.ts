import { Router } from "express";
import axios from "axios";
import { checkAuthMiddleware } from "../../middleware";

const projectRouter = Router();

projectRouter.get('/:id', checkAuthMiddleware, async(req, res) => {
    try {
        const response = await axios.get(`${process.env.PROJECTS_API}/api/project/${req.params.id}`);
        res.json(response.data);
    } catch (error: any) {
        res.status(400).json({ error: error.response?.data?.message });
    }
});

projectRouter.get('/user/:id', checkAuthMiddleware, async(req, res) => {
    try {
        const response = await axios.get(`${process.env.PROJECTS_API}/api/project/user/${req.params.id}`);
        res.json(response.data);
    } catch (error: any) {
        res.status(400).json({ error: error.response?.data?.message });
    }
});

projectRouter.post('/', checkAuthMiddleware, async(req, res) => {
    try {
        const response = await axios.post(`${process.env.PROJECTS_API}/api/project/`, req.body);
        res.json(response.data);
    } catch (error: any) {
        res.status(400).json({ error: error.response?.data?.message });
    }
});

projectRouter.delete('/:id', checkAuthMiddleware, async(req, res) => {
    try {
        const response = await axios.delete(`${process.env.PROJECTS_API}/api/project/${req.params.id}`);
        res.json(response.data);
    } catch (error: any) {
        res.status(400).json({ error: error.response?.data?.message });
    }
});

projectRouter.post('/applyTools/:id', checkAuthMiddleware, async(req, res) => {
    try {
        const response = await axios.post(`${process.env.PROJECTS_API}/api/project/applyTools/${req.params.id}`, req.body);
        res.json(response.data);
    } catch (error: any) {
        res.status(400).json({ error: error.response?.data?.message });
    }
});

export default projectRouter;