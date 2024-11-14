import TestLibraries.testImplementHamcrest
import TestLibraries.testImplementKotest

plugins {
    kotlin("jvm")
    alias(libs.plugins.ktlint)
}

dependencies {
    api(platform(libs.junit.bom))
    implementation(project(":core"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(libs.slf4j)
    implementation(libs.logback.classic)
    implementation(libs.logback.core)
    testImplementation(project(":testing"))
    testImplementation(project(":hamcrest-support"))
    testImplementation(libs.junit.jupiter)
    testImplementHamcrest()
    testImplementKotest()
}
