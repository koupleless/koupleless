FROM openjdk:8-jdk-alpine
ARG JAR_FILE=./base/bootstrap/target/*.jar
ADD ${JAR_FILE} app.jar
COPY config/arkctl* /bin/arkctl
# 如果是  mac 64位机器，则执行
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]