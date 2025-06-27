import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.1.10"
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
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.6-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0")
    implementation("dev.jorel:commandapi-bukkit-shade:10.1.0")
    implementation("org.bstats:bstats-bukkit:3.1.0")
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<ShadowJar> {
    relocate("dev.jorel.commandapi", "fi.sulku.mc.parrots.lib.commandapi")
    relocate("org.bstats", "fi.sulku.mc.parrots.bstats")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}