# PictuRAS Contrast Tool Microservice

## Overview
This microservice adjusts the contrast of images as part of the PictuRAS ecosystem.

## Features
- Processes contrast adjustment requests via a message queue.
- Supports fine-tuned contrast parameters.

## Usage
1. Send a contrast adjustment request via the RabbitMQ queue.
2. Retrieve the processed image from the output queue.

---
