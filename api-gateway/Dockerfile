FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN apk add --no-cache curl

COPY target/api-gateway-1.0.0.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]