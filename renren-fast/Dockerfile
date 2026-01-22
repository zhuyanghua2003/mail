FROM eclipse-temurin:8-jdk-alpine

ENV TZ Asia/Shanghai

EXPOSE 8093

VOLUME /tmp

ADD  target/*.jar /app.jar

ENTRYPOINT ["java","-jar","/app.jar","--spring.profiles.active=dev"]