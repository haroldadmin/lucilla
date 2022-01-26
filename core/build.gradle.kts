plugins {
    kotlin("jvm")
    `maven-publish`
    id("org.jlleitschuh.gradle.ktlint")
}

val artifactID = "core"
val groupID by extra("groupId")
val projectName by extra("projectName")
val projectVersion by extra("projectVersion")

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
            groupId = groupID
            artifactId = artifactID
            version = projectVersion

            from(components["java"])
        }
    }
}


dependencies {
    implementation(libs.kotlinReflect)
    implementation(libs.apacheCommonsCollection)
    implementation(libs.apacheOpenNlp)

    testImplementation(libs.kotestRunner)
    testImplementation(libs.kotestAssertions)
}
