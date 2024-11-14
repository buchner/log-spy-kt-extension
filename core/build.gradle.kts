import TestLibraries.testImplementKotest

plugins {
    kotlin("jvm")
    alias(libs.plugins.ktlint)
}

dependencies {
    api(platform(libs.junit.bom))
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.hamcrest)
    testImplementation(TestLibraries.kotlinReflect)
    testImplementKotest()
    testImplementation(TestLibraries.kotestAssert)
}

kotlin { explicitApi() }
