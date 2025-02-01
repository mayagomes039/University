import { Router } from "express";
import axios from "axios";
import multer from "multer";
import FormData from "form-data";
import fs from "fs";
import { checkAuthMiddleware } from "../../middleware";

const toolRouter = Router();

const upload = multer({ dest: "uploads/" });

toolRouter.post("/:id", checkAuthMiddleware, upload.single("file"), async (req, res) => {
    const file = req.file;

    try {
        const formData = new FormData();

        // Add the uploaded file to the FormData
        if (file) {
            formData.append("file", fs.createReadStream(file.path), file.originalname);
        }

        // Add all other request body fields to FormData
        Object.keys(req.body).forEach((key) => {
            formData.append(key, req.body[key]);
        });

        // Forward the request to the backend service
        const response = await axios.post(`${process.env.PROJECTS_API}/api/tool/${req.params.id}`, formData, {
            headers: {
                ...formData.getHeaders(), // Include multipart headers
            },
        });

        // Clean up the uploaded file after forwarding
        if (file) fs.unlinkSync(file.path);

        // Send back the response from the backend to the client
        res.status(response.status).json(response.data);
    } catch (error: any) {
        // Cleanup file in case of error
        if (file) fs.unlinkSync(file.path);

        res.status(400).json({ error: error.response?.data?.message });
    }
});

toolRouter.post('/edit/:id', checkAuthMiddleware, async(req, res) => {
    try {
        const response = await axios.post(`${process.env.PROJECTS_API}/api/tool/edit/${req.params.id}`, req.body);
        res.json(response.data);
    } catch (error: any) {
        res.status(400).json({ error: error.response?.data?.message });
    }
});

toolRouter.delete('/:projectId/:toolId', checkAuthMiddleware, async(req, res) => {
    try {
        const response = await axios.delete(`${process.env.PROJECTS_API}/api/tool/${req.params.projectId}/${req.params.toolId}`);
        res.json(response.data);
    } catch (error: any) {
        res.status(400).json({ error: error.response?.data?.message });
    }
});

export default toolRouter;