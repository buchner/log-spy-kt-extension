import TestLibraries.testImplementHamcrest
import TestLibraries.testImplementKotest

plugins {
    kotlin("jvm")
    alias(libs.plugins.ktlint)
}

dependencies {
    implementation(project(":core"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(Libraries.slf4jApi)
    implementation(libs.logback.classic)
    implementation(libs.logback.core)
    testImplementation(project(":testing"))
    testImplementation(project(":hamcrest-support"))
    testImplementation(TestLibraries.junitJupiter)
    testImplementHamcrest()
    testImplementKotest()
}
