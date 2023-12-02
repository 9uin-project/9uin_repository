FROM node:18.16.0
WORKDIR /app
COPY package.json .
RUN yarn install
ENV REACT_APP_NODE_ENV=development

COPY . .
EXPOSE 3000
CMD ["yarn", "start:dev"]
