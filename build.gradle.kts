import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.github.patrick.gradle.remapper.RemapTask

plugins {
    kotlin("jvm") version "2.1.10"
    java
    id("com.gradleup.shadow") version "8.3.3"
    id("io.github.patrick.remapper") version "1.4.2"
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
    implementation("dev.jorel:commandapi-bukkit-shade:10.1.0") // todo pluginyml
    implementation("org.bstats:bstats-bukkit:3.1.0")
    //protocollib:
    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0")
}

kotlin {
    jvmToolchain(21)
}

val shadowJarTask = tasks.named<ShadowJar>("shadowJar")

tasks.named<RemapTask>("remap") {
    dependsOn(shadowJarTask)
    version.set("1.21.6")
    inputTask.set(shadowJarTask) // Tell remapper to remap the shaded JAR

    // Optional: clean output structure
    archiveName.set("${project.name}-${project.version}-remapped.jar")
    //archiveDirectory.set(File(buildDir, "remapped"))
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("shadow")
    relocate("dev.jorel.commandapi", "fi.sulku.mc.parrots.lib.commandapi")
}

tasks.build {
    dependsOn(tasks.remap)
}