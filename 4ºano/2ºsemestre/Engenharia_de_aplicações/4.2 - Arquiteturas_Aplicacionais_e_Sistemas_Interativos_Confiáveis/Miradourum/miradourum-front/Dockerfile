FROM node:18

WORKDIR /app

COPY . .

RUN npm install && npm run build

# Use a simple static server for production (like `serve`)
RUN npm install -g serve

EXPOSE 5173

CMD ["serve", "-s", "dist", "-l", "5173"]
