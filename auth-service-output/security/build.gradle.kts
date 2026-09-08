dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:port"))

    implementation(libs.spring.security.crypto)
    implementation(libs.bouncycastle)
    implementation(libs.nimbus.jose.jwt)
}
