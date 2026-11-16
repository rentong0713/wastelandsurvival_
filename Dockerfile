
# Use Maven and Java 25 to build the application.
FROM maven:3.9-eclipse-temurin-25 AS build

# Set the working directory inside Docker.
WORKDIR /app

# Copy the Maven configuration.
COPY pom.xml .

# Copy the application source code.
COPY src ./src

# Build the Spring Boot application.
RUN mvn clean package -DskipTests

# Use Java 25 to run the application.
FROM eclipse-temurin:25-jre

# Set the working directory.
WORKDIR /app

# Copy the generated JAR from the build stage.
COPY --from=build /app/target/*.jar app.jar

# Expose the application port.
EXPOSE 10000

# Start Spring Boot using Render's assigned port.
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-10000} -Dserver.address=0.0.0.0 -jar app.jar"]
