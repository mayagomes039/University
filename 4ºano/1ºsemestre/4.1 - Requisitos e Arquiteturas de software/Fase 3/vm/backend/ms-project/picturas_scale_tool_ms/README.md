## PictuRAS Scale Tool Microservice

## Overview
This microservice resizes images as part of the PictuRAS ecosystem.

## Features
- Processes scaling requests via a message queue.
- Supports custom dimensions and aspect ratio preservation.

## Usage
1. Send a scaling request via the RabbitMQ queue.
2. Retrieve the processed image from the output queue.

---
