dependencies {
    implementation(project(":core:port"))

    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.boot.starter.validation)
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:3.1.1")
}
