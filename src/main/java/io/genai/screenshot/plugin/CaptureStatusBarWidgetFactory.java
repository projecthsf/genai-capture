package io.genai.screenshot.plugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Status-bar camera button → immediate screenshot (IDE included).
 * Enabled by default; hideable via the status-bar right-click menu.
 */
public class CaptureStatusBarWidgetFactory implements StatusBarWidgetFactory {

    @NotNull
    @Override
    public String getId() {
        return CaptureStatusBarWidget.SCREENSHOT_ID;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "GenAI Capture: Screenshot";
    }

    @NotNull
    @Override
    public StatusBarWidget createWidget(@NotNull Project project) {
        return new CaptureStatusBarWidget(
                CaptureStatusBarWidget.SCREENSHOT_ID,
                PluginIcons.CAMERA,
                "Take Screenshot (GenAI Capture)",
                CaptureService::capture);
    }
}
