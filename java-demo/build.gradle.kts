import TestLibraries.testImplementHamcrest

plugins {
    `java-library`
    alias(libs.plugins.ktlint)
}

dependencies {
    api(platform(libs.junit.bom))
    implementation(libs.slf4j)
    implementation(libs.logback.classic)
    implementation(libs.logback.core)
    testImplementation(project(":core"))
    testImplementation(project(":junit5-support"))
    testImplementation(project(":hamcrest-support"))
    testImplementation(project(":slf4j-logback"))
    testImplementHamcrest()
    testImplementation(libs.junit.jupiter)
}

tasks.test { configure<JacocoTaskExtension> { isEnabled = false } }
