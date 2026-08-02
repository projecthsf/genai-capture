import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask.FailureLevel

plugins {
    id("java")
    // Kotlin is used only for the MCP toolset (io.genai.screenshot.mcpide). Version must
    // match the target IDE's Kotlin metadata — IntelliJ 2026.1 (261) ships Kotlin 2.3.
    id("org.jetbrains.kotlin.jvm") version "2.3.0"
    // IntelliJ Platform Gradle Plugin. Docs:
    // https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
    // 2.18.0 (not 2.1.0): the pluginVerification DSL used by the publish gate is
    // incompatible with Gradle 9.3 on 2.1.0. Matches the php-portable/jenkinsfile setup.
    id("org.jetbrains.intellij.platform") version "2.18.0"
}

group = "io.genai.screenshot"
version = "1.0.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Compile against a 2025.2+ IDE because the MCP toolset uses com.intellij.mcpServer
// (bundled since 2025.2). Dev uses the locally-installed IntelliJ IDEA (no multi-GB
// download); otherwise download IC 2025.2. The plugin still targets sinceBuild 233 —
// the MCP dependency is OPTIONAL (see plugin.xml), so on older IDEs the plugin loads
// with its capture/menu features and simply omits the MCP tools.
val ideaApp = file("/Applications/IntelliJ IDEA.app/Contents")
val useLocalIde = ideaApp.exists() && !providers.environmentVariable("CI").isPresent

dependencies {
    intellijPlatform {
        if (useLocalIde) {
            local(ideaApp.absolutePath)
        } else {
            intellijIdeaCommunity("2025.2")
        }
        // MCP server API (bundled 2025.2+). On the compile classpath here; made
        // runtime-OPTIONAL via <depends optional=…> in plugin.xml.
        bundledPlugin("com.intellij.mcpServer")
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

// Reuse the app's resources (themes, icons, messages) BUT ship the plugin's own
// application.properties (app.name=Desktop Capture) so the plugin is branded
// "Desktop Capture" inside the IDE while the standalone app stays "GenAI Capture".
val reusedResources = layout.buildDirectory.dir("reused-resources")
val copyAppResources by tasks.registering(Copy::class) {
    from("../src/main/resources") {
        exclude("io/genai/screenshot/application.properties")  // plugin provides its own
    }
    into(reusedResources)
}

sourceSets {
    main {
        java.srcDir(reusedSrc)              // reused app code (+ plugin's own src/main/java)
        resources.srcDir(reusedResources)  // reused resources minus application.properties
        // (the plugin's own src/main/resources — META-INF + the branded
        //  application.properties — is already a resource root)
    }
}

tasks.named("compileJava") { dependsOn(copyAppSources) }
tasks.named("processResources") { dependsOn(copyAppResources) }
// The Kotlin MCP toolset references the reused Java engine (CaptureTools), so the
// copied sources must exist before Kotlin joint-compiles against them.
tasks.named("compileKotlin") { dependsOn(copyAppSources) }

// Skip the headless-IDE settings indexer: it's optional (settings are still searchable
// at runtime) and flaky against a local() IDE. Keeps buildPlugin fast and reliable.
tasks.named("buildSearchableOptions") { enabled = false }

// Compile WITH JDK 21 (the 2025.2+ platform jars we depend on — for the MCP API —
// are Java 21 bytecode, which javac 17 can't read), but EMIT Java 17 bytecode via
// --release 17, so the plugin still loads on JBR-17 IDEs (sinceBuild 233).
java {
    toolchain { languageVersion = JavaLanguageVersion.of(21) }
}
tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

kotlin {
    jvmToolchain(21)   // compile with JDK 21 (to read the platform)…
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)   // …but emit Java 17, to match compileJava
    }
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
            // Verify against the newest released IDEA (has the MCP server) — confirms the
            // 1.1.0 MCP additions are structurally valid and forward-compatible. The core
            // capture/annotate code is unchanged from the already-verified 1.0.1, so its
            // 2023.3+ compatibility is unaffected; the MCP tools are optional-dependency
            // gated (mcp.xml), so pre-2025.2 IDEs load the plugin without them.
            latest {
                types.set(listOf(IntelliJPlatformType.IntellijIdea))
            }
        }
    }
}
