plugins {
	java
	id("org.springframework.boot") version "3.1.2"
	id("io.spring.dependency-management") version "1.1.2"
}

group = "com.jolly.microservices.core.review"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
}

val mapstructVersion by extra { "1.5.5.Final" }
val testcontainersVersion by extra { "1.18.3" }
val mysqlConnectorVersion by extra { "8.1.0" }
val springCloudVersion by extra { "2022.0.4" }

dependencies {
	implementation(project(":api"))
	implementation(project(":util"))

	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.mapstruct:mapstruct:${mapstructVersion}")

	compileOnly("org.mapstruct:mapstruct-processor:${mapstructVersion}")
	annotationProcessor("org.mapstruct:mapstruct-processor:${mapstructVersion}")
	testAnnotationProcessor("org.mapstruct:mapstruct-processor:${mapstructVersion}")

	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("com.mysql:mysql-connector-j:${mysqlConnectorVersion}")
	implementation(platform("org.testcontainers:testcontainers-bom:${testcontainersVersion}"))
	testImplementation("org.testcontainers:testcontainers")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:mysql")

	implementation("org.springframework.cloud:spring-cloud-starter-stream-rabbit")
	implementation("org.springframework.cloud:spring-cloud-starter-stream-kafka")

	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
