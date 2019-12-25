plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation(project(":core"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.hamcrest:hamcrest:2.2")
    implementation("org.hamcrest:hamcrest-library:2.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.61")
}