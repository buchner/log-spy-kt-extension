plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.junit.jupiter:junit-jupiter-api:5.5.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.5.1")
    testImplementation("org.hamcrest:hamcrest:2.1")
    testImplementation("org.hamcrest:hamcrest-library:2.1")
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:1.3.50")
}