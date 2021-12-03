plugins {
  `java-library`
  id("com.github.johnrengelman.shadow") version "7.1.0"
  id("io.papermc.paperweight.userdev") version "1.3.0"
}

group = "com.hackclub.hccore"
version = "1.0.0"
description = "Main plugin for the Hack Club Minecraft server."

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

dependencies {
    implementation("com.github.Trigary:AdvancementCreator:v2.0")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("de.tr7zw:item-nbt-api:2.8.0")

    compileOnly("com.comphenix.protocol:ProtocolLib:4.5.1")
    paperDevBundle("1.18-R0.1-SNAPSHOT")
}

tasks {
  // Run reobfJar on build
  build {
    dependsOn(reobfJar)
  }

  compileJava {
    options.encoding = Charsets.UTF_8.name()
    options.release.set(17)
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
    archiveVersion.set("")
  }
}

repositories {
    //jcenter()
    mavenCentral()

    // AdvancementCreator
    maven("https://jitpack.io")

    // NBT-API
    maven("https://repo.codemc.org/repository/maven-public/")

    // ProtoLib
    maven("https://repo.dmulloy2.net/nexus/repository/public/")

    mavenLocal()
}
