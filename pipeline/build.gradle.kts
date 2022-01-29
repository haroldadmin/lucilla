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
        create<MavenPublication>("lucilla-pipeline") {
            version = "0.0.1"
            groupId = "com.github.haroldadmin.lucilla"
            artifactId = "pipeline"

            from(components["java"])
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    api(libs.apacheOpenNlp)

    testImplementation(libs.kotestRunner)
    testImplementation(libs.kotestAssertions)
}
