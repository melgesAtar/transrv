# Multi-stage Dockerfile for Spring Boot app (Render compatible)

FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /app

# Copy source
COPY pom.xml ./
COPY src ./src

# Build the application
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=build /app/target/*.jar /app/app.jar

# Default port; Render injects PORT at runtime
ENV PORT=8080
ENV JAVA_OPTS=""

EXPOSE 8080

# Forward Render's PORT to Spring Boot
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT} -jar /app/app.jar"]


