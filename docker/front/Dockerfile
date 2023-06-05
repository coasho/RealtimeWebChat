FROM node:16.20-alpine3.16
MAINTAINER coasho
#参数,node的环境为生产环境
ENV NODE_ENV=production
#任意ip
ENV HOST 0.0.0.0
#容器内创建目录app
RUN mkdir -p /app
#复制项目文件到容器内
COPY ./nuxtjs /app
WORKDIR /app
EXPOSE 3000
#配置npm的远程仓库
RUN npm install cnpm -g --registry=https://registry.npm.taobao.org
#避免缓存造成的依赖干扰导致构建失败
RUN npm cache clean --force
#安装依赖
RUN npm install --no-package-lock
#构建,生成dist文件
RUN npm run build
#startCMD
CMD ["npm","start"]
