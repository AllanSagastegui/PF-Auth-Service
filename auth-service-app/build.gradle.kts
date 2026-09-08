import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.getByType
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    alias(libs.plugins.spring.boot)
}

springBoot {
    mainClass.set("pe.ask.auth.MainApplication")
}

dependencies {
    implementation(platform(libs.spring.cloud.dependencies))
    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.data.r2dbc)
    implementation(libs.reactor.kafka)
    implementation(libs.nimbus.jose.jwt)
    implementation(libs.springdoc.openapi)

    implementation(project(":core:model"))
    implementation(project(":core:port"))
    implementation(project(":core:use-case"))

    implementation(project(":input:api"))
    implementation(project(":input:kafka-consumer"))

    implementation(project(":output:database"))
    implementation(project(":output:kafka-producer"))
    implementation(project(":output:security"))

    testImplementation(libs.ask.persistence.core)
}

val bootJarTask = tasks.named<BootJar>("bootJar")

tasks.register<Copy>("explodedJar") {
    group = LifecycleBasePlugin.BUILD_GROUP
    description = "Extracts the executable Spring Boot JAR into build/exploded."

    dependsOn(bootJarTask)

    from(
        bootJarTask.flatMap { it.archiveFile }
            .map { zipTree(it.asFile) }
    )

    into(layout.buildDirectory.dir("exploded"))
}

tasks.named<Jar>("jar") {
    enabled = false
}

bootJarTask.configure {
    archiveFileName.set(
        archiveExtension.map { extension ->
            "${project.parent?.name ?: rootProject.name}.$extension"
        }
    )
}

abstract class VerifyForbiddenDependencies : DefaultTask() {
    @get:Input
    abstract val coordinates: ListProperty<String>

    @get:Input
    abstract val forbiddenCoordinates: SetProperty<String>

    @TaskAction
    fun verify() {
        val violations = coordinates.get().toSet().intersect(forbiddenCoordinates.get())
        check(violations.isEmpty()) {
            "Blocking runtime dependencies detected: ${violations.sorted().joinToString()}"
        }
    }
}

abstract class VerifyReactiveSource : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceFiles: ConfigurableFileCollection

    @get:Input
    abstract val forbiddenRegexes: ListProperty<String>

    @TaskAction
    fun verify() {
        val patterns = forbiddenRegexes.get().map(::Regex)
        val violations = sourceFiles.files.sortedBy { it.path }.flatMap { file ->
            file.readLines().mapIndexedNotNull { index, line ->
                if (patterns.any { pattern -> pattern.containsMatchIn(line) }) {
                    "${file.invariantSeparatorsPath}:${index + 1}: ${line.trim()}"
                } else {
                    null
                }
            }
        }

        check(violations.isEmpty()) {
            "Potentially blocking or manually subscribed code detected:\n${violations.joinToString("\n")}"
        }
    }
}

val forbiddenRuntimeModules = setOf(
    "org.springframework.boot:spring-boot-starter-web",
    "org.springframework.boot:spring-boot-starter-tomcat",
    "org.springframework.boot:spring-boot-starter-jdbc",
    "org.springframework.boot:spring-boot-starter-data-jpa",
    "org.hibernate.orm:hibernate-core",
    "com.zaxxer:HikariCP",
    "org.postgresql:postgresql"
)

val runtimeCoordinates = configurations.named("runtimeClasspath").flatMap { configuration ->
    configuration.incoming.artifacts.resolvedArtifacts.map { artifacts ->
        artifacts.mapNotNull { artifact ->
            val component = artifact.id.componentIdentifier
            if (component is ModuleComponentIdentifier) {
                "${component.group}:${component.module}"
            } else {
                null
            }
        }.sorted()
    }
}

val verifyNoBlockingRuntimeDependencies = tasks.register<VerifyForbiddenDependencies>(
    "verifyNoBlockingRuntimeDependencies"
) {
    group = "verification"
    description = "Fails when known blocking Servlet/JDBC/JPA dependencies are present at runtime."
    coordinates.set(runtimeCoordinates)
    forbiddenCoordinates.set(forbiddenRuntimeModules)
}

val verifyReactiveSource = tasks.register<VerifyReactiveSource>("verifyReactiveSource") {
    group = "verification"
    description = "Fails when obvious blocking or manual-subscription calls exist in production Java source."
    rootProject.subprojects.forEach { sub ->
        sub.plugins.withId("java") {
            val javaPluginExtension = sub.extensions.getByType<JavaPluginExtension>()
            sourceFiles.from(javaPluginExtension.sourceSets.getByName("main").allJava)
        }
    }
    forbiddenRegexes.set(
        listOf(
            "\\.block\\s*\\(",
            "\\.blockFirst\\s*\\(",
            "\\.blockLast\\s*\\(",
            "\\.subscribe\\s*\\(",
            "Thread\\.sleep\\s*\\(",
            "Future\\.get\\s*\\(",
            "\\.join\\s*\\("
        )
    )
}

tasks.named("check") {
    dependsOn(
        verifyNoBlockingRuntimeDependencies,
        verifyReactiveSource
    )
}
