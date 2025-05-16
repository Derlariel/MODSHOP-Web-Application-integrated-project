FROM node:20-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .

ARG NODE_ENV=production
ENV NODE_ENV=${NODE_ENV}

RUN npm run build -- --base=/kk1/

FROM nginx:stable-alpine

COPY --from=build /app/dist /usr/share/nginx/html/kk1
COPY ./nginx.conf /etc/nginx/conf.d/default.conf

RUN chmod -R 775 /usr/share/nginx/html/kk1
RUN chmod -R nginx:nginx /usr/share/nginx/html/kk1

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]