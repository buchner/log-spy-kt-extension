import org.gradle.api.artifacts.dsl.DependencyHandler

object Libraries {
    internal object Versions {
        const val kotlin = "1.5.0"
        const val logback = "1.2.3"
    }

    const val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"
}

object TestLibraries {
    private object Versions {
        const val kotest = "4.6.0"
    }

    const val kotestRunner = "io.kotest:kotest-runner-junit5-jvm:${Versions.kotest}"
    const val kotestProperty = "io.kotest:kotest-property:${Versions.kotest}"
    const val kotestAssert = "io.kotest:kotest-assertions-core:${Versions.kotest}"
    const val kotlinReflect = Libraries.kotlinReflect

    fun DependencyHandler.testImplementKotest() {

        add("testImplementation", kotestRunner)
        add("testImplementation", kotestProperty)
    }
}