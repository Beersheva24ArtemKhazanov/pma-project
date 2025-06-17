FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/patient-call-receiver.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]