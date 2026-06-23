package io.genai.screenshot.plugin;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.Nullable;

/**
 * "Tools ▸ Take Delayed Screenshot" — a submenu offering a few fixed delays
 * (3s / 5s / 10s) before the screen is captured.
 */
public class DelayedScreenshotGroup extends ActionGroup {

    private static final AnAction[] DELAYS = {
            new DelayedScreenshotAction(3),
            new DelayedScreenshotAction(5),
            new DelayedScreenshotAction(10),
    };

    public DelayedScreenshotGroup() {
        getTemplatePresentation().setIcon(PluginIcons.CAMERA);
    }

    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent e) {
        return DELAYS;
    }
}
