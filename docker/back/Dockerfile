# 基础镜像
FROM  openjdk:8-jre-alpine
# author
MAINTAINER coasho

# 挂载目录
VOLUME /home/wschat
# 创建目录
RUN mkdir -p /home/wschat
# 指定路径
WORKDIR /home/wschat
# 复制jar文件到路径
COPY ./ws-chat-1.0-SNAPSHOT.jar /home/wschat/ws-chat-1.0-SNAPSHOT.jar
# 启动文章服务
ENTRYPOINT ["nohup","java","-Xmx50m","-Xms50m","-jar","ws-chat-1.0-SNAPSHOT.jar"]
