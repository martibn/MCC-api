FROM maven:3.9.16-eclipse-temurin-25 AS builder
WORKDIR /app
COPY pom.xml ./
RUN mvn dependency:go-offline --no-transfer-progress
COPY src src
RUN mvn package -DskipTests --no-transfer-progress

FROM eclipse-temurin:25-jre-ubi10-minimal
WORKDIR /app
COPY --from=builder /app/target/mcc-api.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
