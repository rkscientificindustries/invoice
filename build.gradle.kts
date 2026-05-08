plugins {
  java
  id("org.springframework.boot") version "4.0.5"
  id("io.spring.dependency-management") version "1.1.7"
  id("com.vaadin") version "25.1.5"
}

group = "com.rkscientificindustries"
version = "0.0.1-SNAPSHOT"
description = "Invoice Management System"

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(25)
  }
}

configurations {
  compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
  }
}

repositories {
  mavenCentral()
  maven("https://maven.vaadin.com/vaadin-addons")
}

extra["vaadinVersion"] = "25.1.5"
extra["springBootVersion"] = "4.0.5"

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
  implementation("org.springframework.boot:spring-boot-starter-flyway")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.flywaydb:flyway-database-postgresql")
  implementation("com.vaadin:vaadin-spring-boot-starter")
  implementation("com.github.librepdf:openpdf:3.0.3")
  implementation("com.vaadin.componentfactory:breadcrumb:4.0.1")

  developmentOnly("com.vaadin:vaadin-dev")
  developmentOnly("org.springframework.boot:spring-boot-devtools")
  developmentOnly("org.springframework.boot:spring-boot-docker-compose")
  runtimeOnly("org.postgresql:postgresql")
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  annotationProcessor("org.projectlombok:lombok")

  testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
  testImplementation("org.springframework.boot:spring-boot-starter-data-jdbc-test")
  testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
  testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
  testImplementation("org.springframework.boot:spring-boot-starter-security-test")
  testImplementation("org.springframework.boot:spring-boot-testcontainers")
  testImplementation("org.testcontainers:testcontainers-junit-jupiter")
  testImplementation("org.testcontainers:testcontainers-postgresql")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
  imports {
    mavenBom("com.vaadin:vaadin-bom:${property("vaadinVersion")}")
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}

tasks.register("updateReadmeBadges") {
  group = "documentation"
  description = "Updates the version badges in README.md based on project configuration."

  doLast {
    val readmeFile = file("README.md")
    if (!readmeFile.exists()) {
      println("README.md not found.")
      return@doLast
    }

    val javaVersion = java.toolchain.languageVersion.get().toString()
    val springBootVersion = project.extra["springBootVersion"] as String
    val vaadinVersion = project.extra["vaadinVersion"] as String

    val composeFile = file("compose.yml")
    val postgresVersion = if (composeFile.exists()) {
      val content = composeFile.readText()
      Regex("image: postgres:(\\d+)").find(content)?.groupValues?.get(1) ?: "unknown"
    } else {
      "unknown"
    }

    var content = readmeFile.readText()
    content = content.replace(
      Regex("!\\[Java\\]\\(https://img\\.shields\\.io/badge/Java-[^)]+\\)"),
      "![Java](https://img.shields.io/badge/Java-$javaVersion-blue.svg)"
    )
    content = content.replace(
      Regex("!\\[Spring Boot\\]\\(https://img\\.shields\\.io/badge/Spring_Boot-[^)]+\\)"),
      "![Spring Boot](https://img.shields.io/badge/Spring_Boot-$springBootVersion-6DB33F.svg?logo=spring-boot)"
    )
    content = content.replace(
      Regex("!\\[Vaadin\\]\\(https://img\\.shields\\.io/badge/Vaadin-[^)]+\\)"),
      "![Vaadin](https://img.shields.io/badge/Vaadin-${vaadinVersion.replace("-", "--")}-00B4F0.svg?logo=vaadin)"
    )
    content = content.replace(
      Regex("!\\[PostgreSQL\\]\\(https://img\\.shields\\.io/badge/PostgreSQL-[^)]+\\)"),
      "![PostgreSQL](https://img.shields.io/badge/PostgreSQL-$postgresVersion-316192.svg?logo=postgresql)"
    )

    readmeFile.writeText(content)
    println("Successfully updated README.md badges with Java $javaVersion, Spring Boot $springBootVersion, Vaadin $vaadinVersion, and PostgreSQL $postgresVersion.")
  }
}
