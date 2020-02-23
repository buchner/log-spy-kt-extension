
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.3.61"))
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:0.10.0")
    }
}

plugins {
    `java-library`
    `maven-publish`
    signing
}

allprojects {
    group = "net.torommo.logspy"
    version = "0.8.0-SNAPSHOT"

    repositories {
        maven(url = "https://dl.bintray.com/kotlin/dokka")
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "org.jetbrains.dokka")

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<GenerateModuleMetadata> {
        enabled = false
    }

    val dokka by tasks.getting(DokkaTask::class) {
        outputFormat = "html"
        outputDirectory = "$buildDir/javadoc"
    }

    val dokkaJar by tasks.creating(Jar::class) {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "Assembles Kotlin documentation with Dokka."
        archiveClassifier.set("javadoc")
        from(dokka)
    }
    artifacts.add("archives", dokkaJar)

    val sourcesJar by tasks.creating(Jar::class) {
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
                            name.set("GNU LESSER GENERAL PUBLIC LICENSE, Version 3.0")
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
                        connection.set("scm:git:https://github.com/buchner/log-spy-kt-extension.git")
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
                    username = project.property("mavenUser") as String? ?: System.getProperty("mavenUser")
                    password = project.property("mavenPassword") as String? ?: System.getProperty("mavenPassword")
                }
            }
        }
    }

    signing {
        val signingKey = project.property("signingKey") as String? ?: System.getProperty("signingKey")
        val signingPassword = project.property("signingPassword") as String? ?: System.getProperty("signingPassword")
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["mavenJava"])
    }
}