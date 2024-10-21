FROM openjdk:21-jdk-slim
VOLUME /tmp
EXPOSE 8080
ARG JAR_FILE=build/libs/gateway-service.jar
ADD ${JAR_FILE} gateway-service.jar
ENTRYPOINT ["java","-jar","/gateway-service.jar"]