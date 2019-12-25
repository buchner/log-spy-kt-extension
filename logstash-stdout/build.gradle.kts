plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation(project(":core"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.61")
    implementation("com.google.code.gson:gson:2.8.6")
    testImplementation(project(":testing"))
    testImplementation(project(":hamcrest-support"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.5.1")
    testImplementation("org.hamcrest:hamcrest:2.1")
    testImplementation("org.hamcrest:hamcrest-library:2.1")
    testImplementation("org.slf4j:slf4j-api:1.7.28")
    testImplementation("ch.qos.logback:logback-classic:1.2.3")
    testImplementation("ch.qos.logback:logback-core:1.2.3")
    testImplementation("ch.qos.logback:logback-access:1.2.3")
    testImplementation("net.logstash.logback:logstash-logback-encoder:6.2")
}