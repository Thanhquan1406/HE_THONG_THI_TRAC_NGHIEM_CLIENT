package com.thitracnghiem.client.ui.components;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class GradientPanel extends JPanel {
    private Color from = new Color(0xE8F0FF);
    private Color to = new Color(0xDDF7F1);
    private int arc = 18;

    public GradientPanel() {
        setOpaque(false);
    }

    public void setColors(Color from, Color to) {
        if (from != null) this.from = from;
        if (to != null) this.to = to;
        repaint();
    }

    public void setArc(int arc) {
        this.arc = Math.max(0, arc);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(0, 0, from, getWidth(), getHeight(), to);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }
}
