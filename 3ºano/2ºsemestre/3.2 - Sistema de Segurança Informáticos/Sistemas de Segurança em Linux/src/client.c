#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <fcntl.h>
#include <sys/stat.h>

#define MAX_MESSAGE_LEN 512

char fifoCLIENT_path[100];

void sigint_handler(int sig) {
    if (fifoCLIENT_path[0] != '\0' && unlink(fifoCLIENT_path) == -1) {
        perror("unlink");
        exit(EXIT_FAILURE);
    }
    exit(EXIT_SUCCESS);
}


int main() {
    if (signal(SIGINT, sigint_handler) == SIG_ERR) {
        perror("signal");
        exit(EXIT_FAILURE);
    }

    char option[50];
    for (int i = 0; i < 50; i++) {
        option[i] = '\0';
    }

    while (strcmp(option, "1") != 0 && strcmp(option, "2") != 0) {
        printf("Choose an option:\n1 - Login\n2 - Register\n");
        fgets(option, sizeof(option), stdin);
        option[strcspn(option, "\n")] = '\0';
    }


    char username[50];
    for (int i = 0; i < 50; i++) {
        username[i] = '\0';
    }
    char *fifoSERVER_path = "fifos/servidor";
    for (int i = 0; i < 100; i++) {
        fifoCLIENT_path[i] = '\0';
    }

    printf("Enter your username: ");
    fgets(username, sizeof(username), stdin);
    username[strcspn(username, "\n")] = '\0';

    sprintf(fifoCLIENT_path, "fifos/%s", username);

    int fdSERVER = open(fifoSERVER_path, O_WRONLY, 0666);
    if (fdSERVER == -1) {
        perror("open fdSERVER");
        return 1;
    }
    char *login_request = malloc(strlen(username)+17);
    if (strcmp(option, "1") == 0) {
        sprintf(login_request, "%s/concordia-login", username);
    } else {
        sprintf(login_request, "%s/concordia-ativar", username);
    }
    printf("%s\n", login_request);
    write(fdSERVER, login_request, strlen(login_request)+1);
    close(fdSERVER);

    mkfifo(fifoCLIENT_path, 0666);

    int fdCLIENT = open(fifoCLIENT_path, O_RDONLY, 0666);
    if (fdCLIENT == -1) {
        perror("open fdCLIENT");
        return 1;
    }
    char loginAccepted[MAX_MESSAGE_LEN];
    read(fdCLIENT, loginAccepted, MAX_MESSAGE_LEN);
    close(fdCLIENT);
    // Print the read message
    printf("Answer: %s\n", loginAccepted);

    if (strcmp(option, "2") == 0 && strcmp(loginAccepted, "User adicionado com sucesso.\n") != 0) {
        return 0;
    }

    if (strcmp(loginAccepted, "User não existe!") == 0) {
        if (unlink(fifoCLIENT_path) == -1) {
            perror("unlink");
            exit(EXIT_FAILURE);
        }
        return 0;
    }

    while (1)
    {
        char message[MAX_MESSAGE_LEN];
        for (int i = 0; i < MAX_MESSAGE_LEN; i++) {
            message[i] = '\0';
        }
        printf("Enter your command: ");

        fgets(message, sizeof(message), stdin);

        fdSERVER = open(fifoSERVER_path, O_WRONLY, 0666);
        if (fdSERVER == -1) {
            perror("open fdSERVER");
            return 1;
        }
        char query[MAX_MESSAGE_LEN + strlen(username) + 1];
        sprintf(query, "%s/%s", username, message);
        query[strlen(query)-1] = '\0';
        write(fdSERVER, query, strlen(query)+1);
        close(fdSERVER);


        fdCLIENT = open(fifoCLIENT_path, O_RDONLY, 0666);
        if (fdCLIENT == -1) {
            perror("open fdCLIENT");
            return 1;
        }
        char answer[MAX_MESSAGE_LEN];
        read(fdSERVER, answer, MAX_MESSAGE_LEN);
        printf("Answer: %s\n", answer);

        if(strcmp(query, "concordia-listar -a") == 0){
            while (strcmp(answer, "Fim") != 0) {
                read(fdSERVER, answer, MAX_MESSAGE_LEN);
                printf("%s\n", answer);
            }
        }
        close(fdCLIENT);
    }
    return 0;
}