import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask.FailureLevel

plugins {
    id("java")
    // IntelliJ Platform Gradle Plugin. Docs:
    // https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
    // 2.18.0 (not 2.1.0): the pluginVerification DSL used by the publish gate is
    // incompatible with Gradle 9.3 on 2.1.0. Matches the php-portable/jenkinsfile setup.
    id("org.jetbrains.intellij.platform") version "2.18.0"
}

group = "io.genai.screenshot"
version = "1.0.1"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Dev builds use the locally-installed GoLand (no multi-GB download); CI (and any
// machine without it) downloads an IDE, so the build is reproducible anywhere. A
// plugin built against the bare platform loads in every JetBrains IDE.
val useLocalIde = file("/Applications/GoLand.app/Contents").exists() &&
        !providers.environmentVariable("CI").isPresent

dependencies {
    intellijPlatform {
        if (useLocalIde) {
            local("/Applications/GoLand.app/Contents")
        } else {
            intellijIdeaCommunity("2024.1")
        }
        // instrumentationTools() removed in plugin 2.x — added automatically now.
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
        exclude("io/genai/screenshot/UpdateChecker.java")     // desktop-app updater; plugin updates via Marketplace
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

    // `./gradlew publishPlugin` reads the JetBrains Marketplace token from the PUBLISH_TOKEN
    // env var (set as a GitHub Actions secret). No signing configured, so uploads are unsigned.
    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }

    // `./gradlew verifyPlugin` runs the JetBrains Plugin Verifier (same tool Marketplace uses).
    // This is a publish gate in CI (see .github/workflows/publish.yml).
    pluginVerification {
        failureLevel.set(listOf(
            FailureLevel.COMPATIBILITY_PROBLEMS,
            FailureLevel.INTERNAL_API_USAGES,
            FailureLevel.MISSING_DEPENDENCIES,
            FailureLevel.INVALID_PLUGIN,
        ))
        ides {
            // Verify against the newest released unified IDEA. One download, enough to catch
            // forward-compat problems.
            latest {
                types.set(listOf(IntelliJPlatformType.IntellijIdea))
            }
        }
    }
}
