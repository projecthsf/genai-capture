package io.genai.screenshot.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * A single "wait N seconds, then capture" item inside the
 * {@link DelayedScreenshotGroup} submenu — shows an on-screen countdown so you
 * have time to switch windows or open a menu before the screen is grabbed.
 */
public class DelayedScreenshotAction extends AnAction {

    private final int seconds;

    public DelayedScreenshotAction(int seconds) {
        super(seconds + "s");
        this.seconds = seconds;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        CountdownCapture.start(seconds);
    }
}
