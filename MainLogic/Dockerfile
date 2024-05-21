FROM openjdk:21-jdk-slim

ENV db_port=db
COPY target/MainLogic-0.0.1-SNAPSHOT.jar /app.jar

WORKDIR /app

CMD ["java", "--enable-preview", "-jar", "/app.jar"]
