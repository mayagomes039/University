import { Router } from "express";
import { ImageController } from "../controller/Image";
import multer from "multer";
import { S3Client, CreateBucketCommand, ListBucketsCommand, PutObjectCommand, ListObjectsCommand, DeleteObjectCommand, GetObjectCommand } from "@aws-sdk/client-s3";
import fs from "fs";
import { ProjectController } from "../controller/Project";
import AdmZip from "adm-zip"
import path from "path";
import archiver from "archiver";
import { Readable } from 'stream';

const imageRouter = Router();
const imageController = new ImageController()
const projectController = new ProjectController()

// Configuração do Multer
const upload = multer({ dest: "uploads/" });

export const s3Client = new S3Client({
  endpoint: process.env.S3_ENDPOINT_URL || "http://localhost:9000", // URL do MinIO
  credentials: {
    accessKeyId: process.env.S3_ACCESS_KEY || "root",
    secretAccessKey: process.env.S3_SECRET_KEY || "password1234",
  },
  forcePathStyle: true, // Força o estilo de path para S3 compatível
  region: "us-east-1", // Região arbitrária
});
export const bucketName = "my-bucket";




// Get image
imageRouter.get("/:id", async (req, res, next) => {
    const imageId = req.params.id
    try{
        const image = await imageController.one(imageId)
        if(!image){
            res.status(400).json({ message: "Image doesn't exist" });
            return;
        }
        res.status(200).json(image);
    }
    catch (e) {
        res.status(400).json({ message: "Error getting image" });
        return;
    }
});





// Upload an image or multiple images
imageRouter.post("/:id", upload.array("file", 10), async (req, res) => {
  const files = req.files;

  // Check if 'files' is an array, since it could be either an array or an object
  if (!files || (Array.isArray(files) && files.length === 0)) {
    res.status(400).json({ message: "No files uploaded." });
    return;
  }

  const allowedMimeTypes = [
    "image/jpeg",
    "image/jpg",
    "image/png",
    "image/gif",
    "image/bmp",
    "image/webp",
  ];

  try {
    const project = await projectController.oneProjectFullInformation(req.params.id);
    if (!project) {
      res.status(400).json({ message: "Project doesn't exist" });
      return;
    }

    // If 'files' is an array, proceed with file processing
    if (Array.isArray(files)) {

      for (const file of files) {
        if (!allowedMimeTypes.includes(file.mimetype)) {
          res.status(400).json({ message: `Invalid file type: ${file.originalname}. Only image files are allowed.` });
          return;
        }
      }

      // Check if any of the uploaded files already exist in the project images
      for (const file of files) {
        for (const image of project.images) {
          if (image.uri.includes(file.originalname)) {
            res.status(400).json({ message: `Image with the name ${file.originalname} already exists.` });
            return;
          }
        }
      }

      for (const file of files) {
        const imageName = project.id + "/in/" + file.originalname;

        // Upload the file to MinIO
        const fileStream = fs.createReadStream(file.path);
        const uploadCommand = new PutObjectCommand({
          Bucket: bucketName,
          Key: imageName,
          Body: fileStream,
          ContentType: file.mimetype,
        });

        await s3Client.send(uploadCommand);

        // Delete the file locally after upload
        fs.unlinkSync(file.path);

        // Save image information in the database
        await imageController.save({
          projectId: project.id,
          uri: `${bucketName}/${imageName}`,
        });
      }
    }

    res.status(200).json({ message: "Files uploaded successfully." });
  } catch (error) {
    console.error(error);
    res.status(500).json({ message: "Upload error." });
  }
});
imageRouter.post("/zip/:id", upload.single("file"), async (req, res) => {
  const file = req.file;

  console.log('File received:', req.file);
  console.log('File mimetype:', req.file?.mimetype);
  console.log('File name:', req.file?.originalname);

  // Ensure the file is a .zip file
  if (!file || (file.mimetype !== 'application/zip' && file.mimetype !== 'application/x-zip-compressed')) {
    res.status(400).json({ message: "Please upload a valid .zip file." });
    return;
  }

  try {
    const project = await projectController.oneProjectFullInformation(req.params.id);
    if (!project) {
      res.status(400).json({ message: "Project doesn't exist" });
      return;
    }

    // Extract existing file names from the project images
    const existingFileNames = project.images.map(image => image.uri.split("/").pop());

    // Step 1: Extract the .zip file
    const zip = new AdmZip(file.path);
    const zipEntries = zip.getEntries(); // List of entries in the .zip file

    // Check if all files in the zip are images
    const imageFiles = [];

    for (const entry of zipEntries) {
      if (entry.isDirectory) continue; // Skip directories

      const fileName = entry.entryName;
      const fileExtension = fileName.split('.').pop()?.toLowerCase(); // Optional chaining to safely handle undefined

      // If fileExtension is undefined (i.e., no period in the filename), skip this entry
      if (!fileExtension) {
        res.status(400).json({ message: `Invalid file type: ${fileName}. The file has no extension.` });
        return;
      }

      // Check if the file is an image (you can extend this list of valid extensions)
      if (['jpg', 'jpeg', 'png', 'gif', 'bmp'].includes(fileExtension)) {

        // Check if the file name already exists in the project images
        if (existingFileNames.includes(fileName)) {
          res.status(400).json({ message: `Image with the name ${fileName} already exists in the project.` });
          return;
        }

        imageFiles.push(entry);
      } else {
        res.status(400).json({ message: `Invalid file type: ${fileName}. Only image files are allowed in the zip.` });
        return;
      }
    }

    // If no images found
    if (imageFiles.length === 0) {
      res.status(400).json({ message: "No valid images found in the .zip file." });
      return;
    }

    // Step 2: Upload each image to MinIO and save the image information
    for (const imageFile of imageFiles) {
      const imageName = `${project.id}/in/${imageFile.entryName}`;

      // Extract the image content from the zip entry
      const imageBuffer = imageFile.getData();

      // Upload the image to MinIO
      const uploadCommand = new PutObjectCommand({
        Bucket: bucketName,
        Key: imageName,
        Body: imageBuffer,
        ContentType: `image/${imageFile.entryName.split('.').pop()?.toLowerCase()}`, // Optional chaining for safe handling
      });

      await s3Client.send(uploadCommand);

      // Save image information in the database
      await imageController.save({
        projectId: project.id,
        uri: `${bucketName}/${imageName}`,
      });
    }

    // Step 3: Delete the zip file locally after processing
    fs.unlinkSync(file.path);

    res.status(200).json({ message: "Images extracted and uploaded successfully." });
  } catch (error) {
    console.error(error);
    res.status(500).json({ message: "Error processing the .zip file." });
  }
});





// Delete an image
imageRouter.delete('/:id/:imageId', async (req, res, next) => {
  const { id: projectId, imageId } = req.params;

  try {
    const project = await projectController.oneProjectFullInformation(projectId);
    if (!project) {
      res.status(400).json({ message: "Project doesn't exist" });
      return;
    }

    const image = project.images.find((img) => img.id === imageId);
    if (!image) {
      res.status(400).json({ message: "Image doesn't exist in the project" });
      return;
    }

    const imageName = image.uri.split('/').slice(1).join('/');

    const deleteCommand = new DeleteObjectCommand({
      Bucket: bucketName,
      Key: imageName,
    });
    await s3Client.send(deleteCommand);

    await imageController.remove(imageId);
    res.status(200).json({ message: "Image deleted successfully" });
  } catch (error) {
    res.status(500).json({ message: "Error deleting image" });
  }
});


// Download images
imageRouter.post('/download/:id', async (req, res) => {
  const projectId = req.params.id;

  try {
    const project = await projectController.oneProjectFullInformation(projectId);
    if (!project) {
      res.status(400).json({ message: "Project doesn't exist" });
      return;
    }

    const images = project.images.filter(image => image.isFinal);

    if (!images || images.length === 0) {
      res.status(404).json({ message: "No images found for this project" });
      return;
    }

    const archive = archiver('zip', { zlib: { level: 9 } });
    const zipName = `${project.name}-images.zip`;

    res.setHeader('Content-Type', 'application/zip');
    res.setHeader('Content-Disposition', `attachment; filename="${zipName}"`);

    archive.pipe(res);

    for (const image of images) {

      const getObjectCommand = new GetObjectCommand({
        Bucket: bucketName,
        Key: image.uri.replace(bucketName + "/", ""),
      });

      const imageResponse = await s3Client.send(getObjectCommand);

      if (!imageResponse.Body) {
        console.warn(`Image ${image.uri} not found or invalid`);
        continue;
      }

      const imageName = path.basename(image.uri);

      const stream = imageResponse.Body as Readable;
      archive.append(stream, { name: `${project.name}/${imageName}` });
    }
    await archive.finalize();
  } catch (error) {
    console.error(error);
    res.status(500).json({ message: 'Error downloading images' });
  }
});

export default imageRouter;