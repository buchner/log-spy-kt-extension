plugins {
    antlr
    idea
    `java-library`
}

dependencies {
    implementation(Libraries.antlrRuntime)
    antlr(Libraries.antlr)
}

tasks.generateGrammarSource {
    arguments.add("-package")
    arguments.add("net.torommo.logspy")
    arguments.add("-lib")
    arguments.add("src/main/antlr/net/torommo/logspy")
}

// workaround for https://github.com/gradle/gradle/issues/25885
sourceSets.configureEach {
    val generateGrammarSource = tasks.named(getTaskName("generate", "GrammarSource"))
    java.srcDir(generateGrammarSource.map { files() })
}

tasks.test { configure<JacocoTaskExtension> { isEnabled = false } }