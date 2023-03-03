plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("xyz.jpenilla.run-paper") version "2.0.0"
}

group = "com.hackclub.hccore"
version = "1.0.0"
description = "Main plugin for the Hack Club Minecraft server."

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("de.tr7zw:item-nbt-api:2.8.0")

    compileOnly("com.frengor:ultimateadvancementapi:2.2.2")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.0.0-SNAPSHOT")
    compileOnly("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT")
}

tasks {
    // Run shadowJar on build
    build {
        dependsOn(shadowJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
        // show deprecation warnings: paper deprecated lots of pure string functions for chat components
//      options.isDeprecation = true
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
        expand("name" to rootProject.name, "version" to version)
    }

    shadowJar {
        archiveBaseName.set("HCCore-Shadow")
        archiveClassifier.set("")
    }


    runServer {
        minecraftVersion("1.19.3")

    }
}

repositories {
    mavenCentral()

    // UltimateAdvancementAPI
    maven("https://nexus.frengor.com/repository/public/")

    // NBT-API
    maven("https://repo.codemc.org/repository/maven-public/")

    // ProtoLib
    maven("https://repo.dmulloy2.net/nexus/repository/public/")

    // Papermc
    maven("https://repo.papermc.io/repository/maven-public/")
}
