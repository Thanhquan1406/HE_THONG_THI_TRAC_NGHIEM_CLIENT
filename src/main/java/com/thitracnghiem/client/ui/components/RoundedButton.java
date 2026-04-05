package com.thitracnghiem.client.ui.components;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class RoundedButton extends JButton {
    private int arc = 14;
    private Color bg = new Color(0x1F6FEB);
    private Color fg = Color.WHITE;
    private Color bgDisabled = new Color(0xA7B7D8);

    public RoundedButton(String text) {
        super(text);
        setOpaque(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setForeground(fg);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void setArc(int arc) {
        this.arc = Math.max(0, arc);
        repaint();
    }

    public void setColors(Color bg, Color fg) {
        if (bg != null) this.bg = bg;
        if (fg != null) this.fg = fg;
        setForeground(this.fg);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isEnabled() ? bg : bgDisabled);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }
}
