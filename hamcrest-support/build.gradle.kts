plugins {
    kotlin("jvm")
    alias(libs.plugins.ktlint)
}

dependencies {
    api(platform(libs.kotest.bom))
    implementation(project(":core"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(libs.hamcrest)
    implementation(kotlin("reflect"))
    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.framework)
    testImplementation(libs.kotest.property)
    testImplementation(libs.kotest.assertions)
}

kotlin { explicitApi() }
