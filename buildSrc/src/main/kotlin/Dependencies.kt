object Libraries {
    internal object Versions {
        const val kotlin = "1.5.0"
        const val logback = "1.2.3"
    }

    const val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"
}

object TestLibraries {
    const val kotlinReflect = Libraries.kotlinReflect
}