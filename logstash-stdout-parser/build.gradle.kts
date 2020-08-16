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

tasks.withType<JavaCompile> { dependsOn(tasks.generateGrammarSource) }

tasks.test { configure<JacocoTaskExtension> { isEnabled = false } }