#BUILDER

FROM node:22-alpine as builder

#Diretório de trabalho
WORKDIR /app

#Copia os arquivos package.son e package-lock.json
COPY package*.json ./

#Instala todas as dependencias do projeto
RUN npm install

COPY . .

RUN npm run build
CMD [ "npm" , "run", "preview", "--", "--port", "9090", "--host", "0.0.0.0"]
