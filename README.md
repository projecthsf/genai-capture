# GenAI Capture

[![Downloads](https://img.shields.io/github/downloads/projecthsf/genai-capture/total?label=downloads)](https://github.com/projecthsf/genai-capture/releases)
[![Latest release](https://img.shields.io/github/v/release/projecthsf/genai-capture?label=latest)](https://github.com/projecthsf/genai-capture/releases/latest)

Screen capture + annotation, two ways:

- **Desktop app** — capture the whole desktop, annotate (arrows, shapes, text,
  highlighter, mosaic redaction, watermark), then copy / save / pin. Global
  hotkeys, gallery, themes. Cross-platform (Java).
- **JetBrains IDE plugin** (this repo's source) — the same engine inside any
  JetBrains IDE: GoLand, PhpStorm, PyCharm, WebStorm, IntelliJ, CLion, Rider, …

## Download the desktop app

Grab the installer for your OS from **[Releases](https://github.com/projecthsf/genai-capture/releases)**:

| OS | File |
|---|---|
| macOS | `GenAI Capture-<version>.dmg` |
| Windows (installer) | `GenAI Capture-<version>.exe` |
| Windows (portable) | `GenAI-Capture-portable-win.zip` |

> **macOS note:** the app isn't notarized yet. If macOS blocks the first launch,
> allow it under **System Settings ▸ Privacy & Security ▸ "Open Anyway"**, and grant
> **Screen Recording** permission when asked. Details ship in the DMG's
> *First Launch.txt*.

The plugin is on the [JetBrains Marketplace](https://plugins.jetbrains.com/) —
search for **GenAI Capture** (or install the zip from Releases by hand).

## Use with AI agents (MCP)

The desktop app (v1.2.0+) doubles as an **MCP server**: AI agents that speak the
[Model Context Protocol](https://modelcontextprotocol.io) — Claude Code, Claude
Desktop, Cursor, … — can capture your screen, **look at the image**, annotate it
(arrows, boxes, text, highlight, blur/redact), and save the result. Visual bug
reports and before/after proof, driven by one sentence to your AI.

```bash
# Claude Code (macOS path shown; use the install dir's binary on Windows)
claude mcp add genai-capture -- \
  "/Applications/GenAI Capture.app/Contents/MacOS/GenAI Capture" --mcp
```

Tools: `capture_fullscreen`, `capture_region`, `annotate`, `get_image`,
`save_image`. Captures play an audible cue; everything runs locally — images
only go to the AI client you connected.

---

# JetBrains IDE plugin

Brings the screenshot capture + annotation engine inside any JetBrains IDE
(GoLand, PhpStorm, PyCharm, WebStorm, IntelliJ, CLion, Rider, …).

## Features

- **Tools ▸ Take Screenshot** — `⌘⇧1` (macOS) / `Ctrl+Shift+1` (Win/Linux).
  Captures the whole desktop (IDE included) and opens the annotation overlay.
- **Tools ▸ Capture Desktop (Hide IDE)** — `⌘⇧2` / `Ctrl+Shift+2`.
  Temporarily hides the IDE, captures the desktop behind it, and keeps the IDE
  hidden until you dismiss the overlay (ESC / save / copy / pin / close).
- **Tools ▸ Take Delayed Screenshot ▸ 3s / 5s / 10s** — shows an on-screen
  countdown, then captures (time to switch windows or open a menu first).
- **Status-bar buttons** — a camera icon (immediate capture) and a monitor icon
  (hide-IDE desktop capture). Toggle either via the status-bar right-click menu.
- **Annotation overlay** — arrows, shapes, text, highlighter, mosaic redaction,
  watermark, with copy / save / pin.
- **Settings ▸ Tools ▸ GenAI Capture** — a **Watermark** page (appearance, default
  text, behaviour, live preview) and a **Toolbar** page (choose/reorder the tools
  on the capture toolbar). All shortcuts are rebindable in *Settings ▸ Keymap*.

On macOS, grant the IDE **Screen Recording** permission (System Settings ▸ Privacy
& Security ▸ Screen Recording) or the capture comes back black — same requirement
as any screen-capture tool.

## How it's built

It **reuses the desktop app's code** (`../src/main/java` + `../src/main/resources`)
via a Gradle `Copy` step that filters out the app shell and the parts that don't
load inside the IDE's JBR (the `osystem/` FFM code, jnativehook global-hotkey, tray,
and the desktop-themed Settings panels). Thin FFM-free shims under
`src/main/java/io/genai/screenshot/` (`osystem/AbstractOs`, `CaptureController`,
`ScreenshotApp`) satisfy the few OS calls the overlay makes. The capture itself uses
`java.awt.Robot` (no native code). The plugin's own actions, settings pages, and
status-bar widgets live under `src/main/java/io/genai/screenshot/plugin/`.

Built against the **platform module only** (`com.intellij.modules.platform`), so a
single build runs in every JetBrains IDE.

## Prerequisites

- **JDK 17+** (a JBR or any JDK 17+).
- A JetBrains IDE installed (the build targets your local **GoLand** by default —
  see `build.gradle.kts`). To use a different IDE, edit the `local("…")` line, or
  switch to a downloaded one, e.g. `intellijIdeaCommunity("2024.1")`.
- Gradle: easiest is to **open this `jetbrains-plugin/` folder in IntelliJ IDEA**
  (it imports the Gradle project and provides the wrapper). Or `brew install gradle`
  and run `gradle wrapper` once.

## Run it (sandbox IDE)

```bash
cd jetbrains-plugin
./gradlew runIde          # launches GoLand with the plugin loaded
```

Then use **Tools ▸ Take Screenshot** (`⌘⇧1`) or the status-bar camera icon.

## Build an installable plugin

```bash
cd jetbrains-plugin
./gradlew buildPlugin      # -> build/distributions/genai-capture-plugin-1.0.0.zip
```

Install in any IDE: **Settings ▸ Plugins ▸ ⚙ ▸ Install Plugin from Disk…** → pick the zip.

## Notes / next steps

- The reused overlay code is verified to compile at Java 17 with no FFM /
  jnativehook dependency.
- First `runIde` may need minor tweaks to the IntelliJ Platform Gradle DSL or the
  `local(...)` IDE path for your exact versions — iterate from there.
- Possible future options: capture a chosen region only, or a configurable
  default delay.
