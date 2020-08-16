import TestLibraries.testImplementHamcrest
import TestLibraries.testImplementKotest

plugins { kotlin("jvm") }

dependencies {
    implementation(project(":core"))
    implementation(project(":logstash-stdout-parser"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(Libraries.gson)
    implementation(Libraries.kotlinReflect)
    testImplementation(project(":testing"))
    testImplementation(project(":hamcrest-support"))
    testImplementation(TestLibraries.junitJupiter)
    testImplementHamcrest()
    testImplementKotest()
    testImplementation(TestLibraries.slf4jApi)
    testImplementation(TestLibraries.logbackClassic)
    testImplementation(TestLibraries.logbackCore)
    testImplementation(TestLibraries.logbackAccess)
    testImplementation(TestLibraries.logstashEncoder)
}