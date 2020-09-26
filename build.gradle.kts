
import java.io.ByteArrayOutputStream
import kotlin.streams.toList
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript { repositories { mavenCentral() } }

plugins {
    kotlin("jvm") version "1.4.10"
    jacoco
    `java-library`
    `maven-publish`
    signing
    id("com.github.nbaztec.coveralls-jacoco") version "1.0.5"
    id("tech.formatter-kt.formatter") version "0.6.1"
    id("org.jetbrains.dokka") version "1.4.0-rc"
}

allprojects {
    group = "net.torommo.logspy"
    version = "${gitVersion("0.8.0")}-SNAPSHOT"

    repositories {
        mavenCentral()
        jcenter()
    }
}

tasks.register<JacocoReport>("codeCoverageReport") {
    subprojects {
        val subproject = this
        subproject.plugins
            .withType<JacocoPlugin>()
            .configureEach {
                subproject.tasks
                    .matching {
                        it.extensions.findByType<JacocoTaskExtension>()?.isEnabled ?: false
                    }
                    .configureEach {
                        val testTask = this
                        sourceSets(subproject.sourceSets.main.get())
                        executionData(testTask)
                    }
                subproject.tasks
                    .matching { it.extensions.findByType<JacocoTaskExtension>() != null }
                    .forEach { rootProject.tasks["codeCoverageReport"].dependsOn(it) }
            }
    }

    reports {
        xml.isEnabled = true
        xml.destination = file("${project.rootProject.buildDir}/reports/jacoco/report.xml")
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "jacoco")

    tasks.withType<KotlinCompile> { kotlinOptions.jvmTarget = "1.8" }

    tasks.withType<Test> { useJUnitPlatform() }

    tasks.withType<GenerateModuleMetadata> { enabled = false }

    val dokkaJar by
        tasks.creating(Jar::class) {
            group = JavaBasePlugin.DOCUMENTATION_GROUP
            description = "Assembles Kotlin documentation with Dokka."
            archiveClassifier.set("javadoc")
            from("$buildDir/dokka/javadoc")
        }
    dokkaJar.dependsOn(tasks.dokkaJavadoc)
    artifacts.add("archives", dokkaJar)

    val sourcesJar by
        tasks.creating(Jar::class) {
            group = JavaBasePlugin.DOCUMENTATION_GROUP
            description = "Assembles sources."
            archiveClassifier.set("sources")
            from(sourceSets.getByName("main").allSource)
        }
    artifacts.add("archives", sourcesJar)

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                artifact(dokkaJar)
                artifact(sourcesJar)
                pom {
                    url.set("https://github.com/buchner/log-spy-kt-extension")
                    licenses {
                        license {
                            name.set("LGPL-3.0-or-later")
                            url.set("https://www.gnu.org/licenses/lgpl-3.0.en.html")
                        }
                    }
                    developers {
                        developer {
                            id.set("buchner")
                            name.set("Björn Buchner")
                        }
                    }
                    scm {
                        connection.set(
                            "scm:git:https://github.com/buchner/log-spy-kt-extension.git"
                        )
                        url.set("https://github.com/buchner/log-spy-kt-extension")
                    }
                }
            }
        }
        repositories {
            maven {
                name = "sonatype"
                url = uri("https://oss.sonatype.org/content/repositories/snapshots")
                credentials {
                    username = project.findProperty("mavenUser") as String?
                    password = project.findProperty("mavenPassword") as String?
                }
            }
        }
    }

    signing {
        val signingKey = project.findProperty("signingKey") as String?
        val signingPassword = project.findProperty("signingPassword") as String?
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["mavenJava"])
    }
}

coverallsJacoco {
    reportPath = "${buildDir.name}/reports/jacoco/report.xml"
    reportSourceSets = subprojects.stream().map { it.sourceSets }.map { it.main.get() }.toList()
}

fun gitVersion(default: String = "0.0.0"): String {
    val versionRegex = Regex("""v(\d+\.\d+\.\d+)(-\d+-\w+)?""")
    val tagName = System.getenv("TAG_NAME") ?: tagNameFromGit()
    val match = versionRegex.matchEntire(tagName)
    return match?.groupValues?.get(1) ?: default
}

fun tagNameFromGit(): String {
    ByteArrayOutputStream()
        .use { stream ->
            exec {
                commandLine("git", "describe", "--tags")
                standardOutput = stream
            }
            return stream.toString(Charsets.UTF_8.name()).trim()
        }
}