plugins {
	java
	id("io.spring.dependency-management") version "1.1.2"
}

group = "com.jolly.microservices.api"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
}

val springBootVersion by extra { "3.1.2" }
val springdocVersion by extra { "2.2.0" }

dependencies {
	implementation(platform("org.springframework.boot:spring-boot-dependencies:${springBootVersion}"))

	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springdoc:springdoc-openapi-starter-common:${springdocVersion}")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
