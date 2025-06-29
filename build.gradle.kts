import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.2.0"
    java
    id("com.gradleup.shadow") version "8.3.3"
}

group = "fi.sulku.mc.parrots"
version = "2.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") //spigot
    maven("https://repo.dmulloy2.net/repository/public/") //protocolLib
    maven("https://repo.codemc.io/repository/maven-snapshots/") // Packet Events
}

dependencies {
    implementation("dev.jorel:commandapi-bukkit-shade:10.1.0")
    implementation("org.bstats:bstats-bukkit:3.1.0")
    implementation("com.github.retrooper:packetevents-spigot:2.9.0-SNAPSHOT")

    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.2.0")
    compileOnly("org.spigotmc:spigot-api:1.21.6-R0.1-SNAPSHOT")
    compileOnly("org.xerial:sqlite-jdbc:3.45.3.0")         // SQLite
    compileOnly("org.jetbrains.exposed:exposed-core:1.0.0-beta-2")
    compileOnly("org.jetbrains.exposed:exposed-jdbc:1.0.0-beta-2")
    compileOnly("mysql:mysql-connector-java:8.0.33")        // MySQL
    compileOnly("com.zaxxer:HikariCP:6.3.0")
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<ShadowJar> {
    relocate("dev.jorel.commandapi", "fi.sulku.mc.parrots.lib.commandapi")
    relocate("org.bstats", "fi.sulku.mc.parrots.bstats")
    relocate("com.github.retrooper.packetevents", "fi.sulku.mc.parrots.lib.packetevents")
    minimize()
}