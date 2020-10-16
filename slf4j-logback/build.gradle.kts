import TestLibraries.testImplementHamcrest
import TestLibraries.testImplementKotest

plugins {
    kotlin("jvm")
    id("tech.formatter-kt.formatter")
}

dependencies {
    implementation(project(":core"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(Libraries.slf4jApi)
    implementation(Libraries.logbackClassic)
    implementation(Libraries.logbackCore)
    testImplementation(project(":testing"))
    testImplementation(project(":hamcrest-support"))
    testImplementation(TestLibraries.junitJupiter)
    testImplementHamcrest()
    testImplementKotest()
}