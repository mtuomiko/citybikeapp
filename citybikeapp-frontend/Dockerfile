FROM node:18.13.0-alpine3.16 as build-stage

WORKDIR /stage

ENV CI=true

COPY package.json package-lock.json .

RUN npm ci

COPY . .

RUN npm run build

FROM nginx:1.23.3-alpine

COPY --from=build-stage /stage/build / usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80
