import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.8.0"
    id("org.jetbrains.compose") version "1.5.0"
}


group = "com.company"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Project_160"
            packageVersion = "1.0.0"
        }
    }
}

// Set both Kotlin and Java to use the same JVM toolchain
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8)) // Use Java 17 (or Java 11 if preferred)
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(8)) // Align Kotlin JVM target with Java toolchain
    }
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "MainKt"  // Make sure the main class matches your entry point
        )
    }
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE // Handle duplicate resources in dependencies

}
