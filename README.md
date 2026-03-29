# HRMS Platform

Java 17 | Maven 3.9.9 | Spring Boot 3.2.5 | PostgreSQL 16

## Quick Start
```
docker-compose up -d
mvn clean install -DskipTests
mvn -pl service-discovery spring-boot:run
mvn -pl api-gateway spring-boot:run
mvn -pl service-auth spring-boot:run
mvn -pl service-core-hr spring-boot:run
```

Eureka: http://localhost:8761
Gateway: http://localhost:8080
MinIO: http://localhost:9001 (hrms_minio / hrms_minio_secret)
