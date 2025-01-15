plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.3.4"
	id("io.spring.dependency-management") version "1.1.6"
	kotlin("plugin.jpa") version "1.9.25"
}

group = "com.ecommercedemo"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

fun loadEnv(): Map<String, String> {
	val envFile = file("${rootProject.projectDir}/.env")
	if (!envFile.exists()) {
		throw GradleException(".env file not found")
	}

	return envFile.readLines()
		.filter { it.isNotBlank() && !it.startsWith("#") }
		.map { it.split("=", limit = 2) }
		.associate { it[0] to it.getOrElse(1) { "" } }
}

val githubUsername: String? = project.findProperty("githubUsername") as String? ?: System.getenv("GITHUB_USERNAME") ?: loadEnv()["GITHUB_USERNAME"]
val githubToken: String? = project.findProperty("githubToken") as String? ?: System.getenv("GITHUB_TOKEN") ?: loadEnv()["GITHUB_TOKEN"]

extra["springCloudVersion"] = "2023.0.3"

repositories {
	mavenCentral()
	maven {
		url = uri("https://maven.pkg.github.com/aronvaupel/Commons")
		credentials {
			username = githubUsername ?: throw GradleException("GitHub username not provided")
			password = githubToken ?: throw GradleException("GitHub token not provided")
		}
	}
	maven {
		url = uri("https://repo.spring.io/milestone")
	}
	maven {
		url = uri("https://repo.spring.io/snapshot")
	}
}

extra["springCloudVersion"] = "2023.0.3"

dependencies {
	implementation("com.github.aronvaupel:commons:7.1.1")
	implementation("io.hypersistence:hypersistence-utils-hibernate-63:3.9.0")
	implementation("org.hibernate:hibernate-core:6.6.3.Final")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.cloud:spring-cloud-starter-gateway-mvc")
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.1.3")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("io.jsonwebtoken:jjwt:0.9.1")
	implementation("io.github.cdimascio:dotenv-kotlin:6.2.2")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("com.fasterxml.jackson.module:jackson-module-jakarta-xmlbind-annotations:2.15.2")
	implementation("org.springframework.boot:spring-boot-starter-data-redis:3.3.4")
	implementation("org.postgresql:postgresql:42.7.2")
	implementation("org.springframework.kafka:spring-kafka")
	implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
