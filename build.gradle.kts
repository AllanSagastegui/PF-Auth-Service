import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    java
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.freefair.lombok) apply false
}

allprojects {
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

val catalog = libs

subprojects {
    pluginManager.apply(JavaPlugin::class.java)
    pluginManager.apply(catalog.plugins.freefair.lombok.get().pluginId)

    java {
        toolchain {
            languageVersion.set(
                JavaLanguageVersion.of(21)
            )
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation", "-parameters"))
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
        jvmArgs("-XX:+AllowRedefinitionToAddDeleteMethods")
    }
}
