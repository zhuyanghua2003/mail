FROM eclipse-temurin:8-jdk-alpine

ENV TZ Asia/Shanghai

EXPOSE 80

VOLUME /tmp

ADD target/*.jar  /app.jar

ENTRYPOINT ["java","-jar","/app.jar"]