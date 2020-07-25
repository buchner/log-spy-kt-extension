import TestLibraries.testImplementHamcrest

plugins {
    `java-library`
}

dependencies {
    implementation(Libraries.slf4jApi)
    implementation(Libraries.logbackClassic)
    implementation(Libraries.logbackCore)
    testImplementation(project(":core"))
    testImplementation(project(":junit5-support"))
    testImplementation(project(":hamcrest-support"))
    testImplementation(project(":slf4j-logback"))
    testImplementHamcrest()
    testImplementation(TestLibraries.junitJupiter)
}

tasks.test {
    configure<JacocoTaskExtension> {
        isEnabled = false
    }
}