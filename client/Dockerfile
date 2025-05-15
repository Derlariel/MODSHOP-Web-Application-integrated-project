FROM node:20-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .

ARG NODE_ENV=production
ENV NODE_ENV=${NODE_ENV}

RUN npm run build -- --mode production

FROM nginx:stable-alpine
COPY --from=build /app/dist /usr/share/nginx/html/kk1/itb-mshop
COPY ./nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]