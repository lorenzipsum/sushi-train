FROM maven:3.9.11-eclipse-temurin-25 AS build
WORKDIR /app

COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd
COPY src src

RUN ./mvnw -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /app/target/sushi-train-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
