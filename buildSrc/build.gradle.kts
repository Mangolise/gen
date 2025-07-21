plugins {
    id("java")
    id("maven-publish")
}

group = "net.mangolise"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.serble.net/snapshots/")
}

dependencies {
    implementation("net.mangolise:mango-game-sdk:latest")
    implementation("net.minestom:minestom:2025.07.03-1.21.5")
    implementation("dev.hollowcube:polar:1.14.5")
    implementation(gradleApi())
}
