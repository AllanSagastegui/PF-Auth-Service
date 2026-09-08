dependencies {
    implementation(project(":core:port"))

    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.springdoc.openapi)
}
