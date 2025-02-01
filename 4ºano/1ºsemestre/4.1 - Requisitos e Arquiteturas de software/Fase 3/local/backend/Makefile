# Define relative paths for your directories
DIR1 = ./Projects
DIR2 = ./Subscriptions
DIR3 = ./Users

# Start docker compose up for each directory
du:
	docker-compose -f $(DIR1)/docker-compose.yml up -d --remove-orphans
	docker-compose -f $(DIR2)/docker-compose.yml up -d --remove-orphans
	docker-compose -f $(DIR3)/docker-compose.yml up -d --remove-orphans

# Stop docker compose down for each directory
dd:
	docker-compose -f $(DIR1)/docker-compose.yml down --remove-orphans
	docker-compose -f $(DIR2)/docker-compose.yml down --remove-orphans
	docker-compose -f $(DIR3)/docker-compose.yml down --remove-orphans
