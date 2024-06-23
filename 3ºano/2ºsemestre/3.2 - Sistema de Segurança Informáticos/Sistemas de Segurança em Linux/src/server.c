#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <pthread.h>
#include <sys/types.h>
#include <dirent.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <pwd.h>
#include <sys/ipc.h>
#include <sys/shm.h>
#include <sys/wait.h>
#include <signal.h>
#include <grp.h>


#define MAX_MESSAGE_LEN 512
typedef struct {
    char name[256];
} FileInfo;
#define MAX_GROUP_ID 65535

#define MAX_FILES 1000


void deleteDirectoryRecursively(const char *path) {
    DIR *dir = opendir(path);
    struct dirent *entry;

    while ((entry = readdir(dir)) != NULL) {
        if (strcmp(entry->d_name, ".") != 0 && strcmp(entry->d_name, "..") != 0) {
            char fullPath[1024];
            snprintf(fullPath, sizeof(fullPath), "%s/%s", path, entry->d_name);

            if (entry->d_type == DT_DIR) {
                deleteDirectoryRecursively(fullPath);
            } else {
                remove(fullPath);
            }
        }
    }

    closedir(dir);
    rmdir(path); // Excluir diretório principal após excluir todos os arquivos e subdiretórios
}

void acrescenta_grupo(char *username, char *group_name) {
    char command[256];
    for (int i = 0; i < 256; i++) {
        command[i] = '\0';
    }
    sprintf(command, "dseditgroup -o edit -a %s -t user %s", username, group_name);
    system(command);
}

void retira_grupo(char *username, char *group_name) {
    char command[256];
    for (int i = 0; i < 256; i++) {
        command[i] = '\0';
    }
    sprintf(command, "dseditgroup -o edit -d %s -t user %s", username, group_name);
    system(command);
}

char **get_group_members(const char *group_name) {
    struct group *grp;

    grp = getgrnam(group_name);
    if (grp == NULL) {
        perror("getgrnam");
        return NULL;
    }

    return grp->gr_mem;
}

int string_in_array(const char *str, char **array) {
    for (int i = 0; array[i]; i++) {
        if (strcmp(str, array[i]) == 0) {
            return 1;
        }
    }
    return 0;
}

int compareFileInfo(const void *a, const void *b) {
    if (strlen(((FileInfo *)a)->name) > strlen(((FileInfo *)b)->name)) {
        return -1;
    }
    return strcmp(((FileInfo *)b)->name, ((FileInfo *)a)->name);
}

FileInfo* listFilesDescending(const char *path) {
    DIR *dir;
    struct dirent *ent;
    FileInfo *files = (FileInfo *) malloc(MAX_FILES * sizeof(FileInfo));
    int count = 0;

    if ((dir = opendir(path)) != NULL) {
        while ((ent = readdir(dir)) != NULL) {
            if (ent->d_type == DT_REG) {
                if (strcmp(ent->d_name, "") == 0 || strcmp(ent->d_name, ".") == 0 || strcmp(ent->d_name, "..") == 0){
                    continue;
                }
                strcpy(files[count].name, ent->d_name);
                count++;
            }
        }
        closedir(dir);

        qsort(files, count, sizeof(FileInfo), compareFileInfo);

    } else {
        // Se não for possível abrir o diretório
        perror("Não foi possível abrir o diretório");
    }
    return files;
}

char *removeDepoisDeConteudo(char *str) {
    char *dup = strdup(str);
    char *ptr = strstr(dup, "conteúdo");
    if (ptr != NULL) {
        *ptr = '\0';
    }
    return dup;
}

void sigint_handler(int sig) {
    if (unlink("fifos/servidor") == -1) {
        perror("unlink");
        exit(EXIT_FAILURE);
    }

    kill(0, SIGKILL);

    exit(EXIT_SUCCESS);
}

int getlastIDmessage(const char* caminho) {
    DIR* dir = opendir(caminho);
    if (dir == NULL) {
        perror("Erro ao abrir diretório");
        return -1;
    }

    struct dirent *entry;
    char* ultimo_nome = NULL;

    while ((entry = readdir(dir)) != NULL) {
        if (entry->d_type == DT_REG) {
            if (ultimo_nome == NULL || strlen(entry->d_name) > strlen(ultimo_nome) || strcmp(entry->d_name, ultimo_nome) > 0) {
                if (ultimo_nome != NULL) {
                    free(ultimo_nome);
                }
                ultimo_nome = strdup(entry->d_name);
            }
        }
    }

    closedir(dir);
    if (ultimo_nome == NULL) {
        return 0;
    }
    char *ponto = strrchr(ultimo_nome, '.');
    if (ponto != NULL) {
        *ponto = '\0';
    }
    return atoi(strdup(ultimo_nome));
}

int userLogado(char *username) {
    DIR *dir = opendir("fifos");
    if (dir == NULL) {
        perror("opendir fifos");
        exit(EXIT_FAILURE);
    }

    struct dirent *entry;

    while ((entry = readdir(dir)) != NULL) {
        char path[1024];
        snprintf(path, sizeof(path), "%s/%s", "fifos", entry->d_name);

        struct stat file_stat;
        if (stat(path, &file_stat) == -1) {
            perror("stat");
            exit(EXIT_FAILURE);
        }

        if (S_ISFIFO(file_stat.st_mode) && strcmp(entry->d_name, username) == 0) {
            return 1;
        }
    }

    // Fechar o diretório
    closedir(dir);
    return 0;
}

uid_t get_userUID(char *username) {
    struct passwd *user_info;

    user_info = getpwnam(username);

    if (user_info == NULL) {
        return -1;
    }

    return user_info->pw_uid;
}

int change_username(char* username) {
    uid_t new_uid = get_userUID(username);
    if (setuid(new_uid) == -1) {
        perror("Erro ao mudar de usuário\n");
        return 0;
    }
    return 1;
}

int directory_exists(const char *username) {
    struct stat st = {0};
    char directory_name[50];
    sprintf(directory_name, "./%s", username);
    return stat(directory_name, &st) == 0;
}

int move_message_to_folder(char *recipient, char *message, char *sender, time_t message_time, int message_id) {
    char recipient_folder[100];
    char sender_folder[100];
    sprintf(recipient_folder, "./%s/%sEntrada/", recipient, recipient);
    sprintf(sender_folder, "./%s/%sSaida/", sender, sender);

    struct stat st;
    if (stat(recipient_folder, &st) == -1 || stat(sender_folder, &st) == -1) {
        fprintf(stderr, "Erro: O destinatário não existe.\n");
        return 0;
    }

    int recipient_msg_id = getlastIDmessage(recipient_folder) + 1;
    int sender_msg_id = getlastIDmessage(sender_folder) + 1;
    sprintf(recipient_folder + strlen(recipient_folder), "%d.txt", recipient_msg_id);
    sprintf(sender_folder + strlen(sender_folder), "%d.txt", sender_msg_id);
    char* time = ctime(&message_time);
    time[strlen(time) - 1] = '\0';
    pid_t pid = fork();
    if (pid < 0) {
        perror("fork");
        exit(EXIT_FAILURE);
    } else if (pid == 0) {
        if (change_username(sender)) {
            FILE *arquivo = fopen(sender_folder, "wb");

            if (arquivo == NULL) {
                char sender_file[50];
                perror("Sender File: ");
                return 0;
            }

            char to_write[MAX_MESSAGE_LEN];
            sprintf(to_write, "id: %d; data: %s; destinatário: %s; tamanho: %lu; conteúdo: %s\n",
                    sender_msg_id, time, recipient, strlen(message), message);

            fwrite(to_write, 1, strlen(to_write), arquivo);
            fclose(arquivo);
        } else {
            perror("Change username: ");
        }
        return 0;
    } else {
        wait(NULL);

        FILE *arquivo = fopen(recipient_folder, "wb");

        if (arquivo == NULL) {
            char r_file [50];
            sprintf(r_file, "--->%s\n", recipient_folder);
            perror(r_file);
            return 0;
        }

        char to_write [MAX_MESSAGE_LEN];
        sprintf(to_write, "id: %d; data: %s; remetente: %s; tamanho: %lu; conteúdo: %s\n",
                recipient_msg_id, time, sender, strlen(message), message);

        fwrite(to_write, 1, strlen(to_write), arquivo);
        fclose(arquivo);
        return 1;
    }
    return 0;
}

void list_messages_in_folder(const char *username, int fd, int n) {
    char folder_path[100];
    sprintf(folder_path, "./%s/%sEntrada/", username, username);

    char fifoCLIENTE[100];
    sprintf(fifoCLIENTE, "fifos/%s", username);

    DIR *dir;

    dir = opendir(folder_path);
    if (dir == NULL) {
        perror("opendir");
        return;
    }

    char answer[MAX_MESSAGE_LEN];
    for (int i = 0; i < MAX_MESSAGE_LEN; i++) {
        answer[i] = '\0';
    }
    int contador = 0;

    sprintf(folder_path, "%s/%sEntrada/", username, username);
    FileInfo *f = listFilesDescending(folder_path);

    for (int i = 0; i < n; i++) {
        if (strlen(f[i].name) == 0) {
            break;
        }
        char full_path[100];
        for (int j = 0; j < 100; j++) {
            full_path[j] = '\0';
        }
        sprintf(full_path, "%s%s", folder_path, f[i].name);
        FILE *file = fopen(full_path, "rb");
        if (file == NULL) {
            perror("fopen");
            continue;
        }
        char line[MAX_MESSAGE_LEN];
        if (fgets(line, sizeof(line), file)) {
            char *details = removeDepoisDeConteudo(line);
            if (details != NULL && strlen(details) + contador + 1 < MAX_MESSAGE_LEN) {
                strcat(answer, details);
                strcat(answer, "\n");
                contador += strlen(details) + 1;
            } else if (details != NULL && strlen(details) + contador + 1 >= MAX_MESSAGE_LEN) {
                int value = write(fd, answer, strlen(answer) + 1);
                if (value == -1) {
                    perror("write");
                }
                contador = 0;
                for (int j=0; j<strlen(answer); j++) {
                    answer[j] = '\0';
                }
                strcat(answer, details);
                strcat(answer, "\n");
                contador += strlen(details) + 1;
            }
        }
        fclose(file);
    }
    int value = write(fd, answer, strlen(answer) + 1);
    if (value == -1) {
        perror("write");
    }
    value = write(fd, "Fim",  4);
    if (value == -1) {
        perror("write");
    }
    closedir(dir);
}

void *handle_client(void *arg) {
    char query[MAX_MESSAGE_LEN];
    char username[100];
    char *token = strtok((char *) arg, "/");
    if (token != NULL) {
        strcpy(username, token);
        token = strtok(NULL, "/");
        if (token != NULL) {
            strcpy(query, token);
        }
    }

    char *fifoCLIENTpath = malloc(6 + strlen(username) + 1);
    sprintf(fifoCLIENTpath, "fifos/%s", username);



    int fdCLIENT = open(fifoCLIENTpath, O_WRONLY, 0666);


    if (fdCLIENT == -1) {
        perror(fifoCLIENTpath);
    }

    if (strcmp(query, "") != 0) {
        if (strcmp(query, "concordia-ativar")==0) {
            if (get_userUID(username) != -1) {
                char *added_msg = "User já existe.\n";
                write(fdCLIENT, added_msg, strlen(added_msg) + 1);
                close(fdCLIENT);
                return NULL;
            }

            char command[256];
            for (int i = 0; i < 256; i++) {
                command[i] = '\0';
            }
            int uid;
            for (uid = MAX_GROUP_ID; uid > 0; uid--) {
                if (getpwuid(uid) == NULL) {
                    break;
                }
            }

            char comandos[6][100];
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 100; j++) {
                    comandos[i][j] = '\0';
                }
            }
            sprintf(comandos[0], "%s%s", "dscl . -create /Users/", username);
            sprintf(comandos[1], "%s%s%s%d", "dscl . -create /Users/", username, " UniqueID ", uid);
            sprintf(comandos[2], "%s%s%s", "dscl . -create /Users/", username, " UserShell /bin/bash");
            sprintf(comandos[3], "%s%s%s%d", "dscl . -create /Users/", username, " PrimaryGroupID ", uid);
            sprintf(comandos[4], "%s%s%s%s", "dscl . -create /Users/", username, " NFSHomeDirectory ./",
                    username);
            sprintf(comandos[5], "%s%s%s", "dscl . passwd /Users/", username, " 1234");


            for (int i = 0; i < 5; i++) {
                int output = system(comandos[i]);
                if (output != 0) {
                    char not_added_msg[MAX_MESSAGE_LEN];
                    sprintf(not_added_msg, "User não adicionado: %s\n", comandos[i]);
                    write(fdCLIENT, not_added_msg, strlen(not_added_msg) + 1);
                    return NULL;
                }
            }

            pid_t pid = fork();
            if (pid < 0) {
                perror("fork");
                exit(EXIT_FAILURE);
            } else if (pid == 0) {
                char directory_name[50];
                sprintf(directory_name, "%s", username);
                if (mkdir(directory_name, 0666) != 0) {
                    char *added_msg = "Diretoria não criada\n";
                    write(fdCLIENT, added_msg, strlen(added_msg) + 1);
                }
                char folder_entrada[100];
                char folder_saida[100];
                for (int i = 0; i < 100; i++) {
                    folder_entrada[i] = '\0';
                    folder_saida[i] = '\0';
                }
                sprintf(folder_entrada, "%s/%sEntrada/", directory_name, username);
                sprintf(folder_saida, "%s/%sSaida/", directory_name, username);
                if (mkdir(folder_entrada, 0666) != 0 || mkdir(folder_saida, 0666) != 0) {
                    char *added_msg = "Diretorias de entrada e saída não criadas.\n";
                    write(fdCLIENT, added_msg, strlen(added_msg) + 1);
                    return NULL;
                }

                if (chown(directory_name, uid, -1) == -1) {
                    perror("chown");
                    return NULL;
                }
                if (chown(folder_entrada, uid, -1) == -1) {
                    perror("chown");
                    return NULL;
                }
                if (chown(folder_saida, uid, -1) == -1) {
                    perror("chown");
                    return NULL;
                }

                mode_t mode = S_IRUSR | S_IWUSR | S_IXUSR;

                if (chmod(directory_name, mode) == -1) {
                    perror("chmod");
                }

                if (chmod(folder_entrada, mode) == -1) {
                    perror("chmod");
                }

                if (chmod(folder_saida, mode) == -1) {
                    perror("chmod");
                }

                char *added_msg = "User adicionado com sucesso.\n";
                write(fdCLIENT, added_msg, strlen(added_msg) + 1);
                return NULL;
            }
            wait(NULL);
            return NULL;
        }


        if (strcmp(query, "concordia-login") != 0 && userLogado(username) == 0) {
            char loginDenied[] = "User não logado!";
            write(fdCLIENT, loginDenied, strlen(loginDenied) + 1);
            close(fdCLIENT);
            return NULL;
        }

        if (strcmp(query, "concordia-login") == 0) {
            if (get_userUID(username) != -1) {
                char loginAccepted[] = "User logado!";
                write(fdCLIENT, loginAccepted, strlen(loginAccepted) + 1);
                close(fdCLIENT);
                return NULL;
            } else {
                char loginDenied[] = "User não existe!";
                write(fdCLIENT, loginDenied, strlen(loginDenied) + 1);
                close(fdCLIENT);
                return NULL;
            }
        }
        if (strncmp(query, "concordia-enviar", strlen("concordia-enviar")) == 0) {
            char *token = strtok(query, " ");
            token = strtok(NULL, " ");
            if (token != NULL) {
                char *recipient = token;
                token = strtok(NULL, "");
                if (token != NULL) {
                    char *message = token;

                    if (directory_exists(recipient)) {
                        move_message_to_folder(recipient, message, username, time(NULL), 0);
                        char message_sent[] = "Mensagem enviada com sucesso.\n";
                        write(fdCLIENT, message_sent, strlen(message_sent) + 1);
                        close(fdCLIENT);
                        return NULL;
                    } else {
                        char not_found_msg[] = "Destinatário não existe.\n";
                        write(fdCLIENT, not_found_msg, strlen(not_found_msg) + 1);
                        close(fdCLIENT);
                        return NULL;
                    }
                }
            }
        } else if (strncmp(query, "concordia-listar-g", strlen("concordia-listar-g")) == 0) {
            pid_t pid = fork();
            if (pid < 0) {
                perror("fork");
                exit(EXIT_FAILURE);
            }
            if (pid == 0) {
                char *token = strtok(query, " ");
                token = strtok(NULL, " ");
                if (token != NULL) {
                    char *group_name = token;
                    char group_folder[100];
                    sprintf(group_folder, "%s", group_name);
                    if (directory_exists(group_name) && string_in_array(username, get_group_members(group_name))) {
                        char group_folder_entrada[100];
                        list_messages_in_folder(group_name, fdCLIENT, 1000);
                    } else {
                        char *not_found_msg = "Grupo não encontrado.\n";
                        write(fdCLIENT, not_found_msg, strlen(not_found_msg) + 1);
                    }
                }
            } else {
                wait(NULL);
            }
        }else if (strncmp(query, "concordia-listar", strlen("concordia-listar")) == 0) {
            pid_t pid = fork();
            if (pid < 0) {
                perror("fork");
                exit(EXIT_FAILURE);
            } else if (pid == 0) {
                change_username(username);
                int all = 0;
                if (strstr(query, "-a") != NULL) {
                    all = 1;
                }
                if (all) {
                    list_messages_in_folder(username, fdCLIENT, 1000);
                } else {
                    list_messages_in_folder(username, fdCLIENT, 5);
                }
            } else {
                wait(NULL);
            }
        } else if (strncmp(query, "concordia-ler-g", strlen("concordia-ler-g")) == 0){
            char *token = strtok(query, " ");
            token = strtok(NULL, " ");
            if (token != NULL) {
                char *group_name = token;
                token = strtok(NULL, " ");
                if (token != NULL) {
                    char *message_id = token;
                    char group_folder[100];
                    sprintf(group_folder, "%s", group_name);
                    if (directory_exists(group_name) && string_in_array(username, get_group_members(group_name))) {
                        char group_folder_entrada[100];
                        sprintf(group_folder_entrada, "%s/%sEntrada/", group_folder, group_name);
                        char message_path[100];
                        sprintf(message_path, "%s%s.txt", group_folder_entrada, message_id);
                        FILE *file = fopen(message_path, "rb");
                        if (file == NULL) {
                            char *not_found_msg = "Mensagem não encontrada.\n";
                            write(fdCLIENT, not_found_msg, strlen(not_found_msg) + 1);
                            return NULL;
                        }
                        char line[MAX_MESSAGE_LEN];
                        if (fgets(line, sizeof(line), file)) {
                            write(fdCLIENT, line, strlen(line) + 1);
                        }
                        fclose(file);
                    } else {
                        char *not_found_msg = "Grupo não encontrado.\n";
                        write(fdCLIENT, not_found_msg, strlen(not_found_msg) + 1);
                    }
                }
            }
        }else if (strncmp(query, "concordia-ler", strlen("concordia-ler")) == 0) {
            char *token = strtok(query, " ");
            token = strtok(NULL, " ");
            if (token != NULL) {
                char folder_path[100];
                sprintf(folder_path, "./%s/%sEntrada/", username, username);
                char message_path[100];
                sprintf(message_path, "%s%s.txt", folder_path, token);
                FILE *file = fopen(message_path, "rb");
                if (file == NULL) {
                    char *not_found_msg = "Mensagem não encontrada.\n";
                    write(fdCLIENT, not_found_msg, strlen(not_found_msg) + 1);
                    return NULL;
                }
                char line[MAX_MESSAGE_LEN];
                if (fgets(line, sizeof(line), file)) {
                    write(fdCLIENT, line, strlen(line) + 1);
                }
                fclose(file);
            }
        } else if (strncmp(query, "concordia-remover", strlen("concordia-remover")) == 0) {
            char *token = strtok(query, " ");
            token = strtok(NULL, " ");
            if (token != NULL) {
                char folder_path[100];
                sprintf(folder_path, "./%s/%sEntrada/", username, username);
                char message_path[100];
                sprintf(message_path, "%s%s.txt", folder_path, token);
                if (remove(message_path) == 0) {
                    char *removed_msg = "Mensagem removida com sucesso.\n";
                    write(fdCLIENT, removed_msg, strlen(removed_msg) + 1);
                } else {
                    char *not_found_msg = "Mensagem não encontrada.\n";
                    write(fdCLIENT, not_found_msg, strlen(not_found_msg) + 1);
                }
            }
        } else if (strncmp(query, "concordia-grupo-criar", strlen("concordia-grupo-criar")) == 0) {
            //concordia-grupo-criar grupo1
            char *token = strtok(query, " ");
            token = strtok(NULL, " ");
            if (token != NULL) {
                char group_name[100];
                strcpy(group_name, token);
                char group_folder[100];
                sprintf(group_folder, "%s/", group_name);
                if (mkdir(group_folder, 0666) == 0) {
                    char command[256];
                    for (int i = 0; i < 256; i++) {
                        command[i] = '\0';
                    }
                    gid_t gid;
                    for (gid = MAX_GROUP_ID - 1; gid > 0; gid--) {
                        struct group *grp = getgrgid(gid);

                        if (grp == NULL) {
                            sprintf(command, "dseditgroup -o create -r \"%s\" -i %d %s", group_name, gid, group_name);
                            int output = system(command);
                            if (output == 0) {
                                break;
                            }
                        }
                    }

                    acrescenta_grupo(username, group_name);

                    char group_folder_entrada[100];
                    char group_folder_saida[100];
                    for (int i = 0; i < 100; i++) {
                        group_folder_entrada[i] = '\0';
                        group_folder_saida[i] = '\0';
                    }
                    sprintf(group_folder_entrada, "%s%sEntrada/", group_folder, group_name);
                    sprintf(group_folder_saida, "%s%sSaida/", group_folder, group_name);
                    if (mkdir(group_folder_entrada, 0666) == 0 && mkdir(group_folder_saida, 0666) == 0) {
                        if (chown(group_folder, get_userUID(username), gid) == -1) {
                            perror("chown parent group folder");
                            return NULL;
                        }
                        if (chown(group_folder_entrada, get_userUID(username), gid) == -1) {
                            perror("chown group folder entrada");
                            return NULL;
                        }
                        if (chown(group_folder_saida, get_userUID(username), gid) == -1) {
                            perror("chown group folder saida");
                            return NULL;
                        }

                        mode_t mode = S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP | S_IXUSR | S_IXGRP | S_ISGID  | S_ISUID ;
                        if (chmod(group_folder, mode) == -1) {
                            perror("chmod parent group folder");
                            return NULL;
                        }
                        if (chmod(group_folder_entrada, mode) == -1) {
                            perror("chmod group folder entrada");
                            return NULL;
                        }

                        if (chmod(group_folder_saida, mode) == -1) {
                            perror("chmod group folder saida");
                            return NULL;
                        }

                        char *created_msg = "Grupo criado com sucesso.\n";
                        write(fdCLIENT, created_msg, strlen(created_msg) + 1);
                        return NULL;
                    }
                }
                perror("Criar grupo: ");
                char *not_created_msg = "Grupo não criado.\n";
                write(fdCLIENT, not_created_msg, strlen(not_created_msg) + 1);
            }
        } else if (strncmp(query, "concordia-grupo-destinario-adicionar",
                           strlen("concordia-grupo-destinario-adicionar")) == 0) {
            //concordia-grupo-destinario-adicionar grupo1 user1
            char *token = strtok(query, " ");
            token = strtok(NULL, " ");
            if (token != NULL) {
                char group_name[100];
                strcpy(group_name, token);
                token = strtok(NULL, " ");
                if (token != NULL) {
                    char user_name[100];
                    strcpy(user_name, token);
                    char group_folder[100];
                    sprintf(group_folder, "%s/", group_name);
                    char group_folder_entrada[100];
                    sprintf(group_folder_entrada, "%s%sEntrada/", group_folder, group_name);
                    if (directory_exists(group_name) && directory_exists(user_name) &&
                        string_in_array(username, get_group_members(group_name))) {
                        acrescenta_grupo(user_name, group_name);
                        char *added_msg = "Usuário adicionado ao grupo com sucesso.\n";
                        write(fdCLIENT, added_msg, strlen(added_msg) + 1);
                        return NULL;
                    }
                    if (!directory_exists(group_name)) {
                        char *not_found_msg = "Grupo não encontrado.\n";
                        write(fdCLIENT, not_found_msg, strlen(not_found_msg) + 1);
                        return NULL;
                    }
                    if (!directory_exists(user_name)) {
                        char *not_found_msg = "User não encontrado.\n";
                        write(fdCLIENT, not_found_msg, strlen(not_found_msg) + 1);
                        return NULL;
                    }
                    if (!string_in_array(username, get_group_members(group_name))) {
                        char *not_found_msg = "Usuário não tem permissão para adicionar usuários ao grupo.\n";
                        write(fdCLIENT, not_found_msg, strlen(not_found_msg) + 1);
                        return NULL;
                    }
                }
            }
        } else if (strncmp(query, "concordia-grupo-destinario-remover", strlen("concordia-grupo-destinario-remover")) ==
                   0) {
            //concordia-grupo-destinario-remover grupo1 user1
            char *token = strtok(query, " ");
            token = strtok(NULL, " ");
            if (token != NULL) {
                char group_name[100];
                strcpy(group_name, token);
                token = strtok(NULL, " ");
                if (token != NULL) {
                    char user_name[100];
                    strcpy(user_name, token);
                    char group_folder[100];
                    sprintf(group_folder, "%s/", group_name);
                    char group_folder_entrada[100];
                    sprintf(group_folder_entrada, "%s%sEntrada/", group_folder, group_name);
                    if (directory_exists(group_name) && string_in_array(username, get_group_members(group_name))) {
                        retira_grupo(user_name, group_name);
                        char *removed_msg = "Usuário removido do grupo com sucesso.\n";
                        write(fdCLIENT, removed_msg, strlen(removed_msg) + 1);
                        return NULL;
                    }
                    char *not_removed_msg = "Usuário não removido do grupo.\n";
                    write(fdCLIENT, not_removed_msg, strlen(not_removed_msg) + 1);
                }
            }
        } else if (strncmp(query, "concordia-grupo-remover", strlen("concordia-grupo-remover")) == 0) {
            //concordia-grupo-remover grupo1
            char *token = strtok(query, " ");
            token = strtok(NULL, " ");
            if (token != NULL) {
                char group_name[100];
                strcpy(group_name, token);
                char group_folder[100];
                sprintf(group_folder, "%s/", group_name);
                if (directory_exists(group_name) && string_in_array(username, get_group_members(group_name))) {
                    deleteDirectoryRecursively(group_folder);
                    char command[256];
                    for (int i = 0; i < 256; i++) {
                        command[i] = '\0';
                    }
                    sprintf(command, "dseditgroup -o delete %s", group_name);
                    int output = system(command);
                    if (output == 0) {
                        char *removed_msg = "Grupo removido com sucesso.\n";
                        write(fdCLIENT, removed_msg, strlen(removed_msg) + 1);
                        return NULL;
                    } else {
                        char *not_removed_msg = "Grupo não removido.\n";
                        write(fdCLIENT, not_removed_msg, strlen(not_removed_msg) + 1);
                        return NULL;
                    }
                } else {
                    char *not_removed_msg = "Diretoria do grupo não removida.\n";
                    write(fdCLIENT, not_removed_msg, strlen(not_removed_msg) + 1);
                    return NULL;
                }
                if (!directory_exists(group_name)) {
                    char *not_found_msg = "Grupo não encontrado.\n";
                    write(fdCLIENT, not_found_msg, strlen(not_found_msg) + 1);
                    return NULL;
                }
                if (!string_in_array(username, get_group_members(group_name))) {
                    char *not_removed_msg = "Usuário não tem permissão para remover o grupo.\n";
                    write(fdCLIENT, not_removed_msg, strlen(not_removed_msg) + 1);
                    return NULL;
                }
            }
        } else if (strncmp(query, "concordia-grupo-listar", strlen("concordia-grupo-listar")) == 0) {
            //concordia-grupo-listar grupo1
            char *token = strtok(query, " ");
            token = strtok(NULL, " ");
            if (token != NULL) {
                char group_name[100];
                strcpy(group_name, token);
                char group_folder[100];
                sprintf(group_folder, "%s/", group_name);

                if (directory_exists(group_name) && string_in_array(username, get_group_members(group_name))) {
                    char members[MAX_MESSAGE_LEN];

                    char **group_members = get_group_members(group_name);
                    for (int i = 0; group_members[i]; i++) {
                        char *member = group_members[i];
                        strcat(members, member);
                        strcat(members, " ; ");
                    }
                    write(fdCLIENT, members, strlen(members) + 1);
                    return NULL;
                }
                if (!directory_exists(group_name)) {
                    char *not_found_msg = "Grupo não encontrado.\n";
                    write(fdCLIENT, not_found_msg, strlen(not_found_msg) + 1);
                    return NULL;
                }
                if (!string_in_array(username, get_group_members(group_name))) {
                    char *not_found_msg = "User não tem permissão para listar o grupo.\n";
                    write(fdCLIENT, not_found_msg, strlen(not_found_msg) + 1);
                    return NULL;
                }

            }

        }  else if (strcmp(query, "concordia-desativar") == 0) {
            char command[256];
            for (int i = 0; i < 256; i++) {
                command[i] = '\0';
            }
            sprintf(command, "dscl . -delete /Users/%s", username);
            int output = system(command);
            if (output == 0) {
                deleteDirectoryRecursively(username);
                char *removed_msg = "User removido com sucesso.\n";
                write(fdCLIENT, removed_msg, strlen(removed_msg) + 1);
                return NULL;
            } else {
                char *not_removed_msg = "User não removido.\n";
                write(fdCLIENT, not_removed_msg, strlen(not_removed_msg) + 1);
                return NULL;
            }
        }
        else {
            char answer[] = "Comando inválido\n";
            write(fdCLIENT, answer, strlen(answer) + 1);
            return NULL;
        }

    }
    return NULL;
}

int main() {
    printf("Server started\n");

    if (signal(SIGINT, sigint_handler) == SIG_ERR) {
        perror("signal");
        exit(EXIT_FAILURE);
    }

    if (signal(SIGTERM, sigint_handler) == SIG_ERR) {
        perror("signal");
        exit(EXIT_FAILURE);
    }

    char *fifoSERVERpath = "fifos/servidor";
    mkfifo(fifoSERVERpath, 0666);
    mode_t new_permissions = S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP | S_IROTH | S_IWOTH;

    if (chmod(fifoSERVERpath, new_permissions) != 0) {
        perror("Erro ao alterar permissões");
        return EXIT_FAILURE;
    }

    while (1) {
        int fdSERVER = open(fifoSERVERpath, O_RDONLY, 0666);

        if (fdSERVER == -1) {
            perror("Erro ao abrir o arquivo fifoSERVER");
            return EXIT_FAILURE;
        }
        char buffer[MAX_MESSAGE_LEN];
        for (int i = 0; i < MAX_MESSAGE_LEN; i++) {
            buffer[i] = '\0';
        }
        read(fdSERVER, buffer, MAX_MESSAGE_LEN);
        printf("Mensagem recebida: %s\n", buffer);
        pid_t pid = fork();
        if (pid < 0) {
            perror("fork");
            exit(EXIT_FAILURE);
        } else if (pid == 0) {
            handle_client(buffer);
            exit(EXIT_SUCCESS);
        }
        close(fdSERVER);
    }

    pthread_exit(NULL);
}
