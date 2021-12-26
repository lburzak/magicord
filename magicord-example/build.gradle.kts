plugins {
    id("com.google.devtools.ksp")
    kotlin("jvm")
}

group = "com.github.ligmalabs.magicord"
version = "1.0"

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":magicord-processor"))
    ksp(project(":magicord-processor"))
}

sourceSets.main {
    java.srcDirs("src/main/kotlin")
}

ksp {
    arg("enabled", "true")
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}
