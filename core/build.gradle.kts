plugins {
    kotlin("jvm")
    `maven-publish`
}

repositories {
    mavenCentral()
}

kotlin {
    explicitApi()
}

publishing {
    publications {
        create<MavenPublication>("lucilla-core") {
            version = "0.0.1"
            groupId = "com.github.haroldadmin.lucilla"
            artifactId = "core"

            from(components["java"])
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    api(project(":annotations"))
    api(project(":ir"))
    api(project(":pipeline"))

    implementation(libs.kotlinReflect)
    implementation(libs.apacheCommonsCollection)
    implementation(libs.apacheOpenNlp)

    testImplementation(libs.kotestRunner)
    testImplementation(libs.kotestAssertions)
}
