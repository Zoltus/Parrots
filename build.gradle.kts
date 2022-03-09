group = "sh.zoltus"
version = "2.0"
description = "All in one essentials plugin for any type of server!"

plugins {
    java
    idea
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

repositories {
    //Built spigot
    mavenLocal()
    //Spigot & requires
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
    maven("https://repo.maven.apache.org/maven2/")
    mavenCentral()
}

dependencies {
    //Shade
    implementation("org.bstats:bstats-bukkit:3.0.0")
    //compileOnly("org.jetbrains:annotations:22.0.0")
    compileOnly("org.spigotmc:spigot:1.18.2-R0.1-SNAPSHOT")
    compileOnly("org.spigotmc:minecraft-server:1.18.2-R0.1-SNAPSHOT")
    //Annotations
    compileOnly("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok:1.18.22")
    compileOnly("org.spigotmc:plugin-annotations:1.2.3-SNAPSHOT")
    annotationProcessor("org.spigotmc:plugin-annotations:1.2.3-SNAPSHOT")
}

tasks.compileJava {
    options.encoding = "UTF-8"
}

tasks.jar {
    include("org.bstats:bstats-bukkit:3.0.0")
    from(configurations.compileClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

tasks.shadowJar {
    dependsOn("jar")
    archiveClassifier.set("")
    destinationDirectory.set(file("D:/Noa/Minecraft/Servers/@Test_Server/plugins/")) //"$rootDir/jars/"
    relocate("org.bstats", "sh.zoltus.apis.bstats")
    minimize()
}