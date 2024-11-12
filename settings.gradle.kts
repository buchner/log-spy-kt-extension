dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            plugin("ktlint", "org.jlleitschuh.gradle.ktlint").version("12.1.1")
            version("logback", "1.3.14")
            library("logback-core", "ch.qos.logback", "logback-core").versionRef("logback")
            library("logback-classic", "ch.qos.logback", "logback-classic").versionRef("logback")
            library("slf4j", "org.slf4j:slf4j-api:2.0.16")
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
