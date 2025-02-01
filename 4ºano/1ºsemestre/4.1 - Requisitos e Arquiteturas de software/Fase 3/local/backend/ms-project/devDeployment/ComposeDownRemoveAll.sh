#!/bin/bash
# Description: Clean up all Docker containers, images, and volumes

docker compose down --rmi all --volumes --remove-orphans
docker compose down --rmi all --volumes --remove-orphans
rm images/out/* -f