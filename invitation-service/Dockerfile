FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN apk add --no-cache curl

COPY target/invitation-service-1.0.0.jar app.jar

EXPOSE 8083

CMD ["java", "-jar", "app.jar"]