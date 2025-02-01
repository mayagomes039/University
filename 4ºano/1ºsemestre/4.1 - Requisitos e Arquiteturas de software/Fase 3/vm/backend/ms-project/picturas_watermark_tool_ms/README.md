# PictuRAS Watermark Tool Microservice

## Overview
This microservice applies watermarks to images as part of the PictuRAS ecosystem.

## Features
- Processes watermarking requests via a message queue.
- Supports configurable watermark images and positioning.

## Usage
1. Send a watermarking request via the RabbitMQ queue.
2. Retrieve the processed image from the output queue.

---
