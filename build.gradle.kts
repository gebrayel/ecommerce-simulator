import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.math.BigDecimal

plugins {
    // no aplicamos spring boot globalmente
    kotlin("jvm") version "1.9.0" apply false
    id("io.spring.dependency-management") version "1.1.7"
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
    apply(plugin = "jacoco")

    // versiones comunes
    extra["springBootVersion"] = "3.5.7"

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        finalizedBy("jacocoTestReport", "jacocoTestCoverageVerification")
    }

    tasks.withType<JacocoReport>().configureEach {
        dependsOn(tasks.withType<Test>())
        val coverageExclusions = listOf(
            "**/config/**",
            "**/infrastructure/**",
            "**/*Application*",
            "**/DataInitializer*"
        )
        classDirectories.setFrom(classDirectories.files.map {
            fileTree(it) {
                exclude(coverageExclusions)
            }
        })
        reports {
            xml.required.set(true)
            csv.required.set(false)
            html.required.set(true)
        }
    }

    tasks.withType<JacocoCoverageVerification>().configureEach {
        dependsOn(tasks.withType<Test>())
        val coverageExclusions = listOf(
            "**/config/**",
            "**/infrastructure/**",
            "**/*Application*",
            "**/DataInitializer*"
        )
        classDirectories.setFrom(classDirectories.files.map {
            fileTree(it) {
                exclude(coverageExclusions)
            }
        })
        violationRules {
            rule {
                element = "CLASS"
                includes = listOf(
                    "${project.group}.**.application.service.*",
                    "${project.group}.**.application.service.security.*"
                )
                limit {
                    counter = "LINE"
                    value = "COVEREDRATIO"
                    minimum = BigDecimal("0.80")
                }
            }
        }
    }
}
