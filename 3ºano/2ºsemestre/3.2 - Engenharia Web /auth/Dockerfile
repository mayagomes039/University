FROM node:20

# Create app directory
WORKDIR /auth

# Install app dependencies
COPY package*.json ./

RUN npm install

# Bundle app source
COPY . .

EXPOSE 3434
CMD [ "node", "./bin/www" ]