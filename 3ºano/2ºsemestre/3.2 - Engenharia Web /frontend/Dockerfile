FROM node:14

WORKDIR /frontend


COPY package*.json ./

RUN npm install

COPY . .

ENV PORT=3439

EXPOSE 3439


CMD ["npm", "start"]

