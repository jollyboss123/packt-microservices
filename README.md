# Prototyping microservices
Prototyping microservices based on Magnus Larsson's book.
Changes have been made in accordance with Spring Boot 3, Java 17:
* use of Java 17 record
* fix spring security deprecates 
* dockerizing with `eclipse-temurin:17-jre-alpine`
* use of spring authorization server

## Getting Started
### To test
```bash
./gradlew clean build && docker-compose build && ./test-em-all.bash start stop
```
### To run
```bash
./gradlew build && docker-compose build && docker-compose up -d
```

### To generate self-signed cert
```bash
keytool -genkeypair -alias localhost -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore edge.p12 -validity 3650
```

## Reference Documentation
For further reference, please consider the following sections:

* [Microservices with Spring Boot and Spring Cloud](https://www.amazon.com/Microservices-Spring-Boot-Cloud-microservices/dp/1801072973)
* [Building Dockerfile on M1](https://stackoverflow.com/questions/68984133/error-failed-to-solve-with-frontend-dockerfile-v0-failed-to-create-llb-defini)
* [OpenAPI 3 & Spring Boot](https://springdoc.org)
* [Single Container Pattern for Testcontainers](https://java.testcontainers.org/test_framework_integration/manual_lifecycle_control/#singleton-containers)
* [MySQL image high memory usage](https://github.com/docker-library/mysql/issues/579)
* [Docker Settings on Mac](https://docs.docker.com/desktop/settings/mac/)
* [Spring Authorization Server](https://spring.io/projects/spring-authorization-server#overview)
* [ReactiveJwtDecoder that could not be found issue](https://www.jianshu.com/p/70b926c23704)



