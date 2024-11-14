import org.gradle.api.artifacts.dsl.DependencyHandler

object Libraries {
    internal object Versions {
        const val hamcrest = "2.2"
        const val kotlin = "1.5.0"
        const val logback = "1.2.3"
    }

    const val hamcrest = "org.hamcrest:hamcrest:${Versions.hamcrest}"
    const val hamcrestLibrary = "org.hamcrest:hamcrest-library:${Versions.hamcrest}"
    const val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"
}

object TestLibraries {
    private object Versions {
        const val kotest = "4.6.0"
    }

    const val hamcrest = Libraries.hamcrest
    const val hamcrestLibrary = Libraries.hamcrestLibrary
    const val kotestRunner = "io.kotest:kotest-runner-junit5-jvm:${Versions.kotest}"
    const val kotestProperty = "io.kotest:kotest-property:${Versions.kotest}"
    const val kotestAssert = "io.kotest:kotest-assertions-core:${Versions.kotest}"
    const val kotlinReflect = Libraries.kotlinReflect

    fun DependencyHandler.testImplementHamcrest() {
        add("testImplementation", hamcrest)
        add("testImplementation", hamcrestLibrary)
    }

    fun DependencyHandler.testImplementKotest() {

        add("testImplementation", kotestRunner)
        add("testImplementation", kotestProperty)
    }
}