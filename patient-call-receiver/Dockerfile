FROM openjdk:21-jdk-slim
WORKDIR /app
COPY patient-call-receiver-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]