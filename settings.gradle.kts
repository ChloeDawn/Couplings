pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://maven.fabricmc.net")
    maven("https://jitpack.io")
  }

  resolutionStrategy {
    eachPlugin {
      if ("net.nemerosa.versioning" == requested.id.id) {
        useModule("com.github.nemerosa:versioning:${requested.version}")
      }
    }
  }
}

rootProject.name = "Couplings"
