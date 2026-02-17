plugins {
    kotlin("jvm") version "2.3.0"
    id("org.jetbrains.compose") version "1.10.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.0"
}

group = "com.group7"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
    maven(url = "https://jitpack.io/")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("com.github.PortSim.SimPort:api:7a17606583")
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.addAll("-Xcontext-parameters")
    }
}