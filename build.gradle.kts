import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.8.0"
    id("org.jetbrains.compose") version "1.5.0"
    id("com.github.johnrengelman.shadow") version "7.1.0" // Add Shadow plugin
    kotlin("plugin.serialization") version "1.8.10" // Update to the latest Kotlin version
}


group = "com.company"
version = "1.0"


tasks.shadowJar {
    archiveBaseName.set("Project_160")
    archiveVersion.set("1.0")
    archiveClassifier.set("")
}
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
    implementation("org.xerial:sqlite-jdbc:3.34.0") // SQLite JDBC driver
    implementation("io.ktor:ktor-client-core:2.3.2")
    implementation("io.ktor:ktor-client-cio:2.3.2")
    implementation("io.ktor:ktor-client-json:2.3.2")
    implementation("io.ktor:ktor-client-logging:2.3.2")
    implementation("io.ktor:ktor-client-serialization:2.3.2")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("org.slf4j:slf4j-simple:1.7.32") // Replace version with the latest compatible one.
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
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
//    toolchain {
//        languageVersion.set(JavaLanguageVersion.of(17))
//        implementation.set(JvmImplementation.J9) // optional for ARM compatibility
//    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17)) // Align Kotlin JVM target with Java toolchain
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