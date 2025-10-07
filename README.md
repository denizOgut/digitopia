
# Digitopia Microservices Platform

A microservices-based organization management platform with user authentication, organization management, and invitation system.

## Tech Stack

- Java 21 + Spring Boot 3.2.0  
- Spring Cloud (Gateway, Eureka)  
- PostgreSQL (Database)  
- Redis (Caching)  
- RabbitMQ (Event messaging)  
- JWT (Authentication)  
- Bucket4j (API protection)  
- OpenAPI/Swagger (Rate Limiting)  
- JUnit 5 (Testing)  

## Services

Service Registry - 8761 - Eureka server  
API Gateway - 8080 - Gateway with JWT and rate limiting  
User Service - 8081 - User management and authentication  
Organization Service - 8082 - Organization CRUD operations  
Invitation Service - 8083 - Invitation management  

## Quick Start

### 1. Start Infrastructure

```bash
docker-compose up -d
```


### 2. Build and Run

```bash
cd common-lib && mvn clean install && cd ..


cd service-registry && mvn spring-boot:run &
cd api-gateway && mvn spring-boot:run &
cd user-service && mvn spring-boot:run &
cd organization-service && mvn spring-boot:run &
cd invitation-service && mvn spring-boot:run &
```

## Swagger Documentation

- [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html) (User Service)
    
- [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html) (Organization Service)
    
- [http://localhost:8083/swagger-ui.html](http://localhost:8083/swagger-ui.html) (Invitation Service)
    

## Features

- JWT authentication and role-based access (ADMIN, MANAGER, USER)
    
- Service discovery with Eureka
    
- Event-driven architecture using ``RabbitMQ``
    
- ``Redis`` caching for performance
    
- Rate limiting (50 requests/minute per user/IP)
    
- Request tracing with X-Trace-Id
    
- Input validation and sanitization
    
- Scheduled jobs (invitation expiry at midnight)
    
- Comprehensive unit tests
    
- ``OpenAPI`` documentation


## AI-generated

- Unit tests especially for throwing exceptions
- Javadocs
- StringUtil sanitize method
- .dockerignore files
    

## TODO

- Deploy to Cloud (AWS, Azure, or GCP using Docker and Kubernetes)
    
- Add Mail Service  for  notification emails

- Add monitoring(Elasticsearch, Kibana, and Prometheus)
