import TestLibraries.testImplementHamcrest

plugins {
    `java-library`
}

dependencies {
    implementation(Libraries.slf4jApi)
    implementation(Libraries.logbackClassic)
    implementation(Libraries.logbackCore)
    testImplementation(project(":core"))
    testImplementation(project(":hamcrest-support"))
    testImplementation(project(":slf4j-logback"))
    testImplementHamcrest()
    testImplementation(TestLibraries.junitJupiter)
}