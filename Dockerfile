# Etap 1: Budowanie aplikacji
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
# Kopiujemy całą zawartość repozytorium do obrazu
COPY . .
# Wchodzimy do podfolderu, w którym faktycznie jest projekt Java
WORKDIR /app/StronaPelnaPiersia
# Budujemy plik JAR, pomijając testy dla szybkości
RUN mvn clean package -DskipTests

# Etap 2: Uruchamianie aplikacji
FROM openjdk:17-jdk-slim
WORKDIR /app
# Kopiujemy zbudowany plik JAR z poprzedniego etapu
COPY --from=build /app/StronaPelnaPiersia/target/*.jar app.jar
# Tworzymy folder na uploady (żeby aplikacja się nie wywaliła przy starcie)
RUN mkdir -p uploads
# Eksponujemy port 8080 (standard dla Spring Boot)
EXPOSE 8080
# Komenda startowa
ENTRYPOINT ["java", "-jar", "app.jar"]
