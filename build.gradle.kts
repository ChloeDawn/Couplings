import org.gradle.util.GradleVersion
import java.time.Instant

plugins {
  id("fabric-loom") version "0.7.30"
  id("net.nemerosa.versioning") version "be24b23"
  id("signing")
}

group = "dev.sapphic"
version = "2.0.0+1.16"

java {
  withSourcesJar()
}

loom {
  refmapName = "mixins/couplings/refmap.json"
  runs {
    configureEach {
      vmArg("-Dmixin.debug.export=true")
      vmArg("-Dmixin.debug.export.decompile=false")
      vmArg("-Dmixin.debug.verbose=true")
      vmArg("-Dmixin.dumpTargetOnFailure=true")
      vmArg("-Dmixin.checks=true")
      vmArg("-Dmixin.hotSwap=true")
    }
  }
}

repositories {
  maven("https://maven.terraformersmc.com/releases") {
    content {
      includeGroup("com.terraformersmc")
    }
  }
}

dependencies {
  minecraft("com.mojang:minecraft:1.16.5")
  mappings(loom.officialMojangMappings())
  modImplementation("net.fabricmc:fabric-loader:0.11.6")
  implementation("org.jetbrains:annotations:21.0.1")
  implementation("org.checkerframework:checker-qual:3.14.0")
  implementation(include("com.electronwill.night-config:core:3.6.3")!!)
  implementation(include("com.electronwill.night-config:toml:3.6.3")!!)
  modRuntime("com.terraformersmc:modmenu:1.16.5")
}

tasks {
  compileJava {
    with(options) {
      release.set(8)
      isFork = true
      isDeprecation = true
      encoding = "UTF-8"
      compilerArgs.addAll(listOf("-Xlint:all", "-parameters"))
    }
  }

  processResources {
    filesMatching("/fabric.mod.json") {
      expand("version" to project.version)
    }
  }

  jar {
    from("/LICENSE.md")

    manifest.attributes(
      "Build-Timestamp" to Instant.now(),
      "Build-Revision" to versioning.info.commit,
      "Build-Jvm" to "${
        System.getProperty("java.version")
      } (${
        System.getProperty("java.vendor")
      } ${
        System.getProperty("java.vm.version")
      })",
      "Built-By" to GradleVersion.current(),

      "Implementation-Title" to project.name,
      "Implementation-Version" to project.version,
      "Implementation-Vendor" to project.group,

      "Specification-Title" to "FabricMod",
      "Specification-Version" to "1.0.0",
      "Specification-Vendor" to project.group,

      "Sealed" to "true"
    )
  }

  assemble {
    dependsOn(versionFile)
  }
}

if (hasProperty("signing.mods.keyalias")) {
  val alias = property("signing.mods.keyalias")
  val keystore = property("signing.mods.keystore")
  val password = property("signing.mods.password")

  listOf(tasks.remapJar, tasks.remapSourcesJar).forEach {
    it.get().doLast {
      if (!project.file(keystore!!).exists()) {
        error("Missing keystore $keystore")
      }

      val file = outputs.files.singleFile
      ant.invokeMethod("signjar", mapOf(
        "jar" to file,
        "alias" to alias,
        "storepass" to password,
        "keystore" to keystore,
        "verbose" to true,
        "preservelastmodified" to true
      ))
      signing.sign(file)
    }
  }
}
