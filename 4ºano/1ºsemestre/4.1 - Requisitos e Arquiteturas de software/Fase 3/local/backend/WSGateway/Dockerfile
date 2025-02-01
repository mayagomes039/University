# Usar a imagem oficial do Node.js
FROM node:18

# Definir o diretório de trabalho no container
WORKDIR /app

# Copiar os arquivos package.json e package-lock.json para o diretório de trabalho no container
COPY package*.json ./

# Instalar as dependências (dev dependencies também)
RUN npm install

# Copiar todo o código para o diretório de trabalho no container
COPY . .

# Expor a porta que o servidor vai usar (8080)
EXPOSE 8080

# Definir o comando que vai iniciar o servidor
CMD ["npx", "ts-node", "src/server.ts"]
