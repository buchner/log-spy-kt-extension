plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":core"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.slf4j:slf4j-api:1.7.28")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("ch.qos.logback:logback-core:1.2.3")
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:1.3.50")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.5.1")
    testImplementation("org.hamcrest:hamcrest:2.1")
    testImplementation("org.hamcrest:hamcrest-library:2.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.1")
}