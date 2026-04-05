package com.thitracnghiem.client.ui.components;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class RoundedPanel extends JPanel {
    private int arc = 16;
    private Color fill = Color.WHITE;

    public RoundedPanel() {
        setOpaque(false);
    }

    public RoundedPanel(int arc) {
        this.arc = Math.max(0, arc);
        setOpaque(false);
    }

    public void setArc(int arc) {
        this.arc = Math.max(0, arc);
        repaint();
    }

    public void setFill(Color fill) {
        this.fill = (fill == null) ? Color.WHITE : fill;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }
}
