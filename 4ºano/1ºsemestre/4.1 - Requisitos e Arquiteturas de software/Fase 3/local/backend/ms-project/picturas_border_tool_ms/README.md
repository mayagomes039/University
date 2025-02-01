# PictuRAS Border Tool Microservice

## Overview
This microservice adds borders to images as part of the PictuRAS ecosystem.

## Features
- Processes border addition requests via a message queue.
- Supports customizable border colors and thickness.

## Usage
1. Send a border request via the RabbitMQ queue.
2. Retrieve the processed image from the output queue.

---
