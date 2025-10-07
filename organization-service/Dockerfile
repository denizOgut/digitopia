FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN apk add --no-cache curl

COPY target/organization-service-1.0.0.jar app.jar

EXPOSE 8082

CMD ["java", "-jar", "app.jar"]