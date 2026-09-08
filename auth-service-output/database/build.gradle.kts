dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:port"))

    implementation(libs.ask.core)
    implementation(libs.ask.exception.core)
    implementation(libs.ask.persistence.core)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.boot.starter.validation)

    implementation(libs.r2dbc.pool)
    implementation(libs.spring.boot.starter.data.r2dbc)

    runtimeOnly(libs.r2dbc.postgresql)

}