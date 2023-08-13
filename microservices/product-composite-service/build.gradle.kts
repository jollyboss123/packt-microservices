plugins {
	java
	id("org.springframework.boot") version "3.1.2"
	id("io.spring.dependency-management") version "1.1.2"
}

group = "com.jolly.microservices.composite.product"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(project(":api"))
	implementation(project(":util"))

	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.2.0")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
