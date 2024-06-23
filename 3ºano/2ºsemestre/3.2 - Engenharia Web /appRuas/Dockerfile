FROM node:14

WORKDIR /appRuas

COPY package*.json ./

RUN npm install

COPY . .

ENV PORT=2034

EXPOSE 2034

CMD ["npm", "start"]

