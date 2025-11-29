import com.diffplug.gradle.spotless.SpotlessExtension
import io.gitlab.arturbosch.detekt.Detekt

buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        // Don't like this, but in some way we need to provide gradle flyway script this dependency. And I couldn't
        // figure out a way to do it with a separately defined "configuration" for the flywayMigrate task.
        classpath("org.flywaydb:flyway-database-postgresql:11.14.1")
    }
}

plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"

    id("org.springframework.boot") version "4.0.0"
    // fails with detekt kotlin version mismatch without workaround, just using spring-boot-dependencies in deps now
    // id("io.spring.dependency-management") version "1.1.6"
    id("org.openapi.generator") version "7.8.0"
    id("org.flywaydb.flyway") version "11.14.1" // match versions with spring bom
    id("org.jooq.jooq-codegen-gradle") version "3.19.28" // match spring jooq

    id("jacoco")

    id("io.gitlab.arturbosch.detekt").version("1.23.8")
    id("com.diffplug.spotless").version("8.1.0")
}

group = "com.mtuomiko"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:4.0.0"))
    api(platform("org.testcontainers:testcontainers-bom:1.20.1"))

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-database-postgresql")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    implementation("io.github.oshai:kotlin-logging-jvm:6.0.9")

    implementation("com.jsoizo:kotlin-csv-jvm:1.10.0") // for dataloader csv parsing

    runtimeOnly("org.postgresql:postgresql")

    // spring bom not enough without version, match spring bom version. codegen requires build time driver dep
    jooqCodegen("org.postgresql:postgresql:42.7.8")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-restclient-test")
    testImplementation("org.springframework.boot:spring-boot-starter-jooq-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.mockk:mockk:1.14.6")

    testImplementation("org.testcontainers:junit-jupiter") // from spring bom
    testImplementation("org.testcontainers:postgresql") // from spring bom

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(kotlin("stdlib-jdk8"))
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

openApiGenerate {
    generatorName = "kotlin-spring"
    inputSpec = "$rootDir/src/main/resources/api/api.yml"
    outputDir = "${layout.buildDirectory.get()}/generated"
    apiPackage = "com.mtuomiko.citybikeapp.gen.api"
    modelPackage = "com.mtuomiko.citybikeapp.gen.model"
    configOptions.set(
        mapOf(
            "useSpringBoot3" to "true",
            "interfaceOnly" to "true", // only interfaces, no actual controller code
            // we don't need the swagger annotations, would require additional dependencies to be compilable
            "annotationLibrary" to "none",
            "gradleBuildFile" to "false", // we're not generating an independent project
            "useJakartaEE" to "true",
            "documentationProvider" to "none", // not needed in generated code
            "exceptionHandler" to "false", // we have our own
        ),
    )
    typeMappings.set(mapOf("java.time.OffsetDateTime" to "java.time.Instant"))
}

// For overriding the url in GitHub actions so Flyway gradle task works. Duplicate config, oh well ¯\_(ツ)_/¯
val jdbcUrl =
    System.getenv("DATABASE_CONNECTION_URL") ?: "jdbc:postgresql://host.docker.internal:5432/citybikeapp"

// This is for gradle flyway tasks. Separate from what Spring runs at runtime.
flyway {
    driver = "org.postgresql.Driver"
    url = jdbcUrl
    user = "postgres"
    password = "Hunter2"
    schemas = arrayOf("citybikeapp")
}

jooq {

    configuration {
        jdbc {
            driver = "org.postgresql.Driver"
            url = jdbcUrl
            user = "postgres"
            password = "Hunter2"
        }

        generator {
            name = "org.jooq.codegen.KotlinGenerator"
            database {
                name = "org.jooq.meta.postgres.PostgresDatabase"
                inputSchema = "citybikeapp"
                includes = "citybikeapp.*"
                excludes = "flyway_schema_history"

                forcedTypes {
                    // get the timestamps as Instants, not OffsetDateTime (since using timestamptz)
                    forcedType {
                        name = "INSTANT"
                        includeTypes = "TIMESTAMP.*"
                    }
                }
            }
            target {
                packageName = "com.mtuomiko.citybikeapp.jooq"
            }
        }
    }
}

sourceSets {
    main {
        kotlin {
            srcDir("${layout.buildDirectory.get()}/generated")
        }
    }
}

tasks.jooqCodegen {
    dependsOn(tasks.flywayMigrate)
}

tasks.compileKotlin {
    dependsOn(tasks.jooqCodegen) // dao depends on the jooq code generation results
    dependsOn(tasks.openApiGenerate) // api depends on the openapi generation results
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
                    // exclude any generated jooq code from test reports
                    exclude("**/com/mtuomiko/citybikeapp/jooq/**")
                }
            },
        ),
    )
}

detekt {
    buildUponDefaultConfig = true
    source.setFrom(files("$rootDir/src"))
    config.setFrom(files("$rootDir/detekt.yml"))
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "21"
    reports {
        // failure and console output enough for now
        xml.required.set(false)
        html.required.set(false)
        txt.required.set(false)
        sarif.required.set(false)
        md.required.set(false)
    }
    exclude(
        "*.gradle.kts", // no need for so strict checks on scripts (unused can cause issues, for example)
        "**/test/**",
    )
}

configure<SpotlessExtension> {
    kotlin {
        target("**/*.kt")
        ktlint("1.7.1")
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint("1.7.1")
    }
}
