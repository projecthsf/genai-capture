package io.genai.screenshot.mcpide

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import io.genai.screenshot.mcp.CaptureTools

/**
 * Exposes Desktop Capture's engine through the IDE's built-in MCP server, so AI
 * agents can capture and annotate the screen without the desktop app installed.
 *
 * The IDE MCP tool API can only return TEXT (its result content is a sealed `Text`
 * type — a plugin cannot emit an image content block). So these tools save a PNG
 * and return its **path**; a client that reads local files (e.g. Claude Code's file
 * read) then views it. Registered only where com.intellij.mcpServer is present
 * (optional dependency), so the plugin still loads on IDEs older than 2025.2.
 */
class CaptureIdeToolset : McpToolset {

    @McpTool
    @McpDescription(
        "Capture the entire screen to a PNG file and return its absolute path. " +
        "This returns a file PATH, not the image inline — to view the screenshot, " +
        "READ the returned file path."
    )
    fun capture_screen_to_file(): String {
        val path = CaptureTools.captureFullscreenToTempFile()
        return "Screenshot saved to: $path\nRead this file path to view the screenshot."
    }

    @McpTool
    @McpDescription(
        "Annotate an existing image file and save the result as a new PNG (returns its " +
        "path — READ it to view). 'operations' is a JSON array of drawing ops in the " +
        "image's full-resolution pixel coordinates. Op types: arrow/line/rect/ellipse " +
        "(x,y,x2,y2); text (x,y,text, optional size); step (x,y,number = numbered badge); " +
        "highlight and blur (x,y,x2,y2 — blur pixelates the area to redact secrets). " +
        "Optional per op: color (hex like #FF3B30), stroke (px). Example: " +
        "[{\"type\":\"rect\",\"x\":10,\"y\":20,\"x2\":200,\"y2\":120,\"color\":\"#FF3B30\"}," +
        "{\"type\":\"blur\",\"x\":50,\"y\":60,\"x2\":180,\"y2\":90}]"
    )
    fun annotate_image(imagePath: String, operations: String): String {
        val path = CaptureTools.annotateFileToTempFile(imagePath, operations)
        return "Annotated image saved to: $path\nRead this file path to view it."
    }
}
