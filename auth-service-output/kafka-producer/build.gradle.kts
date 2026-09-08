dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:port"))

    implementation(libs.reactor.kafka)
}
