FROM eclipse-temurin:8-jdk-alpine

ENV TZ Asia/Shanghai

EXPOSE 8050

VOLUME /tmp

ADD target/*.jar  /app.jar

ENTRYPOINT ["java","-jar","/app.jar"]