# PictuRAS Brightness Tool Microservice

## Overview
This microservice modifies the brightness of images as part of the PictuRAS ecosystem.

## Features
- Processes brightness modification requests via a message queue.
- Supports customizable brightness levels.

## Usage
1. Send a brightness adjustment request via the RabbitMQ queue.
2. Retrieve the processed image from the output queue.

---
