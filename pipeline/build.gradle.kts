plugins {
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint")
}

repositories {
    mavenCentral()
}

kotlin {
    explicitApi()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    implementation(libs.apacheOpenNlp)

    testImplementation(libs.kotestRunner)
    testImplementation(libs.kotestAssertions)
}
