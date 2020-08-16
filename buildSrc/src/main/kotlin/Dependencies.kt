import org.gradle.api.artifacts.dsl.DependencyHandler

object Libraries {
    internal object Versions {
        const val antlr = "4.8-1"
        const val gson = "2.8.6"
        const val hamcrest = "2.2"
        const val junit = "5.6.2"
        const val kotlin = "1.3.72"
        const val logback = "1.2.3"
        const val slf4j = "1.7.30"
    }

    const val antlr = "org.antlr:antlr4:${Versions.antlr}"
    const val antlrRuntime = "org.antlr:antlr4-runtime:${Versions.antlr}"
    const val gson = "com.google.code.gson:gson:${Versions.gson}"
    const val hamcrest = "org.hamcrest:hamcrest:${Versions.hamcrest}"
    const val hamcrestLibrary = "org.hamcrest:hamcrest-library:${Versions.hamcrest}"
    const val junitApi = "org.junit.jupiter:junit-jupiter-api:${Versions.junit}"
    const val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"
    const val logbackClassic = "ch.qos.logback:logback-classic:${Versions.logback}"
    const val logbackCore = "ch.qos.logback:logback-core:${Versions.logback}"
    const val slf4jApi = "org.slf4j:slf4j-api:${Versions.slf4j}"
}

object TestLibraries {
    private object Versions {
        const val kotest = "4.1.3"
        const val logstashEncoder = "6.3"
    }

    const val hamcrest = Libraries.hamcrest
    const val hamcrestLibrary = Libraries.hamcrestLibrary
    const val junitJupiter = "org.junit.jupiter:junit-jupiter:${Libraries.Versions.junit}"
    const val kotestRunner = "io.kotest:kotest-runner-junit5-jvm:${Versions.kotest}"
    const val kotestProperty = "io.kotest:kotest-property:${Versions.kotest}"
    const val kotestAssert = "io.kotest:kotest-assertions-core:${Versions.kotest}"
    const val kotlinReflect = Libraries.kotlinReflect
    const val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Libraries.Versions.kotlin}"
    const val logbackAccess = "ch.qos.logback:logback-access:${Libraries.Versions.logback}"
    const val logbackClassic = Libraries.logbackClassic
    const val logbackCore = Libraries.logbackCore
    const val logstashEncoder = "net.logstash.logback:logstash-logback-encoder:${Versions.logstashEncoder}"
    const val slf4jApi = Libraries.slf4jApi

    fun DependencyHandler.testImplementHamcrest() {
        add("testImplementation", hamcrest)
        add("testImplementation", hamcrestLibrary)
    }

    fun DependencyHandler.testImplementKotest() {

        add("testImplementation", kotestRunner)
        add("testImplementation", kotestProperty)
    }
}