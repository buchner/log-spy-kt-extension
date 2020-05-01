plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":core"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(Libraries.slf4jApi)
    implementation(Libraries.logbackClassic)
    implementation(Libraries.logbackCore)
    testImplementation(project(":testing"))
    testImplementation(project(":hamcrest-support"))
    testImplementation(TestLibraries.junitJupiter)
    testImplementation(TestLibraries.hamcrest)
    testImplementation(TestLibraries.hamcrestLibrary)
}