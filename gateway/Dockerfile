
FROM openjdk:21-jdk-slim

COPY target/gateway-0.0.1-SNAPSHOT.jar /app.jar
COPY /eureka.p12 /app/eureka.p12

WORKDIR /app

CMD ["java", "-jar", "/app.jar"]
