pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "auth-service"

include(":app")
project(":app").projectDir = file("auth-service-app")

include(":core:model", ":core:port", ":core:use-case")

project(":core").projectDir = file("auth-service-core")
project(":core:model").projectDir = file("auth-service-core/model")
project(":core:port").projectDir = file("auth-service-core/port")
project(":core:use-case").projectDir = file("auth-service-core/use-case")

include(":input:api", ":input:kafka-consumer")
project(":input").projectDir = file("auth-service-input")
project(":input:api").projectDir = file("auth-service-input/api")
project(":input:kafka-consumer").projectDir = file("auth-service-input/kafka-consumer")

include(":output:database", ":output:kafka-producer", ":output:security")
project(":output").projectDir = file("auth-service-output")
project(":output:database").projectDir = file("auth-service-output/database")
project(":output:kafka-producer").projectDir = file("auth-service-output/kafka-producer")
project(":output:security").projectDir = file("auth-service-output/security")
