plugins {
    `java-library`
}

version = "0.1.0"

tasks.jar {
//    archiveBaseName = "affix-protocol-mapper"
    archiveFileName = "affix-protocol-mapper.jar"
    manifest {
        attributes(
            mapOf(
                "Implementation-Title" to project.name,
                "Implementation-Version" to rootProject.version
            )
        )
    }
}



repositories {
    mavenCentral()
}

val keycloakVersion = "25.0.6"
val javaVersion = 17 // current keycloaks java version
val slf4jVersion = "2.0.6"

dependencies {
    testImplementation(libs.junit.jupiter)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation(platform("org.keycloak.bom:keycloak-spi-bom:${keycloakVersion}"))
//  implementation(platform("org.keycloak.bom:keycloak-bom-parent:${keycloakVersion}"))
//  implementation(platform("org.keycloak.bom:keycloak-misc-bom:${keycloakVersion}"))
//  implementation(platform("org.keycloak.bom:keycloak-adapter-bom:${keycloakVersion}"))
    compileOnly("org.keycloak:keycloak-core")
    compileOnly("org.keycloak:keycloak-services:${keycloakVersion}")
    compileOnly("org.keycloak:keycloak-server-spi")
//  compileOnly("org.keycloak:keycloak-server-spi-private:${keycloakVersion}")

//  compileOnly("org.projectlombok:lombok")
    compileOnly("org.slf4j:slf4j-api:${slf4jVersion}")
//  compileOnly("org.slf4j:slf4j-reload4j:${slf4jVersion}")


//
//  api(libs.commons.math3)     // This dependency is exported to consumers, that is to say found on their compile classpath.
//  implementation(libs.guava) // // This dependency is used internally, and not exposed to consumers on their own compile classpath.
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
