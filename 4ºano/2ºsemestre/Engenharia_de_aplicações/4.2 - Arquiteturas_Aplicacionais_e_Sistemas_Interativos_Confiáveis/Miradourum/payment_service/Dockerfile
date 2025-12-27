# Usar imagem oficial do Node.js
FROM node:20-alpine

# Diretório da app
WORKDIR /app

# Copiar ficheiros
COPY . .

# Instalar dependências
RUN npm install

# Expor porta da app
EXPOSE 3000

# Comando de arranque
CMD ["npm", "start"]
