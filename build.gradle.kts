
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.3.72"))
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:0.10.0")
    }
}

plugins {
    jacoco
    `java-library`
    `maven-publish`
    signing
    id("tech.formatter-kt.formatter") version "0.4.11"
    id("org.sonarqube") version "2.8"
}

allprojects {
    group = "net.torommo.logspy"
    version = "0.8.0-SNAPSHOT"

    repositories {
        maven(url = "https://dl.bintray.com/kotlin/dokka")
        mavenCentral()
    }

    sonarqube {
        properties {
            property("sonar.projectKey", "buchner_log-spy-kt-extension")
            property("sonar.organization", "buchner")
            property("sonar.host.url", "https://sonarcloud.io")
            property("sonar.login", project.findProperty("sonarLogin")!!)
            property("sonar.coverage.jacoco.xmlReportPaths", "${project.rootProject.buildDir}/reports/jacoco/report.xml")
        }
    }
}

tasks.register<JacocoReport>("codeCoverageReport") {
    subprojects {
        val subproject = this
        subproject.plugins.withType<JacocoPlugin>().configureEach {
            subproject.tasks.matching { it.extensions.findByType<JacocoTaskExtension>()?.isEnabled ?: false }
                .configureEach {
                    val testTask = this
                    sourceSets(subproject.sourceSets.main.get())
                    executionData(testTask)
                }
            subproject.tasks.matching { it.extensions.findByType<JacocoTaskExtension>() != null }.forEach {
                rootProject.tasks["codeCoverageReport"].dependsOn(it)
            }
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
    apply(plugin = "org.sonarqube")
    apply(plugin = "jacoco")
    apply(plugin = "tech.formatter-kt.formatter")

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