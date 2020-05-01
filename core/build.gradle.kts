plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(Libraries.junitApi)
    testImplementation(TestLibraries.junitJupiter)
    testImplementation(TestLibraries.hamcrest)
    testImplementation(TestLibraries.hamcrestLibrary)
    testImplementation(TestLibraries.kotlinReflect)
}