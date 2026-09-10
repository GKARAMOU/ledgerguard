FROM maven:3.9.11-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn --batch-mode dependency:go-offline
COPY src src
RUN mvn --batch-mode package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S ledgerguard && adduser -S ledgerguard -G ledgerguard
WORKDIR /app
COPY --from=build /workspace/target/ledgerguard-api-1.0.0.jar app.jar
USER ledgerguard
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
