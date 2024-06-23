CC = gcc

CFLAGS = #-Wall -Wextra -g

SERVER_TARGET = server
CLIENT_TARGET = client

OBJ = objects

.PHONY: all
all: $(SERVER_TARGET) $(CLIENT_TARGET)

.PHONY: $(SERVER_TARGET)
$(SERVER_TARGET): create_obj_fldr
	$(CC) $(CFLAGS) $(SERVER_TARGET).c -o $(OBJ)/$(SERVER_TARGET)

.PHONY: $(CLIENT_TARGET)
$(CLIENT_TARGET): create_obj_fldr
	$(CC) $(CFLAGS) $(CLIENT_TARGET).c -o $(OBJ)/$(CLIENT_TARGET)

.PHONY: run_server
run_server: 
	sudo ./$(OBJ)/$(SERVER_TARGET)

.PHONY: run_client
run_client: 
	./$(OBJ)/$(CLIENT_TARGET)

.PHONY: create_obj_fldr
create_obj_fldr:
	@mkdir -p $(OBJ)

.PHONY: clean
clean:
	rm -f $(OBJ)/*