// Top-level build.gradle.kts for FenceWise
buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal() // ✅ this line fixes the missing plugin issue
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.5.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0")
        classpath("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.0.0")
        classpath("com.google.gms:google-services:4.4.2")
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
