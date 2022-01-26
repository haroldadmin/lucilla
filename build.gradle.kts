import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    kotlin("jvm") version "1.6.10"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.2"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.6.10"
}

repositories {
    mavenCentral()
}

ext {
    this.set("groupId", "com.github.haroldadmin.lucilla")
    this.set("projectName", "lucilla")
    this.set("projectVersion", "0.0.1")
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<KtlintExtension> {
        version.set("0.43.0")
        ignoreFailures.set(false)
        disabledRules.add("no-wildcard-imports")
    }
}