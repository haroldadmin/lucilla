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
        create<MavenPublication>("lucilla-ir") {
            version = "0.0.1"
            groupId = "com.github.haroldadmin.lucilla"
            artifactId = "ir"

            from(components["java"])
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    implementation(project(":annotations"))
    implementation(project(":pipeline"))

    implementation(libs.kotlinReflect)

    testImplementation(libs.kotestRunner)
    testImplementation(libs.kotestAssertions)
}
