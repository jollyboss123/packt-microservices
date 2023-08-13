plugins {
	java
	id("org.springframework.boot") version "3.1.2"
	id("io.spring.dependency-management") version "1.1.2"
}

group = "com.jolly.microservices.core.recommendation"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
}

val mapstructVersion by extra { "1.5.5.Final" }
val testcontainersVersion by extra { "1.18.3" }

dependencies {
	implementation(project(":api"))
	implementation(project(":util"))

	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.mapstruct:mapstruct:${mapstructVersion}")

	compileOnly("org.mapstruct:mapstruct-processor:${mapstructVersion}")
	annotationProcessor("org.mapstruct:mapstruct-processor:${mapstructVersion}")
	testAnnotationProcessor("org.mapstruct:mapstruct-processor:${mapstructVersion}")

	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation(platform("org.testcontainers:testcontainers-bom:${testcontainersVersion}"))
	testImplementation("org.testcontainers:testcontainers")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:mongodb")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
