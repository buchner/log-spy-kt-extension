plugins {
    kotlin("jvm")
    id("tech.formatter-kt.formatter")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":junit5-support"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":hamcrest-support"))
    implementation(TestLibraries.junitJupiter)
    implementation(TestLibraries.hamcrest)
    implementation(TestLibraries.hamcrestLibrary)
    implementation(TestLibraries.slf4jApi)
}

tasks.test { configure<JacocoTaskExtension> { isEnabled = false } }

tasks.publish { enabled = false }