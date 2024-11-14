import TestLibraries.testImplementKotest

plugins {
    kotlin("jvm")
    alias(libs.plugins.ktlint)
}

dependencies {
    implementation(project(":core"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(libs.hamcrest)
    implementation(Libraries.kotlinReflect)
    testImplementKotest()
    testImplementation(TestLibraries.kotestAssert)
}

kotlin { explicitApi() }
