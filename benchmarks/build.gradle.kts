plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.allopen")
    id("org.jetbrains.kotlinx.benchmark")
}

repositories {
    mavenCentral()
}

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

benchmark {
    targets {
        register("main")
    }
}

dependencies {
    implementation(project(":core"))

    implementation(libs.kotlinxBenchmark)
}