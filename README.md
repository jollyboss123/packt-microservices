# Prototyping microservices
Prototyping microservices based on Magnus Larsson's book with Spring Boot 3, Java 17 and dockerizing with eclipse-temurin:17-jre-alpine.
Changes has been implemented using Java 17 specific updates e.g. record etc.

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
* [MySQL image high memory usage](https://github.com/docker-library/mysql/issues/579)
* [Docker Settings on Mac](https://docs.docker.com/desktop/settings/mac/)



