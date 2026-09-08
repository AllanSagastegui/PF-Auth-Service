dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:port"))

    implementation("org.springframework.security:spring-security-crypto")
    implementation("org.bouncycastle:bcprov-jdk18on:1.80")
    implementation("com.nimbusds:nimbus-jose-jwt:10.0.2")
}
