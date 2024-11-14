dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            plugin("ktlint", "org.jlleitschuh.gradle.ktlint").version("12.1.1")
            library("hamcrest", "org.hamcrest:hamcrest:3.0")
            library("junit-bom", "org.junit", "junit-bom").version("5.11.3")
            library("junit-jupiter-api", "org.junit.jupiter", "junit-jupiter-api").withoutVersion()
            library("junit-jupiter", "org.junit.jupiter", "junit-jupiter").withoutVersion()
            library("kotest-bom", "io.kotest", "kotest-bom").version("5.9.1")
            library("kotest-assertions", "io.kotest", "kotest-assertions-core-jvm").withoutVersion()
            library("kotest-framework", "io.kotest", "kotest-framework-api-jvm").withoutVersion()
            library("kotest-property", "io.kotest", "kotest-property-jvm").withoutVersion()
            library("kotest-runner", "io.kotest", "kotest-runner-junit5-jvm").withoutVersion()
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
