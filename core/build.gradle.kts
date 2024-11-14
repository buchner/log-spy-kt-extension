plugins {
    kotlin("jvm")
    alias(libs.plugins.ktlint)
}

dependencies {
    api(platform(libs.junit.bom))
    api(platform(libs.kotest.bom))
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.hamcrest)
    testImplementation(kotlin("reflect"))
    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.framework)
    testImplementation(libs.kotest.property)
    testImplementation(libs.kotest.assertions)
}

kotlin { explicitApi() }
