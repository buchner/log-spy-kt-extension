plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":logstash-stdout-parser"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.61")
    testImplementation(project(":testing"))
    testImplementation(project(":hamcrest-support"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.hamcrest:hamcrest-library:2.2")
    testImplementation("org.slf4j:slf4j-api:1.7.30")
    testImplementation("ch.qos.logback:logback-classic:1.2.3")
    testImplementation("ch.qos.logback:logback-core:1.2.3")
    testImplementation("ch.qos.logback:logback-access:1.2.3")
    testImplementation("net.logstash.logback:logstash-logback-encoder:6.2")
}