FROM maven:3.9-amazoncorretto-17 AS builder
COPY ./mobile-shop /backend
WORKDIR /backend
RUN mvn clean install -DskipTests

FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /backend/target/*.jar /app/app.jar

COPY wait-for-db.sh /app/wait-for-db.sh
RUN chmod +x /app/wait-for-db.sh

RUN chown -R appuser:appgroup /app

USER appuser

WORKDIR /app

EXPOSE 8080

CMD ["/app/wait-for-db.sh"]