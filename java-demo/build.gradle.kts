plugins {
    `java-library`
}

dependencies {
    implementation("org.slf4j:slf4j-api:1.7.28")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("ch.qos.logback:logback-core:1.2.3")
    testImplementation(project(":core"))
    testImplementation(project(":hamcrest-support"))
    testImplementation(project(":slf4j-logback"))
    testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.60")
    testImplementation("org.hamcrest:hamcrest:2.1")
    testImplementation("org.hamcrest:hamcrest-library:2.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.5.1")
}