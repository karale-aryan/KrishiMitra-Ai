# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Cache dependencies (increases build speed for successive builds)
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/krishimitra-backend-*.jar app.jar

# Create directory for uploads (soil test images/disease images)
RUN mkdir -p /app/uploads/disease-images

# Expose server port
EXPOSE 8080

# Run JVM with proper garbage collector and memory limits
ENTRYPOINT ["java", "-XX:+UseG1GC", "-jar", "app.jar"]
