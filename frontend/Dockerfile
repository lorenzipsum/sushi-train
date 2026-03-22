FROM node:24.14-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY angular.json tsconfig.json tsconfig.app.json tsconfig.spec.json ./
COPY public public
COPY src src
COPY contracts contracts

RUN npm run build

FROM nginx:1.28.2-alpine
COPY nginx.conf /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

EXPOSE 80
