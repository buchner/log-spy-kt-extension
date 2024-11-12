dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            plugin("ktlint", "org.jlleitschuh.gradle.ktlint").version("12.1.1")
        }
    }
}

rootProject.name = "log-spy-kt-extension"
include("core")
include("testing")
include("slf4j-logback")
include("hamcrest-support")
include("junit5-support")
include("java-demo")
