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
    testImplementation(TestLibraries.kotlinStdLib)
    testImplementation(TestLibraries.hamcrest)
    testImplementation(TestLibraries.hamcrestLibrary)
    testImplementation(TestLibraries.junitJupiter)
}