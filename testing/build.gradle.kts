plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation(project(":core"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":hamcrest-support"))
    implementation("org.junit.jupiter:junit-jupiter:5.5.2")
    implementation("org.hamcrest:hamcrest:2.2")
    implementation("org.hamcrest:hamcrest-library:2.2")
    implementation("org.slf4j:slf4j-api:1.7.30")
}