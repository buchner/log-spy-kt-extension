plugins {
    kotlin("jvm")
    alias(libs.plugins.ktlint)
}

dependencies {
    implementation(project(":core"))
    implementation(project(":junit5-support"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":hamcrest-support"))
    implementation(TestLibraries.junitJupiter)
    implementation(TestLibraries.hamcrest)
    implementation(TestLibraries.hamcrestLibrary)
    implementation(libs.slf4j)
    implementation(TestLibraries.kotestRunner)
    implementation(TestLibraries.kotestAssert)
}

tasks.test { configure<JacocoTaskExtension> { isEnabled = false } }
