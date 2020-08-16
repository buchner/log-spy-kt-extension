import TestLibraries.testImplementKotest

plugins { kotlin("jvm") }

dependencies {
    implementation(project(":core"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(Libraries.hamcrest)
    implementation(Libraries.hamcrestLibrary)
    implementation(Libraries.kotlinReflect)
    testImplementKotest()
    testImplementation(TestLibraries.kotestAssert)
}