package com.thitracnghiem.client.ui.components;

import javax.swing.JToggleButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Nút icon "con mắt" để hiện/ẩn mật khẩu (không dùng text).
 */
public class EyeToggleButton extends JToggleButton {
    private Color iconColor = new Color(0x64748B);
    private Color iconHoverColor = new Color(0x334155);
    private boolean hovering = false;

    public EyeToggleButton() {
        setOpaque(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovering = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovering = false;
                repaint();
            }
        });
    }

    public void setIconColor(Color iconColor) {
        if (iconColor != null) this.iconColor = iconColor;
        repaint();
    }

    public void setIconHoverColor(Color iconHoverColor) {
        if (iconHoverColor != null) this.iconHoverColor = iconHoverColor;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int w = getWidth();
        int h = getHeight();
        int s = Math.min(w, h);
        int cx = w / 2;
        int cy = h / 2;

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hovering ? iconHoverColor : iconColor);

            int eyeW = (int) (s * 0.62);
            int eyeH = (int) (s * 0.38);
            int x = cx - eyeW / 2;
            int y = cy - eyeH / 2;

            // outline eye
            g2.drawOval(x, y, eyeW, eyeH);

            // pupil
            int pupil = (int) (s * 0.14);
            g2.fillOval(cx - pupil / 2, cy - pupil / 2, pupil, pupil);

            // when selected: draw a slash (ẩn)
            if (isSelected()) {
                g2.setColor(new Color(g2.getColor().getRGB(), true));
                g2.drawLine(x, y + eyeH, x + eyeW, y);
            }
        } finally {
            g2.dispose();
        }
    }
}

