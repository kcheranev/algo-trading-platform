import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.1.5"
    id("io.spring.dependency-management") version "1.1.3"
    kotlin("jvm") version "1.8.22"
    kotlin("kapt") version "1.8.22"
    kotlin("plugin.spring") version "1.8.22"
}

group = "ru.kcheranev"
version = "0.0.1-SNAPSHOT"

val thymeleafExtrasJava8timeVersion by extra("3.0.4.RELEASE")
val tinkoffApiVersion by extra("1.5")
val ta4jVersion by extra("0.17")
val mapstructVersion by extra("1.5.5.Final")
val springdocOpenapiStarterWebmvcUiVersion by extra("2.2.0")
val kotestVersion by extra("5.8.0")
val kotestExtensionsSpringVersion by extra("1.1.3")
val mockkVersion by extra("1.13.8")
val wiremockVersion by extra("3.3.1")
val wiremockGrpcExtensionVersion by extra("0.4.0")
val testcontainersPostgresqlVersion by extra("1.19.3")

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.retry:spring-retry")
    implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("org.thymeleaf.extras:thymeleaf-extras-java8time:$thymeleafExtrasJava8timeVersion")
    implementation("org.liquibase:liquibase-core")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("ru.tinkoff.piapi:java-sdk-core:$tinkoffApiVersion")
    implementation("org.ta4j:ta4j-core:$ta4jVersion")
    implementation("org.mapstruct:mapstruct:$mapstructVersion")
    implementation("org.postgresql:postgresql")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocOpenapiStarterWebmvcUiVersion")

    kapt("org.mapstruct:mapstruct-processor:$mapstructVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-framework-datatest:$kotestVersion")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:$kotestExtensionsSpringVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.wiremock:wiremock:$wiremockVersion")
    testImplementation("org.wiremock:wiremock-grpc-extension:$wiremockGrpcExtensionVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersPostgresqlVersion")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("--add-opens=java.base/java.util=ALL-UNNAMED", "--add-opens=java.base/java.lang=ALL-UNNAMED")
}