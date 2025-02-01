# PictuRAS Auto Crop Tool Microservice

## Overview
Este microserviço realiza o recorte automático de imagens com base no conteúdo, removendo bordas ou áreas desnecessárias.

## Features
- Processa pedidos de recorte automático através de uma fila RabbitMQ.
- Suporta integração com o S3 para armazenamento de imagens de entrada e saída.

## Usage
1. Enviar um pedido de recorte automático para a fila RabbitMQ, especificando o URI da imagem de entrada e o URI desejado para a imagem recortada.
2. O serviço processa a imagem, identificando e recortando as áreas desnecessárias.
3. A imagem processada é salva no local especificado no S3.

## Input Parameters
- **inputImageURI**: URI da imagem de entrada no S3 (ex.: `bucketName/imageName.jpg`).
- **outputImageURI**: URI onde a imagem recortada será salva no S3.

## Response
- Retorna um JSON com as seguintes informações:
  - **type**: O tipo do resultado (`image`).
  - **imageURI**: O URI da imagem recortada armazenada no S3.

---

Para mais informações, consulte a documentação oficial.
