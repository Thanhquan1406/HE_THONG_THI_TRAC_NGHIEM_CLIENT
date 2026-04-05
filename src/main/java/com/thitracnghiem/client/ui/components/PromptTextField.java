package com.thitracnghiem.client.ui.components;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

public class PromptTextField extends JTextField {
    private String prompt = "";
    private Color promptColor = new Color(0x475569);

    public PromptTextField() {
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD7DCE3)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                repaintLater();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                repaintLater();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                repaintLater();
            }
        });
    }

    public void setPrompt(String prompt) {
        this.prompt = (prompt == null) ? "" : prompt;
        repaint();
    }

    public void setPromptColor(Color promptColor) {
        this.promptColor = (promptColor == null) ? new Color(0x475569) : promptColor;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!getText().isEmpty() || isFocusOwner() || prompt.isBlank()) return;

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(promptColor);
            Insets ins = getInsets();
            int x = ins.left;
            int y = (getHeight() - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
            g2.drawString(prompt, x, y);
        } finally {
            g2.dispose();
        }
    }

    private void repaintLater() {
        SwingUtilities.invokeLater(this::repaint);
    }
}
