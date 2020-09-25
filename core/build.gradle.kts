import TestLibraries.testImplementHamcrest
import TestLibraries.testImplementKotest

plugins { kotlin("jvm") }

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(TestLibraries.junitJupiter)
    testImplementHamcrest()
    testImplementation(TestLibraries.kotlinReflect)
    testImplementKotest()
    testImplementation(TestLibraries.kotestAssert)
}

kotlin { explicitApi() }