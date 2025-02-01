import { Router } from "express";
import { ToolController } from "../controller/Tool";
import { ProjectController } from "../controller/Project";
import { ToolProcedure } from "../db/types";
import { isJSONValid } from "../utils";
import multer from "multer";
import { S3Client, CreateBucketCommand, ListBucketsCommand, PutObjectCommand, ListObjectsCommand, DeleteObjectCommand, GetObjectCommand } from "@aws-sdk/client-s3";
import fs from "fs";
import { bucketName, s3Client } from "./image";

const toolRouter = Router();
const toolController = new ToolController();
const projectController = new ProjectController();

const upload = multer({ dest: "uploads/" });

// Add tool
toolRouter.post("/:id", upload.single("file"), async (req, res, next) => {
    const projectId = req.params.id
    const { procedure, parameters } = req.body

    // Validate fields
    console.log(procedure)
    console.log(parameters)
    
    if (!procedure || !parameters || !Object.values(ToolProcedure).includes(procedure) || !isJSONValid(parameters, procedure)) {
        console.log("INSIDE")
        res.status(400).json({ message: "Missing required fields" });
        return;
    }

    try{
        const project = await projectController.getToolByProject(projectId);
        if(!project){
            res.status(400).json({ message: "Project not found" });
            return;
        }
        console.log("INSIDE")
        const jsonParams = JSON.parse(parameters)

        if(procedure === ToolProcedure.WATERMARK){
            const file = req.file
            if (!file) {
                res.status(400).json({ message: "No files uploaded." });
                return;
            }

            const imageName = project.id + "/aux/" + file.originalname;
            
            // Upload the file to MinIO
            const fileStream = fs.createReadStream(file.path);
            const uploadCommand = new PutObjectCommand({
                Bucket: bucketName,
                Key: imageName,
                Body: fileStream,
                ContentType: file.mimetype,
            });
            await s3Client.send(uploadCommand);

            jsonParams.watermarkImageURI = bucketName+"/"+imageName
        }

        const length = project.tools.length;
        const tool = await toolController.save({
            projectId: projectId,
            position: length+1,
            procedure: procedure,
            parameters: jsonParams
        })

        res.status(200).json({ toolId: tool.identifiers[0].id }); 
    }
    catch (e) {
        res.status(400).json({ message: "Error adding tool" });
        return;
    }
});


// Edit tool
toolRouter.post("/edit/:id", async (req, res, next) => {
    const projectId = req.params.id
    const { toolId, parameters } = req.body

    // Validate fields
    if (!toolId || typeof toolId !== "string" ) {
        res.status(400).json({ message: "Missing required fields" });
        return;
    }

    try{
        const project = await projectController.getToolByProject(projectId);
        if(!project){
            res.status(400).json({ message: "Project not found" });
            return;
        }

        const tool = await toolController.one(toolId);
        if(!tool){
            res.status(400).json({ message: "Tool not exists"})
            return;
        }

        const procedure = tool.procedure;

        // Validate fields
        if (!parameters || !isJSONValid(parameters, procedure)) {
            res.status(400).json({ message: "Missing required fields" });
            return;
        }

        await toolController.update(toolId, {
            parameters: JSON.parse(parameters)
        })

        res.status(200).json({ message: "Tool updated" }); 
    }
    catch (e) {
        res.status(400).json({ message: "Error updating tool" });
        return;
    }
});


toolRouter.delete("/:projectId/:toolId", async (req, res, next) => {
    const projectId = req.params.projectId
    const toolId = req.params.toolId
    
    try{
        const project = await projectController.getToolByProject(projectId);
        if(!project){
            res.status(400).json({ message: "Project not found" });
            return;
        }

        const tool = await toolController.one(toolId);
        if(!tool){
            res.status(400).json({ message: "Tool not found" });
            return
        }
        await toolController.remove(toolId);
        
        for(const toolAux of project.tools){
            if(toolAux.position > tool.position){
                await toolController.update(toolAux.id,{
                    position : toolAux.position - 1
                })
            } 
        }

        res.status(200).json({ message: "Tool removed" }); 
    }
    catch (e) {
        res.status(400).json({ message: "Error removing tool" });
        return;
    }
});

export default toolRouter;