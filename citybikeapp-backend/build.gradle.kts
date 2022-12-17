import com.diffplug.gradle.spotless.SpotlessExtension
import io.gitlab.arturbosch.detekt.Detekt

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.6.21")
    id("org.jetbrains.kotlin.kapt").version("1.6.21")
    id("org.jetbrains.kotlin.plugin.allopen").version("1.6.21")
    id("com.github.johnrengelman.shadow").version("7.1.2")
    id("io.micronaut.application").version("3.6.3")

    id("io.gitlab.arturbosch.detekt").version("1.22.0")
    id("com.diffplug.spotless").version("6.12.0")
}

version = "0.1"
group = "com.mtuomiko"

val kotlinVersion = project.properties["kotlinVersion"]
repositories {
    mavenCentral()
}

dependencies {
    kapt("io.micronaut.data:micronaut-data-processor")
    kapt("io.micronaut:micronaut-http-validation")
    kapt("io.micronaut.openapi:micronaut-openapi")
    kapt("io.micronaut.serde:micronaut-serde-processor")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-management")
    implementation("io.micronaut.data:micronaut-data-jdbc")
    implementation("io.micronaut.flyway:micronaut-flyway")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("io.micronaut.sql:micronaut-jdbc-hikari")
    implementation("io.swagger.core.v3:swagger-annotations")
    implementation("jakarta.annotation:jakarta.annotation-api")
    compileOnly("jakarta.persistence:jakarta.persistence-api:3.0.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:testcontainers")
    implementation("io.micronaut:micronaut-validation")

    implementation("io.github.microutils:kotlin-logging-jvm:2.1.23") // match logback version

    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
}

application {
    mainClass.set("com.mtuomiko.citybikeapp.Application")
}
java {
    sourceCompatibility = JavaVersion.toVersion("17")
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
}
graalvmNative.toolchainDetection.set(false)
micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("com.mtuomiko.citybikeapp.*")
    }
}

configurations.all {
    resolutionStrategy.dependencySubstitution {
        substitute(module("io.micronaut:micronaut-jackson-databind"))
            .using(module("io.micronaut.serde:micronaut-serde-jackson:1.3.3"))
    }
}

detekt {
    buildUponDefaultConfig = true
    source = files(rootDir)
    config = files("$rootDir/detekt.yml")
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "17"
    reports {
        xml.required.set(false)
        html.required.set(false)
        txt.required.set(false)
        sarif.required.set(false)
        md.required.set(false)
    }
    exclude(
        "*.gradle.kts", // no need for so strict checks on scripts (unused can cause issues, for example)
        "**/test/**"
    )
}

configure<SpotlessExtension> {
    kotlin {
        target("**/*.kt")
        ktlint("0.47.1")
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint("0.47.1")
    }
}
