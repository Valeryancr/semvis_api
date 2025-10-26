# =========================================================
# Etapa 1: Compilación
# =========================================================
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar los archivos del proyecto
COPY pom.xml .
COPY src ./src

# Compilar el proyecto (sin correr tests)
RUN mvn clean package -DskipTests

# =========================================================
# Etapa 2: Imagen final
# =========================================================
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiar el jar generado desde la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Puerto donde se ejecuta la app
EXPOSE 8080

# Comando de inicio
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
