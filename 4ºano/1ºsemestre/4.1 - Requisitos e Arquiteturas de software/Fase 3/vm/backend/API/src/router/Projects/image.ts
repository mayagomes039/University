import { Router } from "express";
import axios from "axios";
import multer from "multer";
import FormData from "form-data";
import fs from "fs";
import { checkAuthMiddleware } from "../../middleware";

const imageRouter = Router();

const upload = multer({ dest: "uploads/" });

imageRouter.get('/:id', checkAuthMiddleware, async(req, res) => {
    try {
        const response = await axios.get(`${process.env.PROJECTS_API}/api/image/${req.params.id}`);
        res.json(response.data);
    } catch (error: any) {
        res.status(400).json({ error: error.response?.data?.message });
    }
});

imageRouter.post("/:id", checkAuthMiddleware, upload.array("file", 10), async (req, res) => {
    const files = req.files;

    try {
        const formData = new FormData();

        // Add uploaded files to FormData
        if (files && Array.isArray(files)) {
            for (const file of files) {
                formData.append("file", fs.createReadStream(file.path), file.originalname);
            }
        }

        // Add other request body fields to FormData
        Object.keys(req.body).forEach((key) => {
            formData.append(key, req.body[key]);
        });

        // Forward the request to the backend service
        const response = await axios.post(`${process.env.PROJECTS_API}/api/image/${req.params.id}`, formData, {
            headers: {
                ...formData.getHeaders(), // Include multipart headers
            },
        });

        // Clean up temporary files from the Gateway
        if (files && Array.isArray(files)) {
            for (const file of files) {
                fs.unlinkSync(file.path);
            }
        }

        // Send back the response from the backend
        res.status(response.status).json(response.data);
    } catch (error: any) {
        // Cleanup files in case of error
        if (files && Array.isArray(files)) {
            for (const file of files) {
                fs.unlinkSync(file.path);
            }
        }
        res.status(400).json({ error: error.response?.data?.message });
    }
});

imageRouter.post("/zip/:id", checkAuthMiddleware, upload.single("file"), async (req, res) => {
    const file = req.file;

    try {
        // Validate the uploaded file
        if (!file || (file.mimetype !== "application/zip" && file.mimetype !== "application/x-zip-compressed")) {
            res.status(400).json({ message: "Please upload a valid .zip file." });
            return;
        }

        // Prepare the form data
        const formData = new FormData();
        formData.append("file", fs.createReadStream(file.path), file.originalname);

        // Forward the request to the backend service
        const response = await axios.post(`${process.env.PROJECTS_API}/api/image/zip/${req.params.id}`, formData, {
            headers: {
                ...formData.getHeaders(), // Include multipart headers
            },
        });

        // Clean up the uploaded file from the Gateway's storage
        fs.unlinkSync(file.path);

        // Send back the response from the backend
        res.status(response.status).json(response.data);
    } catch (error: any) {
        // Cleanup in case of error
        if (file) {
            fs.unlinkSync(file.path);
        }
        res.status(400).json({ error: error.response?.data?.message });
    }
});

imageRouter.delete('/:id/:imageId', checkAuthMiddleware, async(req, res) => {
    try {
        const response = await axios.delete(`${process.env.PROJECTS_API}/api/image/${req.params.id}/${req.params.imageId}`, req.body);
        res.json(response.data);
    } catch (error: any) {
        res.status(400).json({ error: error.response?.data?.message });
    }
});

imageRouter.post("/download/:id", checkAuthMiddleware, async (req, res) => {
    try {
        const response = await axios.post(
            `${process.env.PROJECTS_API}/api/image/download/${req.params.id}`,
            null,
            { responseType: "arraybuffer" } // Define o tipo de resposta como binário
        );

        res.setHeader("Content-Type", "application/zip"); // Define o tipo MIME adequado
        res.setHeader("Content-Disposition", `attachment; filename="${req.params.id}.zip"`); // Sugere o nome do ficheiro

        res.send(response.data); // Envia os dados binários diretamente
    } catch (error: any) {
        res.status(400).json({ error: error.response?.data?.message || error.message });
    }
});

export default imageRouter;