# Etapa 1: Construcción (Build)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar el archivo de configuración de dependencias y descargar (cache)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiar el código fuente y compilar el proyecto
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución (Runtime)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copiar el JAR generado desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto que usa Spring Boot (por defecto 8080)
EXPOSE 8080

# Comando para iniciar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]