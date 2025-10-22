# ===============================
# 1️⃣ BUILD STAGE
# ===============================
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom and download dependencies (cached between builds)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build the jar
COPY src ./src
RUN mvn clean package -DskipTests

# ===============================
# 2️⃣ RUNTIME STAGE
# ===============================
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port your app runs on
EXPOSE 8080

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]