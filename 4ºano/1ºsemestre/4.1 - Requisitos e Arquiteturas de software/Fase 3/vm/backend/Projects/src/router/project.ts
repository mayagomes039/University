import { Router } from "express";
import { ProjectController } from "../controller/Project";
import axios from "axios";
import { AvailableToolsPerUser, ToolProcedure, UserType } from "../db/types";
import { S3Client, CreateBucketCommand, ListBucketsCommand, PutObjectCommand, ListObjectsCommand, DeleteObjectCommand, GetObjectCommand } from "@aws-sdk/client-s3";
import { sendToRabbitMQ } from "../utils/rabbitMq";
import { bucketName, s3Client } from "./image";
import { ImageController } from "../controller/Image";
import { ToolController } from "../controller/Tool";

const projectRouter = Router();
const projectController = new ProjectController()
const imageController = new ImageController()
const toolController = new ToolController()

// Get project
projectRouter.get("/:id", async (req, res, next) => {
    const projectId = req.params.id
    try{
        const project = await projectController.oneProjectFullInformation(projectId)
        if(!project){
            res.status(400).json({ message: "Project doesn't exist" });
            return;
        }

        const user = await axios.get(process.env.USER_API + project.userId)
        if (user.status !== 200 || !user.data) {
            res.status(400).json({ message: "User doesn't exist" });
            return;
        }
        
        const userType = user.data.type;

        function getToolsForUser(userType: UserType): readonly ToolProcedure[] {
            return AvailableToolsPerUser[userType];
        }
        
        const availableTools = getToolsForUser(userType);
        res.status(200).json({
            project: project,
            availableTools: availableTools
        });
    }
    catch (e) {
        res.status(400).json({ message: "Error getting project" });
        return;
    }
});


// List all projects
projectRouter.get("/user/:id", async (req, res, next) => {
    const userId = req.params.id
    try{
        const allProjects = await projectController.allProjectsByUser(userId)
        res.status(200).json(allProjects);
    }
    catch (e) {
        res.status(400).json({ message: "Error listing user projects" });
        return;
    }
});


// Add a project
projectRouter.post("/", async (req, res, next) => {
    const { userId, name } = req.body

    // Validate fields
    if (!userId || !name || typeof userId !== "string" || typeof name !== "string") {
        res.status(400).json({ message: "Missing required fields" });
        return;
    }
        
    try{
        const userProject = await projectController.oneByUserId(userId)
        if(!userProject){
            const user = await axios.get(process.env.USER_API + userId)
            if (user.status !== 200 || !user.data) {
                res.status(400).json({ message: "User doesn't exist" });
                return;
            }
        }

        const newProject = await projectController.save({
            userId: userId,
            name: name
        })

        res.status(200).json({ projectId: newProject.identifiers[0].id });
    }
    catch (e) {
        res.status(400).json({ message: "Error adding user project" });
        return;
    }
});


// Remove project
projectRouter.delete("/:id", async (req, res, next) => {
    const projectId = req.params.id
    try{
        const project = await projectController.oneProjectFullInformation(projectId)
        if(!project){
            res.status(400).json({ message: "Project doesn't exist" });
            return;
        }

        for(const image of project.images){
            const imageName = image.uri.split('/').slice(1).join('/');
        
            const deleteCommand = new DeleteObjectCommand({
                Bucket: bucketName,
                Key: imageName,
            });
            await s3Client.send(deleteCommand);
        }
        
        await toolController.removeByProject(projectId)
        await imageController.removeByProject(projectId)
        await projectController.remove(projectId)
        res.status(200).json({ message: "Project deleted successfully" });
    }
    catch (e) {
        res.status(400).json({ message: "Error deleted project" });
        return;
    }
});


// Remove project
projectRouter.post("/applyTools/:id", async (req, res, next) => {
    const projectId = req.params.id;
    const { toolIds } = req.body;
  
    if (!Array.isArray(toolIds) || toolIds.length === 0 || !toolIds.every(id => typeof id === 'string')) {
      res.status(400).json({ message: "Invalid or missing toolIds. It must be a non-empty array of strings." });
      return;
    }
  
    try {
      await imageController.removeByFlag(projectId);

      const project = await projectController.oneProjectFullInformation(projectId);
      if (!project) {
        res.status(400).json({ message: "Project doesn't exist" });
        return;
      }
  
      if (project.images.length === 0) {
        res.status(400).json({ message: "Project doesn't have images" });
        return;
      }
  
      for (let i = 0; i < toolIds.length; i++) {
        const toolId = toolIds[i];
  
        const tool = project.tools.find(t => t.id === toolId);
        if (!tool) {
          res.status(400).json({ message: `Tool with id ${toolId} not found in project.` });
          return;
        }

        await toolController.update(toolId, {position: i + 1});
      }
  
      // Recuperar a ferramenta inicial com a posição 1
      const firstTool = project.tools.find(tool => tool.position === 1);
      if (!firstTool) {
        res.status(400).json({ message: "First tool not found" });
        return;
      }
  
      // Enviar as imagens do projeto para a fila RabbitMQ usando a primeira ferramenta
      for (const image of project.images) {
        const procedure = firstTool.procedure;
        const parameters = {
          ...firstTool.parameters,
          inputImageURI: image.uri,
          outputImageURI: image.uri.replace("/in/", "/out/").replace(`${bucketName}/`, ""),
        };
        const routingKey = "requests." + procedure;
  
        sendToRabbitMQ(procedure, parameters, routingKey, firstTool.id, image.id);
      }
  
      res.status(200).json({ message: "Tool positions updated and images sent to queue" });
    } catch (e) {
      console.error(e);
      res.status(500).json({ message: "Error applying tools and updating positions." });
    }
});

export default projectRouter;