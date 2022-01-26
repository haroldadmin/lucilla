plugins {
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint")
    `maven-publish`
}

val artifactID = "async"
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
        create<MavenPublication>("lucilla-async") {
            groupId = groupID
            artifactId = artifactID
            version = projectVersion

            from(components["java"])
        }
    }
}

dependencies {
    api(project(":core"))
    implementation(libs.kotlinCoroutines)

    testImplementation(libs.kotestRunner)
    testImplementation(libs.kotestAssertions)
}
