FROM openjdk:8-jdk-alpine
ARG JAR_FILE=bootstrap/target/*.jar
ADD ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]