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
    implementation(project(":annotations"))
    implementation(project(":pipeline"))

    implementation(libs.kotlinReflect)

    testImplementation(libs.kotestRunner)
    testImplementation(libs.kotestAssertions)
}
