import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    kotlin("jvm") version "1.6.10"
    `maven-publish`
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
}

val GroupID = "com.github.haroldadmin.lucilla"
val ArtifactID = "core"
val ProjectName = "lucilla"
val ProjectVersion = "0.0.1"

repositories {
    mavenCentral()
}

kotlin {
    explicitApi()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("lucilla-core") {
            groupId = GroupID
            artifactId = ArtifactID
            version = ProjectVersion

            from(components["java"])
        }
    }
}

configure<KtlintExtension> {
    version.set("0.43.0")
    ignoreFailures.set(false)
    disabledRules.add("no-wildcard-imports")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.10")
    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("org.apache.opennlp:opennlp-tools:1.9.4")

    val kotestVersion = "5.0.3"
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
}
