plugins {
	java
	id("org.springframework.boot") version "3.1.2"
	id("io.spring.dependency-management") version "1.1.2"
}

group = "com.jolly.springcloud"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
}

extra["springCloudVersion"] = "2022.0.4"

dependencies {
//	implementation("org.springframework.boot:spring-boot-starter-actuator")
//	implementation("org.springframework.cloud:spring-cloud-starter-gateway")
//	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
//
//	implementation("org.springframework.boot:spring-boot-starter-oauth2-authorization-server")
//	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-actuator") {
		exclude(group = "org.springframework.boot", module = "spring-boot-starter-web")
	}
	implementation("org.springframework.cloud:spring-cloud-starter-gateway") {
		exclude(group = "org.springframework.boot", module = "spring-boot-starter-web")
	}
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client") {
		exclude(group = "org.springframework.boot", module = "spring-boot-starter-web")
	}
	implementation("org.springframework.boot:spring-boot-starter-oauth2-authorization-server") {
		exclude(group = "org.springframework.boot", module = "spring-boot-starter-web")
	}
	implementation("org.springframework.boot:spring-boot-starter-security") {
		exclude(group = "org.springframework.boot", module = "spring-boot-starter-web")
	}

	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-reactor-netty")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
