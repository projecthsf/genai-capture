package io.genai.screenshot.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * "Tools ▸ Take Screenshot" — capture the desktop now and open the GenAI Capture
 * annotation overlay.
 */
public class TakeScreenshotAction extends AnAction {

    public TakeScreenshotAction() {
        getTemplatePresentation().setIcon(PluginIcons.CAMERA);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        CaptureService.capture();
    }
}
