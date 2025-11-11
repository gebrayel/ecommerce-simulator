plugins {
    // no aplicamos spring boot globalmente
    kotlin("jvm") version "1.9.0" apply false
    id("io.spring.dependency-management") version "1.1.0"
}

allprojects {
    group = "com.tuempresa.ecommerce"
    version = "0.1.0-SNAPSHOT"
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "io.spring.dependency-management")
    // versiones comunes
    extra["springBootVersion"] = "3.5.7"
}
