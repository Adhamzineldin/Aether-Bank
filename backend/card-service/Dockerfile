FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .


RUN chmod +x mvnw
# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code and generated code
COPY src ./src
COPY generated ./generated

# Build the application
RUN ./mvnw clean package -Dmaven.test.skip=true

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Copy the JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8084

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

