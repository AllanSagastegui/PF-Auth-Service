import org.gradle.api.GradleException
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.util.Properties

plugins {
    java
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.freefair.lombok) apply false
    id("org.sonarqube") version "7.5.0.8588"
}


allprojects {
    repositories {
        mavenCentral()

        maven {
            url = uri("https://jitpack.io")
        }
    }
}


val catalog = libs

subprojects {

    pluginManager.apply(JavaPlugin::class.java)
    pluginManager.apply(catalog.plugins.freefair.lombok.get().pluginId)
    pluginManager.apply("jacoco")

    java {
        toolchain {
            languageVersion.set(
                JavaLanguageVersion.of(21)
            )
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"

        options.compilerArgs.addAll(
            listOf(
                "-Xlint:unchecked",
                "-Xlint:deprecation",
                "-parameters"
            )
        )
    }

    dependencies {
        implementation(platform(catalog.spring.boot.dependencies))
        implementation(catalog.reactor.core)
        implementation(catalog.reactor.extra)
        implementation(catalog.jackson.core)
        implementation(catalog.jackson.databind)
        implementation(catalog.jackson.jsr310)
        implementation(catalog.slf4j.api)
        implementation(catalog.mapstruct)

        annotationProcessor(catalog.mapstruct.processor)
        annotationProcessor(catalog.lombok.mapstruct.binding)

        testImplementation(platform(catalog.spring.boot.dependencies))
        testImplementation(catalog.spring.boot.starter.test)
        testImplementation(catalog.reactor.test)
        testImplementation(catalog.blockhound.junit)
        testImplementation(catalog.arch.unit)

        testRuntimeOnly(catalog.junit.platform.launcher)
    }

    tasks.withType<Test>().configureEach {

        useJUnitPlatform()

        jvmArgs(
            "-XX:+AllowRedefinitionToAddDeleteMethods"
        )
    }

    extensions.configure<JacocoPluginExtension> {

        toolVersion = "0.8.15"
    }


    tasks.named<JacocoReport>("jacocoTestReport") {

        dependsOn(
            tasks.named<Test>("test")
        )

        reports {
            xml.required.set(true)
            html.required.set(true)
            csv.required.set(false)
        }
    }
}

val sonarPropertiesFile =
    rootProject.file("sonar.properties")

val sonarConfiguration = Properties().apply {

    sonarPropertiesFile.inputStream().use { inputStream ->
        load(inputStream)
    }
}


sonar {
    properties {
        sonarConfiguration
            .stringPropertyNames()
            .forEach { key ->

                property(
                    key,
                    sonarConfiguration.getProperty(key)
                )
            }

        val sonarHostUrl =
            providers
                .environmentVariable("SONAR_HOST_URL")
                .orNull
                ?.takeIf { it.isNotBlank() }
                ?: "http://localhost:9000"

        property(
            "sonar.host.url",
            sonarHostUrl
        )
    }
}

tasks.named("sonar") {

    dependsOn(
        subprojects.map { subproject ->
            subproject.tasks.named("jacocoTestReport")
        }
    )
}

tasks.register("quality") {

    group = "verification"

    description =
        "Runs tests, JaCoCo coverage and SonarQube analysis."

    dependsOn("sonar")
}