FROM maven:3.9-amazoncorretto-17 AS builder
COPY ./mobile-shop /backend
WORKDIR /backend
RUN mvn clean install -DskipTests

FROM eclipse-temurin:17-jre-alpine
COPY --from=builder /backend/target/*.jar /app/app.jar
WORKDIR /app
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]