FROM openjdk:8-jdk-alpine

RUN echo "http://mirrors.aliyun.com/alpine/v3.9/main" > /etc/apk/repositories \
    && echo "http://mirrors.aliyun.com/alpine/v3.9/community" >> /etc/apk/repositories \
    && apk update \
    && apk add --no-cache busybox-extras
ARG JAR_FILE=bootstrap/target/*.jar
ADD ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]