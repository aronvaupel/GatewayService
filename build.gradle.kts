plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.3.4"
	id("io.spring.dependency-management") version "1.1.6"
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

val env = loadEnv()

val githubUsername: String? = project.findProperty("githubUsername") as String? ?: System.getenv("GITHUB_USERNAME") ?: env["GITHUB_USERNAME"]
val githubToken: String? = project.findProperty("githubToken") as String? ?: System.getenv("GITHUB_TOKEN") ?: env["GITHUB_TOKEN"]

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
	implementation("com.github.aronvaupel:commons:6.4.10")
	{
		exclude(group = "org.springframework.data", module = "spring-data-jpa")
		exclude(group = "jakarta.persistence", module = "jakarta.persistence-api")
	}
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.cloud:spring-cloud-starter-gateway-mvc")
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
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
