import TestLibraries.testImplementHamcrest
import TestLibraries.testImplementKotest

plugins {
    kotlin("jvm")
    id("tech.formatter-kt.formatter")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(TestLibraries.junitJupiter)
    testImplementHamcrest()
    testImplementation(TestLibraries.kotlinReflect)
    testImplementKotest()
    testImplementation(TestLibraries.kotestAssert)
}

kotlin { explicitApi() }