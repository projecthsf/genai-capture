package io.genai.screenshot.plugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Status-bar monitor button → hide the IDE, capture the desktop behind it (the
 * IDE stays hidden until the overlay is dismissed). Enabled by default;
 * hideable via the status-bar right-click menu.
 */
public class CaptureDesktopStatusBarWidgetFactory implements StatusBarWidgetFactory {

    @NotNull
    @Override
    public String getId() {
        return CaptureStatusBarWidget.DESKTOP_ID;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "GenAI Capture: Desktop (hide IDE)";
    }

    @NotNull
    @Override
    public StatusBarWidget createWidget(@NotNull Project project) {
        return new CaptureStatusBarWidget(
                CaptureStatusBarWidget.DESKTOP_ID,
                PluginIcons.DESKTOP,
                "Capture Desktop — hide IDE (GenAI Capture)",
                CaptureService::captureHidingIde);
    }
}
