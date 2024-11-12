import TestLibraries.testImplementHamcrest

plugins {
    `java-library`
    alias(libs.plugins.ktlint)
}

dependencies {
    implementation(Libraries.slf4jApi)
    implementation(libs.logback.classic)
    implementation(libs.logback.core)
    testImplementation(project(":core"))
    testImplementation(project(":junit5-support"))
    testImplementation(project(":hamcrest-support"))
    testImplementation(project(":slf4j-logback"))
    testImplementHamcrest()
    testImplementation(TestLibraries.junitJupiter)
}

tasks.test { configure<JacocoTaskExtension> { isEnabled = false } }
