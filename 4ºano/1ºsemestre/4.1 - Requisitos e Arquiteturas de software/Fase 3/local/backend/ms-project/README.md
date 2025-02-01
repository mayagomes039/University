# PictuRAS Backend Documentation

## Before starting
>Run Common/Distribute.sh to update common files on folders

The **PictuRAS Backend** implements various tools as microservices to process image-related tasks asynchronously, leveraging a modular and scalable architecture.

> **Base Image and Metrics:**  
> [![Docker Pulls](https://img.shields.io/docker/pulls/prcsousa/picturas-watermark-tool-ms)](https://hub.docker.com/r/prcsousa/picturas-watermark-tool-ms)

## Overview

This project processes image requests asynchronously using a **message queue** architecture. It is built around three primary components:

1. **RabbitMQ Message Broker:**  
   Manages the queuing and handling of image processing requests and responses.

2. **Request Mocker:**  
   Publishes mock requests to the message queue for testing and demonstration purposes.

3. **Tool Microservices:**  
   Specialized services that process image-related tasks (e.g., watermarking, scaling) and publish the results back to the queue.

> **Note:** A `request mocker` is provided in the `usage_example` directory to demonstrate the system's capabilities.

---

## Project Structure

### 1. **Microservice Tool Directories**

Each microservice is dedicated to a specific image processing task. The current tools included are:

- `picturas_watermark_tool_ms`: Applies watermarks to images.
- `picturas_border_tool_ms`: Adds borders to images.
- `picturas_contrast_tool_ms`: Adjusts image contrast.
- `picturas_brightness_tool_ms`: Modifies image brightness.
- `picturas_extractText_tool_ms`: Extracts text from images.
- `picturas_rotate_tool_ms`: Rotates images to the desired angle.
- `picturas_scale_tool_ms`: Resizes images.

---

### 2. **Generic Microservice Internal Structure**

Each microservice follows a standardized structure, ensuring consistency and ease of development. For example, the `picturas_watermark_tool_ms` includes:

- **`main.py`**  
  The main entry point of the microservice. Initializes the service, connects to the message queue, and begins processing requests.
  
- **`config.py`**  
  Contains configuration parameters such as message queue endpoints and tool-specific settings (e.g., watermark image path).
  
- **`<tool-specific>.py` (e.g., `watermark_tool.py`)**  
  Implements the core logic for the specific tool, extending reusable core components.

- **Message Definition Modules:**  
  - `watermark_request_message.py`: Defines the format and parameters of watermark requests.  
  - `watermark_result_message.py`: Defines the structure of the results returned after processing.

---

### 3. **Core Components (`core/`)**

This folder houses reusable modules designed to support multiple tools within the **PictuRAS** ecosystem. These components abstract common functionalities, such as:

- Message queue handling.
- Image processing utilities.
- Error handling and logging.

Using these core components, developers can accelerate the creation of new tools and maintain consistency across services.

---

### 4. **Usage Example (`usage_example/`)**

The `usage_example` directory contains a demonstration setup showcasing the watermarking microservice. It includes:

- Mock requests that simulate real-world use cases.
- Sample configurations for testing and understanding the system workflow.

---

## Extending the Ecosystem

To create a new tool microservice, follow these steps:

1. Clone one of the tools' repositories as a starting template.
2. Implement the specific **Tool Logic** by creating:
   - A `Tool` class for the image processing logic.
   - A `RequestMessage` class to define input parameters.
   - A `ResultMessage` class to format and publish the output.
3. Integrate with the `core/` components for message handling and shared functionality.

---

This modular approach ensures a robust, scalable, and extensible framework for developing image processing tools within the **PictuRAS** ecosystem.

---

# How to Run

## Requirements

- [Python](https://www.python.org/downloads/)
- [Poetry](https://python-poetry.org/docs/#installing-with-the-official-installer)
- [RabbitMQ](https://www.rabbitmq.com/tutorials)
- [Docker](https://docs.docker.com/engine/install/)
- [MinIO](https://github.com/minio/minio?tab=readme-ov-file)
- [boto3](https://boto3.amazonaws.com/v1/documentation/api/latest/index.html)

## Running _Usage Example_

Follow the steps below to run the usage example for the watermarking microservice:

1. Navigate to `usage_example` directory:

   ```bash
   cd usage_example
   ```

2. Start the services using Docker Compose:
   ```bash
   docker compose up
   ```

This will start the various tool microservices along with the message-broker and the request mocker.
The request mocker will send sample tools requests using the images provided in the `./images/src/` folder, and the processed images will be saved in the `./images/out/` folder.

For further customization or testing, you can modify the request mocker script, or replace the sample images in the `./images/src/` folder.

## Development

### 1. Run RabbitMQ (message-broker)

This tool relies on RabbitMQ to subscribe and publish messages, so we need it running.
One way of doing it is using Docker:

```bash
docker run -p 5672:5672 -p 15672:15672 rabbitmq:4-management-alpine
```

> Ports `5672` and `15672` are exposed to host so you can:
>
> 1. connect your local publishers and subscribers using `localhost` and port `5672`
> 2. access RabbitMQ dashboard from your browser at http://localhost:15672 (default credentials: `guest` / `guest`)

### 2. Install dependencies

```bash
poetry install
```

### 3. Run both _Request mocker_ and _Watermark tool_

1. Start request mocker

```bash
source $(poetry env info --path)/bin/activate
python -m usage_example.request_mocker.main
```

2. Run watermark tool in another bash

```bash
source $(poetry env info --path)/bin/activate
python -m picturas_watermark_tool_ms.main
```

> Tip: if you use VSCode you'll find some handy configurations on "Run and Debug" section

## Environment variables

The application uses several environment variables for configuration. Below is a summary table detailing each variable, its purpose, and default value.

| **Environment Variable**        | **Description**                                | **Default Value**            |
| ------------------------------- | ---------------------------------------------- | ---------------------------- |
| `RABBITMQ_HOST`                 | The hostname of the RabbitMQ server.           | `localhost`                  |
| `RABBITMQ_PORT`                 | The port number for the RabbitMQ server.       | `5672`                       |
| `RABBITMQ_USER`                 | The username for authenticating with RabbitMQ. | `guest`                      |
| `RABBITMQ_PASS`                 | The password for authenticating with RabbitMQ. | `guest`                      |
| `RABBITMQ_REQUESTS_QUEUE_NAME`  | The RabbitMQ queue name for requests.          | `watermark-requests`         |
| `RABBITMQ_RESULTS_EXCHANGE`     | The RabbitMQ exchange name to publish results. | `picturas.tools`             |
| `RABBITMQ_RESULTS_ROUTING_KEY`  | The RabbitMQ routing key to publish results.   | `results`                    |
| `PICTURAS_LOG_LEVEL`            | The logging level for the application.         | `INFO`                       |
| `PICTURAS_MS_NAME`              | The name of the microservice instance.         | `picturas-watermark-tool-ms` |
| `PICTURAS_NUM_THREADS`          | The number of threads used by the application. | `4`                          |
| `PICTURAS_WATERMARK_IMAGE_PATH` | The file path to the watermark image.          | `./watermark.png`            |

### Notes

- **Custom Configuration**: All variables can be overridden by setting them in the environment.
- **Defaults**: If a variable is not set, the default value will be used as indicated in the table.
- **Critical Variables**: Ensure `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`, and `RABBITMQ_PASS` are correctly configured for RabbitMQ connectivity.
