import TestLibraries.testImplementHamcrest

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(Libraries.junitApi)
    testImplementation(TestLibraries.junitJupiter)
    testImplementHamcrest()
    testImplementation(TestLibraries.kotlinReflect)
}