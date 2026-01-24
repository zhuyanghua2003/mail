FROM eclipse-temurin:8-jdk-alpine

ENV TZ Asia/Shanghai

EXPOSE 8040

VOLUME /tmp

ADD target/mall-product-0.0.1-SNAPSHOT.jar /app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
