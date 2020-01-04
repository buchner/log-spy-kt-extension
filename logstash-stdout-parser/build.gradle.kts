plugins {
    antlr
    idea
    `java-library`
}

dependencies {
    implementation("org.antlr:antlr4-runtime:4.7.2")
    antlr("org.antlr:antlr4:4.7.2")
}

tasks.generateGrammarSource {
    arguments.add("-package")
    arguments.add("net.torommo.logspy")
    arguments.add("-lib")
    arguments.add("src/main/antlr/net/torommo/logspy")
}

tasks.withType<JavaCompile> {
    dependsOn(tasks.generateGrammarSource)
}