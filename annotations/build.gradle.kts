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
        create<MavenPublication>("lucilla-annotations") {
            version = "0.0.1"
            groupId = "com.github.haroldadmin.lucilla"
            artifactId = "annotations"

            from(components["java"])
        }
    }
}
