# Etap 1: Budowanie aplikacji
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
WORKDIR /app/StronaPelnaPiersia
RUN mvn clean package -DskipTests

# Etap 2: Uruchamianie aplikacji
FROM eclipse-temurin:17-jre
WORKDIR /app
# Kopiujemy zbudowany plik JAR z poprzedniego etapu
COPY --from=build /app/StronaPelnaPiersia/target/*.jar app.jar
# Tworzymy folder na uploady i nadajemy uprawnienia
RUN mkdir -p /app/uploads && chmod 777 /app/uploads
# Eksponujemy port 8080
EXPOSE 8080
# Komenda startowa
ENTRYPOINT ["java", "-jar", "app.jar"]
