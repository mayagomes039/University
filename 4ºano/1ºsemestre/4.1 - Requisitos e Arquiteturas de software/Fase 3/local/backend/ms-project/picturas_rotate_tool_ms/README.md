# PictuRAS Rotate Tool Microservice

## Overview
This microservice rotates images to the desired angle as part of the PictuRAS ecosystem.

## Features
- Processes rotation requests via a message queue.
- Supports arbitrary rotation angles.

## Usage
1. Send a rotation request via the RabbitMQ queue.
2. Retrieve the processed image from the output queue.

---
