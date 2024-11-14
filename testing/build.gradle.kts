plugins {
    kotlin("jvm")
    alias(libs.plugins.ktlint)
}

dependencies {
    api(platform(libs.junit.bom))
    api(platform(libs.kotest.bom))
    implementation(project(":core"))
    implementation(project(":junit5-support"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":hamcrest-support"))
    implementation(libs.junit.jupiter)
    implementation(libs.hamcrest)
    implementation(libs.slf4j)
    implementation(libs.kotest.runner)
    implementation(libs.kotest.framework)
    implementation(libs.kotest.assertions)
}

tasks.test { configure<JacocoTaskExtension> { isEnabled = false } }
