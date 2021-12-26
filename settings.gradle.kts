pluginManagement {
    val kotlinVersion: String by settings
    val kspVersion: String by settings
    repositories {
        gradlePluginPortal()
        google()
    }
    plugins {
        id("com.google.devtools.ksp") version kspVersion
        kotlin("jvm") version kotlinVersion
    }
}

rootProject.name = "magicord"

include(":magicord-processor")
include(":magicord-example")