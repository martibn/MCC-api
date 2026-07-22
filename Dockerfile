FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline --no-transfer-progress
COPY src src
RUN ./mvnw package -DskipTests --no-transfer-progress

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=builder /app/target/mcc-api.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
