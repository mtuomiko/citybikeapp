import com.diffplug.gradle.spotless.SpotlessExtension
import io.gitlab.arturbosch.detekt.Detekt
import io.micronaut.gradle.docker.MicronautDockerfile
import org.jooq.meta.jaxb.Property as JooqProperty

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.6.21")
    id("org.jetbrains.kotlin.kapt").version("1.6.21")
    id("org.jetbrains.kotlin.plugin.allopen").version("1.6.21")
    id("com.github.johnrengelman.shadow").version("7.1.2")
    id("io.micronaut.application").version("3.7.0")
    id("nu.studer.jooq").version("8.1")

    id("io.gitlab.arturbosch.detekt").version("1.22.0")
    id("com.diffplug.spotless").version("6.12.0")
    id("jacoco")
}

version = "0.1"
group = "com.mtuomiko"

val kotlinVersion = project.properties["kotlinVersion"]
repositories {
    mavenCentral()
}

dependencies {
    kapt("io.micronaut:micronaut-http-validation")
    kapt("io.micronaut.openapi:micronaut-openapi")
    kapt("io.micronaut.serde:micronaut-serde-processor")
    kapt("info.picocli:picocli-codegen")
    kapt("org.mapstruct:mapstruct-processor:1.5.3.Final")

    jooqGenerator("com.github.sabomichal:jooq-meta-postgres-flyway:1.0.9")

    implementation("info.picocli:picocli")
    implementation("io.micronaut.picocli:micronaut-picocli")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-management")
    implementation("io.micronaut.flyway:micronaut-flyway")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("io.micronaut.sql:micronaut-jdbc-hikari")
    implementation("io.micronaut.sql:micronaut-jooq")
    implementation("io.swagger.core.v3:swagger-annotations")
    implementation("jakarta.annotation:jakarta.annotation-api")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("io.micronaut:micronaut-validation")
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.7.0")
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.23") // match logback v1 version from micronaut bom
    implementation("io.micronaut.openapi:micronaut-openapi") { // include annotations
        exclude(group = "org.slf4j", module = "slf4j-nop") // for some reason openapi tries to include this
    }
    implementation("org.mapstruct:mapstruct:1.5.3.Final")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core") // match supported kotlin version

    compileOnly("jakarta.persistence:jakarta.persistence-api:3.1.0")
    compileOnly("com.google.code.findbugs:jsr305") // "unknown enum constant When.MAYBE" warning on kaptKotlin task

    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")

    testImplementation("org.assertj:assertj-core")
    testImplementation("io.mockk:mockk")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:testcontainers")

    // "unknown enum constant GenerationType.IDENTITY" warning on kaptTestKotlin task
    testCompileOnly("jakarta.persistence:jakarta.persistence-api:3.1.0")
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
    resolutionStrategy {
        dependencySubstitution {
            substitute(module("io.micronaut:micronaut-jackson-databind"))
                .using(module("io.micronaut.serde:micronaut-serde-jackson:1.3.3"))
        }
        force("com.github.ben-manes.caffeine:caffeine:3.0.3") // jdbi has direct dependency
    }
}

detekt {
    buildUponDefaultConfig = true
    source = files("$rootDir/src")
    config = files("$rootDir/detekt.yml")
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "17"
    reports { // failure and console output enough for now
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

tasks.named<MicronautDockerfile>("dockerfile") {
    baseImage.set("eclipse-temurin:17.0.5_8-jre-alpine")
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.56".toBigDecimal()
            }
        }
    }
}

tasks.jacocoTestReport {
    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude("**/com/mtuomiko/citybikeapp/jooq/**")
                }
            }
        )
    )
}

jooq {
    version.set("3.17.6")

    configurations {
        create("main") {
            jooqConfiguration.apply {
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = "com.github.sabomichal.jooq.PostgresDDLDatabase"
                        inputSchema = "citybikeapp"
                        includes = "citybikeapp.*"
                        excludes = "flyway_schema_history"
                        properties.addAll(
                            listOf(
                                JooqProperty().apply {
                                    key = "locations"
                                    value = "src/main/resources/db/migration"
                                },
                                JooqProperty().apply {
                                    key = "dockerImage"
                                    value = "postgres:14"
                                },
                                JooqProperty().apply {
                                    key = "defaultSchema"
                                    value = "citybikeapp"
                                }
                            )
                        )
                        forcedTypes.addAll(
                            listOf(
                                org.jooq.meta.jaxb.ForcedType().apply {
                                    name = "INSTANT"
                                    includeTypes = "TIMESTAMP.*"
                                }
                            )
                        )
                    }
                    target.apply {
                        packageName = "com.mtuomiko.citybikeapp.jooq"
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
}
