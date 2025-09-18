import org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES

plugins {
    id("org.springframework.boot")
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.spring")
}

group = "com.github"
version = "1.0.0"

val arrowVersion by extra("1.2.4")
val thymeleafExtrasJava8timeVersion by extra("3.0.4.RELEASE")
val tinkoffApiVersion by extra("1.5")
val ta4jVersion by extra("0.17")
val mapstructVersion by extra("1.6.3")
val springdocOpenapiStarterWebmvcUiVersion by extra("2.2.0")
val kotestVersion by extra("5.9.1")
val kotestExtensionsSpringVersion by extra("1.3.0")
val mockkVersion by extra("1.13.17")
val wiremockVersion by extra("3.12.1")
val wiremockGrpcExtensionVersion by extra("0.10.0")

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(BOM_COORDINATES))

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.retry:spring-retry")
    implementation("io.arrow-kt:arrow-core:$arrowVersion")
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
    testImplementation("org.wiremock:wiremock-standalone:$wiremockVersion")
    testImplementation("org.wiremock:wiremock-grpc-extension-standalone:$wiremockGrpcExtensionVersion")
    testImplementation("org.testcontainers:postgresql")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("--add-opens=java.base/java.util=ALL-UNNAMED", "--add-opens=java.base/java.lang=ALL-UNNAMED")
}