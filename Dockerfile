# Etapa de compilación
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Limitar la memoria de la JVM para Maven
ENV MAVEN_OPTS="-Xmx300m -XX:+UseSerialGC"

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
# Usar el flag -B (Batch mode) para evitar logs excesivos que consuman memoria
RUN mvn clean package -DskipTests -B

# Etapa de ejecución
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 10000

# Limitar la memoria en tiempo de ejecución para que no pase de los 512MB de Render
ENTRYPOINT ["java", "-Xmx350m", "-jar", "app.jar"]