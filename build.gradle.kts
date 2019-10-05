import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
        maven(url = "https://dl.bintray.com/kotlin/dokka")
    }

    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.3.50"))
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:0.9.18")
    }
}

allprojects {
    group = "net.torommo.logspy"
    version = "0.8.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
