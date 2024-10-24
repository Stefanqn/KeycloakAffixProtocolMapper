plugins {
  // Apply the foojay-resolver plugin to allow automatic download of JDKs
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "keycloak-affix-role-protocol-mapper-root"

include("lib")
project(":lib").name = "keycloak-affix-role-protocol-mapper"
