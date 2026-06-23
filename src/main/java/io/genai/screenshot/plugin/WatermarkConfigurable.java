package io.genai.screenshot.plugin;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.ColorPicker;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.ColorIcon;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import io.genai.screenshot.toolbar.annotation.Watermark;
import io.genai.screenshot.toolbar.control.DrawingStyle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * "Settings ▸ Tools ▸ GenAI Capture ▸ Watermark" — native IntelliJ rendering of
 * the watermark options (appearance, default text, behaviour), bound to the
 * shared {@link DrawingStyle} preferences. Every control saves immediately, so
 * there's nothing to Apply — values take effect on the next capture.
 */
public class WatermarkConfigurable implements Configurable {

    private JComponent root;
    private JBTextField text;
    private JComponent preview;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Watermark";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        if (root != null) return root;

        text = new JBTextField(DrawingStyle.defaultWatermarkText(), 18);

        // Live preview: a faux capture (neutral background) with the watermark tiled over it.
        preview = new JComponent() {
            @Override protected void paintComponent(Graphics g) {
                int w = getWidth(), h = getHeight();
                BufferedImage base = new BufferedImage(Math.max(1, w), Math.max(1, h), BufferedImage.TYPE_INT_RGB);
                Graphics2D bg = base.createGraphics();
                bg.setColor(new Color(0xF3, 0xF3, 0xF3));
                bg.fillRect(0, 0, w, h);
                bg.dispose();
                g.drawImage(base, 0, 0, null);
                String t = text.getText().trim();
                if (t.isEmpty()) t = "GENAI";
                Graphics2D g2 = (Graphics2D) g.create();
                Watermark.fromConfig(t).paint(g2, base);
                g2.dispose();
            }
        };
        preview.setPreferredSize(new Dimension(340, 120));
        preview.setBorder(IdeBorderFactory.createBorder());
        Runnable upd = preview::repaint;

        text.getDocument().addDocumentListener(new DocumentListener() {
            void save() { DrawingStyle.setDefaultWatermarkText(text.getText().trim()); upd.run(); }
            @Override public void insertUpdate(DocumentEvent e) { save(); }
            @Override public void removeUpdate(DocumentEvent e) { save(); }
            @Override public void changedUpdate(DocumentEvent e) { save(); }
        });

        ComboBox<Integer> opacity = combo(new Integer[]{10, 20, 35, 50, 75, 100},
                nearest(DrawingStyle.watermarkOpacity(), 10, 20, 35, 50, 75, 100),
                i -> i + "%", i -> { DrawingStyle.setWatermarkOpacity(i); upd.run(); });
        ComboBox<Integer> size = combo(new Integer[]{16, 20, 24, 28, 36, 48},
                nearest(DrawingStyle.watermarkSize(), 16, 20, 24, 28, 36, 48),
                i -> i + " px", i -> { DrawingStyle.setWatermarkSize(i); upd.run(); });
        ComboBox<Integer> rotation = combo(new Integer[]{-45, -30, -15, 0, 15, 30, 45},
                nearest(DrawingStyle.watermarkRotation(), -45, -30, -15, 0, 15, 30, 45),
                i -> i + "°", i -> { DrawingStyle.setWatermarkRotation(i); upd.run(); });
        ComboBox<String> spacing = combo(new String[]{"Tight", "Normal", "Loose"},
                spacingLabel(DrawingStyle.watermarkGap()), s -> s,
                s -> { DrawingStyle.setWatermarkGap(spacingGap(s)); upd.run(); });
        ComboBox<String> blur = combo(new String[]{"None", "Soft", "Strong"},
                blurLabel(DrawingStyle.watermarkBlur()), s -> s,
                s -> { DrawingStyle.setWatermarkBlur(blurVal(s)); upd.run(); });

        JComponent colorRow = colorRow(upd);

        JBCheckBox remember = new JBCheckBox("Remember text I type in the prompt as the new default");
        remember.setSelected(DrawingStyle.watermarkRemember());
        remember.addActionListener(e -> DrawingStyle.setWatermarkRemember(remember.isSelected()));

        JBCheckBox always = new JBCheckBox("Always apply to every screenshot");
        always.setSelected(DrawingStyle.watermarkAlways());
        always.addActionListener(e -> DrawingStyle.setWatermarkAlways(always.isSelected()));

        JComponent form = FormBuilder.createFormBuilder()
                .addLabeledComponent("Opacity:", left(opacity))
                .addLabeledComponent("Size:", left(size))
                .addLabeledComponent("Rotation:", left(rotation))
                .addLabeledComponent("Spacing:", left(spacing))
                .addLabeledComponent("Blur:", left(blur))
                .addLabeledComponent("Colour:", colorRow)
                .addLabeledComponent("Preview:", left(preview))
                .addLabeledComponent("Default text:", left(text))
                .addComponentToRightColumn(comment(
                        "Pre-fills the prompt shown when you click the Watermark tool. Clicking Watermark "
                                + "always asks — you can keep this text or type a new one (it replaces the "
                                + "current watermark)."))
                .addComponent(remember)
                .addComponent(always)
                .addComponentToRightColumn(comment(
                        "Every new capture is stamped with this watermark automatically. Click the Watermark "
                                + "tool to change it, or Undo to remove it from a capture."))
                .getPanel();

        root = new JPanel(new BorderLayout());
        root.setBorder(JBUI.Borders.empty(12));
        root.add(form, BorderLayout.NORTH);
        return root;
    }

    /** Row of preset colour swatches plus a "Custom…" picker. */
    private JComponent colorRow(Runnable upd) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        Color[] presets = { new Color(0x82, 0x82, 0x82), Color.BLACK, Color.WHITE,
                new Color(0xE5, 0x3B, 0x54), new Color(0x21, 0x96, 0xF3) };
        for (Color c : presets) {
            JButton sw = new JButton(new ColorIcon(18, c));
            sw.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            sw.setContentAreaFilled(false);
            sw.addActionListener(e -> { DrawingStyle.setWatermarkColor(c.getRGB() & 0xFFFFFF); upd.run(); });
            p.add(sw);
        }
        JButton custom = new JButton("Custom…");
        custom.addActionListener(e -> {
            // Native IntelliJ colour picker (no opacity; matches the IDE theme).
            Color picked = ColorPicker.showDialog(p, "Watermark Colour",
                    new Color(DrawingStyle.watermarkColor()), false, null, false);
            if (picked != null) { DrawingStyle.setWatermarkColor(picked.getRGB() & 0xFFFFFF); upd.run(); }
        });
        p.add(custom);
        return p;
    }

    /** Keep a control at its natural size instead of stretching across the page. */
    private static JComponent left(JComponent c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        p.add(c);
        return p;
    }

    /** A small, muted, wrapping caption — the IDE's standard "comment" style. */
    private static JComponent comment(String text) {
        JBLabel l = new JBLabel("<html>" + text + "</html>");
        l.setComponentStyle(UIUtil.ComponentStyle.SMALL);
        l.setForeground(UIUtil.getContextHelpForeground());
        l.setBorder(JBUI.Borders.emptyTop(2));
        return l;
    }

    private static <T> ComboBox<T> combo(T[] items, T selected, Function<T, String> label, Consumer<T> onChange) {
        ComboBox<T> c = new ComboBox<>(items);
        c.setSelectedItem(selected);
        c.setRenderer(SimpleListCellRenderer.create(
                (lbl, value, index) -> lbl.setText(value == null ? "" : label.apply(value))));
        c.addActionListener(e -> {
            Object v = c.getSelectedItem();
            if (v != null) {
                @SuppressWarnings("unchecked") T t = (T) v;
                onChange.accept(t);
            }
        });
        return c;
    }

    private static Integer nearest(int v, int... opts) {
        int best = opts[0];
        for (int o : opts) if (Math.abs(o - v) < Math.abs(best - v)) best = o;
        return best;
    }

    private static String spacingLabel(int gap) { return gap <= 45 ? "Tight" : gap >= 90 ? "Loose" : "Normal"; }
    private static int spacingGap(String s) { return "Tight".equals(s) ? 30 : "Loose".equals(s) ? 110 : 60; }
    private static String blurLabel(int b) { return b <= 0 ? "None" : b >= 5 ? "Strong" : "Soft"; }
    private static int blurVal(String s) { return "None".equals(s) ? 0 : "Strong".equals(s) ? 6 : 3; }

    @Override public boolean isModified() { return false; } // saves live
    @Override public void apply() { }
    @Override public void reset() { }

    @Override
    public void disposeUIResources() {
        root = null; text = null; preview = null;
    }
}
