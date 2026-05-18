# Etap 1: Budowanie aplikacji
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
WORKDIR /app/StronaPelnaPiersia
RUN mvn clean package -DskipTests

# Etap 2: Uruchamianie aplikacji
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/StronaPelnaPiersia/target/*.jar app.jar
RUN mkdir -p uploads
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
