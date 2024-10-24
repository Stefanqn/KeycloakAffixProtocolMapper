import java.net.URI

plugins {
  `maven-publish`
  `java-library`
}

group = "utils.keycloak"

val keycloakVersion = "25.0.6"
val javaVersion = 17 // current keycloaks java version
val slf4jVersion = "2.0.6"

version = System.getenv("GITHUB_TAG").let { tag ->
  when {
    tag == null -> "0.0.0"
    tag.startsWith("v", true) -> tag.drop(1)
    else -> tag
  }
}

tasks.jar {
  archiveBaseName = "affix-role-protocol-mapper" // e.g. affix-protocol-mapper-0.1.0.jar
  project.version = "" //remove version from jar - for renovate
  manifest {
    attributes(
      mapOf(
        "Implementation-Title" to project.name,
        "Implementation-Version" to rootProject.version
      )
    )
  }
}

dependencies {
  implementation(platform("org.keycloak.bom:keycloak-spi-bom:${keycloakVersion}"))
  compileOnly("org.keycloak:keycloak-core")
  compileOnly("org.keycloak:keycloak-services:${keycloakVersion}")
  compileOnly("org.keycloak:keycloak-server-spi")

  compileOnly("org.slf4j:slf4j-api:${slf4jVersion}")

  testImplementation(libs.junit.jupiter)

  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(javaVersion)
  }
}

tasks.named<Test>("test") {
  useJUnitPlatform()
}

repositories {
  mavenCentral()
}


// ###### maven release to gh-packages
//publishing { // https://docs.github.com/en/actions/use-cases-and-examples/publishing-packages/publishing-java-packages-with-gradle
//    repositories {
//// for local testing
////        maven {
////            name = "localRepo"
////            url = layout.buildDirectory.dir("repo").get().asFile.toURI()
////        }
//        maven {
//            name = "GitHubPackages"
//            url = URI.create("https://maven.pkg.github.com/Stefanqn/KeycloakAffixProtocolMapper")
//            credentials {
//                username = System.getenv("GITHUB_ACTOR")
//                password = System.getenv("GITHUB_TOKEN")
//            }
//        }
//    }
//    publications {
//        create<MavenPublication>("maven") {
//            from(components["java"])
//        }
//    }
//}
