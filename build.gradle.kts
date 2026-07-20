plugins {
    id("java")
    // IntelliJ Platform Gradle Plugin (2.x). Docs:
    // https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
    id("org.jetbrains.intellij.platform") version "2.1.0"
}

group = "io.genai.screenshot"
version = "1.0.1"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // Build & run against the locally-installed GoLand — no multi-GB download,
        // and a plugin built here (depending only on the platform) loads in every
        // JetBrains IDE (PhpStorm, PyCharm, WebStorm, IntelliJ, …).
        //
        // To use a different IDE, point this at its install path, or download one:
        //   intellijIdeaCommunity("2024.1")   // or  goland("2024.1"), phpstorm("2024.1")
        local("/Applications/GoLand.app/Contents")
        instrumentationTools()
    }
}

// ---- reuse the desktop app's capture + annotation engine -------------------
// Copy the needed sources into build/reused-src, filtering out the app shell and
// the FFM / jnativehook / tray bits that won't load inside the IDE's JBR. The few
// OS calls the overlay makes (isSystemDark / applyWindowChrome / userDataDir) are
// served by the FFM-free shim in src/main/java/io/genai/screenshot/osystem/.
val reusedSrc = layout.buildDirectory.dir("reused-src")
val copyAppSources by tasks.registering(Copy::class) {
    from("../src/main/java") {
        exclude("io/genai/screenshot/osystem/**")             // FFM (java.lang.foreign) + jnativehook
        exclude("io/genai/screenshot/Hotkeys.java")           // jnativehook
        exclude("io/genai/screenshot/GlobalHotkey.java")      // jnativehook
        exclude("io/genai/screenshot/ScreenshotApp.java")     // app shell (tray/menu/main) — shimmed
        exclude("io/genai/screenshot/CaptureController.java")  // app shell — shimmed
        exclude("io/genai/screenshot/FloatingButton.java")    // app shell
        exclude("io/genai/screenshot/GalleryWindow.java")     // app shell
        exclude("io/genai/screenshot/ToolbarConfigPanel.java")     // desktop-themed; plugin uses native ToolbarConfigurable
        exclude("io/genai/screenshot/WatermarkSettingsPanel.java") // desktop-themed; plugin uses native WatermarkConfigurable
        exclude("io/genai/screenshot/ScreenshotIconExporterApp.java") // build-time tool
    }
    into(reusedSrc)
}

sourceSets {
    main {
        java.srcDir(reusedSrc)                    // reused app code (+ plugin's own src/main/java)
        resources.srcDir("../src/main/resources") // themes, icons, messages, application.properties
    }
}

tasks.named("compileJava") { dependsOn(copyAppSources) }

// Skip the headless-IDE settings indexer: it's optional (settings are still searchable
// at runtime) and flaky against a local() IDE. Keeps buildPlugin fast and reliable.
tasks.named("buildSearchableOptions") { enabled = false }

java {
    // GoLand 2024.1 ships JBR 17; compile to 17 for compatibility across IDEs.
    toolchain { languageVersion = JavaLanguageVersion.of(17) }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "233"          // 2023.3+ (broad compatibility)
            untilBuild = provider { null }  // no upper bound → loads in current & future IDEs
        }
    }
}
