# Read Me First
The following was discovered as part of building this project:

* The original package name 'com.jolly.microservices.core.product.product-service' is invalid and this project uses 'com.jolly.microservices.core.product.productservice' instead.

# Getting Started
### To test
```bash
./gradlew clean build && docker-compose build && ./test-em-all.bash start stop
```
### To run
```bash
./gradlew build && docker-compose build && docker-compose up -d
```

### Reference Documentation
For further reference, please consider the following sections:

* [Microservices with Spring Boot and Spring Cloud](https://www.amazon.com/Microservices-Spring-Boot-Cloud-microservices/dp/1801072973)
* [Building Dockerfile on M1](https://stackoverflow.com/questions/68984133/error-failed-to-solve-with-frontend-dockerfile-v0-failed-to-create-llb-defini)
* [OpenAPI 3 & Spring Boot](https://springdoc.org)
* [Single Container Pattern for Testcontainers](https://java.testcontainers.org/test_framework_integration/manual_lifecycle_control/#singleton-containers)



