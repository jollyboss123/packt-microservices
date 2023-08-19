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

val springdocVersion by extra { "2.2.0" }
val springCloudVersion by extra { "2022.0.4" }

dependencies {
	implementation(project(":api"))
	implementation(project(":util"))

	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:${springdocVersion}")

	implementation("org.springframework.cloud:spring-cloud-starter-stream-rabbit")
	implementation("org.springframework.cloud:spring-cloud-starter-stream-kafka")
	testImplementation("org.springframework.cloud:spring-cloud-stream-test-binder")

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
