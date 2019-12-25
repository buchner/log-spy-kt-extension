plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation(project(":core"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.slf4j:slf4j-api:1.7.28")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("ch.qos.logback:logback-core:1.2.3")
    testImplementation(project(":testing"))
    testImplementation(project(":hamcrest-support"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
    testImplementation("org.hamcrest:hamcrest:2.1")
    testImplementation("org.hamcrest:hamcrest-library:2.1")
}