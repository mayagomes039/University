import amqp from "amqplib";
import { v4 as uuidv4 } from "uuid";
import { ToolController } from "../controller/Tool";
import { ImageController } from "../controller/Image";
import { bucketName } from "../router/image";
import axios from "axios";
import { ProjectController } from "../controller/Project";

const RABBITMQ_URL =  process.env.RABBITMQ_URL  || "amqp://guest:guest@localhost:5672"; // User, password, host, and port
const EXCHANGE_NAME = process.env.EXCHANGE_NAME || "picturas.tools"; // Correct exchange name
const RESULTS_QUEUE = process.env.RESULTS_QUEUE || "results";
let channel: amqp.Channel;

// Conectar ao RabbitMQ
export async function connectToRabbitMQ() {
    try {
      const connection = await amqp.connect(RABBITMQ_URL);
      channel = await connection.createChannel();
      await channel.prefetch(10)
  
      // Garantir que a exchange existe
      await channel.assertExchange(EXCHANGE_NAME, "direct", { durable: true });
      console.log("Conectado ao RabbitMQ e exchange configurada.");
    } catch (error) {
      console.error("Erro ao conectar ao RabbitMQ:", error);
      process.exit(1);
    }
}


// Função para enviar mensagens ao RabbitMQ
export async function sendToRabbitMQ(procedure: string, parameters: any, routingKey: string, toolId: string, imageId: string) {
  if (!procedure || !parameters || !routingKey || !toolId || !imageId) {
    console.error("Erro: Campos 'procedure', 'parameters', 'routingKey', 'toolId' e 'imageId' são obrigatórios.");
    return;
  }

  // Gerar os campos obrigatórios
  const messageId = uuidv4(); // ID único para a mensagem
  const timestamp = new Date().toISOString(); // Timestamp no formato ISO 8601

  try {
    // Estruturar o payload
    const payload = {
      messageId,
      timestamp,
      procedure,
      parameters,
      toolId,
      imageId
    };

    // Publicar mensagem na exchange
    channel.publish(
      EXCHANGE_NAME,
      routingKey,
      Buffer.from(JSON.stringify(payload))
    );

    console.log(`Mensagem publicada:`, payload);
  } catch (error) {
    console.error("Erro ao publicar no RabbitMQ:", error);
  }
}

// Listen to the results queue
export async function listenToResultsQueue() {
  const toolController = new ToolController();
  const imageController = new ImageController()
  const projectController = new ProjectController()

  try {
    console.log(`Listening to queue: ${RESULTS_QUEUE}`);

    channel.consume(
      RESULTS_QUEUE,
      async (message) => {
        if (message) {
          const content = message.content.toString();

          try {
            const parsedMessage = JSON.parse(content);

            const { imageId, toolId, status, output, error } = parsedMessage
            console.log({
              imageId,
              toolId,
              status,
              output,
              error
            })

            const tool = await toolController.one(toolId)
            if(!tool){
              console.warn("TOOL DOESNT EXIST")
              return
            }

            const image = await imageController.one(imageId)
            if(!image){
              console.warn("IMAGE DOESNT EXIST")
              return
            }


            const project = await projectController.one(image.projectId)
            if(!project){
              console.warn("PROJECT DOESNT EXIST")
              return
            }


            if(status !== "success"){
              console.warn("TASK FAILED")
              console.warn(error.description) 
              // SEND WEBSOCKET
              await sendWebSocket(project.userId, {
                imageId: imageId,
                projectId: image.projectId,
                status: "failed",
                error: error.description
              })
              return
            }

            const nextTool = await toolController.oneWherePosition(tool.projectId, tool.position+1)

            if(!nextTool){
              const finalImage = await imageController.save({
                projectId: image.projectId,
                uri: output.imageURI,
                isFinal: true
              })


              // SEND WEBSOCKET
              await sendWebSocket(project.userId, {
                imageId: imageId,
                finalImageId: finalImage.identifiers[0].id,
                projectId: image.projectId,
                status: "finished",
                finalImageURI: output.imageURI
              })

              return
            }
            console.log("TASKS TO DO")
            

            const procedure = nextTool.procedure
            const parameters = {
                ...nextTool.parameters,
                inputImageURI: output.imageURI,
                outputImageURI: output.imageURI.replace(`${bucketName}/`, "")
            }
            const routingKey = "requests." + procedure
            
            await sendToRabbitMQ(procedure, parameters, routingKey, nextTool.id, image.id)
            

            
          } catch (error) {
              console.error("Error parsing message content:", error);
          }
          finally{
            // Acknowledge the message
            channel.ack(message);
          }
        }
      },
      { noAck: false } // Ensure messages are acknowledged
    );
  } catch (error) {
    console.error("Error listening to queue:", error);
  }
}


async function sendWebSocket(userId:string, data: {imageId: string, projectId: string, status: string, finalImageId?: string, finalImageURI?: string, error?: string}) {
  try{
    const info = await axios.post(process.env.WS_API + userId, { data })
    console.log(info.status) 
  }
  catch(e){
    console.log("Error", e)
  }

}