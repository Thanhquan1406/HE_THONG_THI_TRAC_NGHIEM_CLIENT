package com.thitracnghiem.client.ui;

import com.thitracnghiem.client.ui.components.GradientPanel;
import com.thitracnghiem.client.ui.components.EyeToggleButton;
import com.thitracnghiem.client.ui.components.PromptPasswordField;
import com.thitracnghiem.client.ui.components.PromptTextField;
import com.thitracnghiem.client.ui.components.RoundedButton;
import com.thitracnghiem.client.ui.components.RoundedPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Objects;

public class LoginPanel extends JPanel {
    public interface Listener {
        void onLogin(String mssv, String password);
    }

    private final PromptTextField mssvField = new PromptTextField();
    private final PromptPasswordField passwordField = new PromptPasswordField();
    private final RoundedButton loginButton = new RoundedButton("Bắt đầu Ca thi");
    private final JLabel errorLabel = new JLabel(" ", SwingConstants.LEFT);
    private final JLabel hintLabel = new JLabel(" ");
    private final EyeToggleButton eyeToggle = new EyeToggleButton();
    private final char defaultEchoChar;

    public LoginPanel(Listener listener) {
        Objects.requireNonNull(listener, "listener");
        setLayout(new BorderLayout());
        setBackground(new Color(0xF6F7F8));
        setOpaque(true);

        defaultEchoChar = passwordField.getEchoChar();

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        setupFieldDefaults();
        setupActions(listener);

        SwingUtilities.invokeLater(() -> mssvField.requestFocusInWindow());
    }

    private void setupFieldDefaults() {
        mssvField.setPrompt("Mã số sinh viên");
        passwordField.setPrompt("Mật khẩu");
        mssvField.setPromptColor(new Color(0x334155));
        passwordField.setPromptColor(new Color(0x334155));
        mssvField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        // Theo spec: input bg #F8FAFC, radius 8 (approx) + padding (18 top/bottom), left 48 để chừa icon
        mssvField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0)),
                BorderFactory.createEmptyBorder(18, 48, 18, 16)
        ));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0)),
                BorderFactory.createEmptyBorder(18, 48, 18, 48)
        ));
        mssvField.setBackground(new Color(0xF8FAFC));
        passwordField.setBackground(new Color(0xF8FAFC));
        mssvField.setForeground(new Color(0x0F172A));
        passwordField.setForeground(new Color(0x0F172A));

        hintLabel.setForeground(new Color(0x475569));
        hintLabel.setFont(hintLabel.getFont().deriveFont(Font.PLAIN, 12f));
        hintLabel.setText("<html>Bằng việc đăng nhập, bạn xác nhận rằng phiên làm việc<br/>của mình được giám sát theo <b>Giao thức Bảo mật LAN của Trường.</b></html>");

        errorLabel.setForeground(new Color(0xD1, 0x43, 0x43));
        errorLabel.setFont(errorLabel.getFont().deriveFont(Font.PLAIN, 12f));

        loginButton.setPreferredSize(new Dimension(398, 56));
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        loginButton.setFont(loginButton.getFont().deriveFont(Font.BOLD, 16f));
        loginButton.setArc(8);
        loginButton.setColors(new Color(0x1C74E9), Color.WHITE);

        eyeToggle.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        eyeToggle.setPreferredSize(new Dimension(28, 28));
        eyeToggle.addActionListener(e -> {
            boolean hidden = eyeToggle.isSelected(); // selected => hidden (slash)
            passwordField.setEchoChar(hidden ? defaultEchoChar : (char) 0);
            passwordField.requestFocusInWindow();
        });
    }

    private void setupActions(Listener listener) {
        loginButton.addActionListener(e -> {
            clearError();
            String mssv = mssvField.getText() == null ? "" : mssvField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            if (mssv.isBlank() || password.isBlank()) {
                showError("Vui lòng nhập MSSV và mật khẩu.");
                return;
            }
            setBusy(true);
            listener.onLogin(mssv, password);
        });

        passwordField.addActionListener(e -> loginButton.doClick());
        mssvField.addActionListener(e -> passwordField.requestFocusInWindow());
    }

    public void setBusy(boolean busy) {
        mssvField.setEnabled(!busy);
        passwordField.setEnabled(!busy);
        loginButton.setEnabled(!busy);
        eyeToggle.setEnabled(!busy);
        if (busy) errorLabel.setText(" ");
    }

    public void showError(String msg) {
        errorLabel.setText(msg == null || msg.isBlank() ? "Đăng nhập thất bại." : msg);
    }

    public void clearError() {
        errorLabel.setText(" ");
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(true);
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE5E7EB)),
                BorderFactory.createEmptyBorder(16, 24, 16, 24)
        ));

        // icon vuông 40x40 như spec
        RoundedPanel logoBox = new RoundedPanel(8);
        logoBox.setFill(new Color(0x1C74E9));
        logoBox.setPreferredSize(new Dimension(40, 40));
        logoBox.setLayout(new BorderLayout());
        JLabel logoMark = new JLabel(" ");
        logoMark.setOpaque(false);
        logoBox.add(logoMark, BorderLayout.CENTER);

        JLabel brand = new JLabel("UniExam");
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 20f));
        brand.setForeground(new Color(0x0F172A));

        JLabel brandSub = new JLabel("PORTAL");
        brandSub.setFont(brandSub.getFont().deriveFont(Font.PLAIN, 12f));
        brandSub.setForeground(new Color(0x1C74E9));

        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        brandText.add(brand);
        brandText.add(brandSub);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        left.add(logoBox);
        left.add(brandText);

        RoundedPanel securePill = new RoundedPanel(999);
        securePill.setFill(new Color(0xDCFCE7));
        securePill.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 6));
        JLabel secureText = new JLabel("KẾT NỐI LAN BẢO MẬT");
        secureText.setForeground(new Color(0x15803D));
        secureText.setFont(secureText.getFont().deriveFont(Font.BOLD, 12f));
        securePill.add(secureText);

        header.add(left, BorderLayout.WEST);
        header.add(securePill, BorderLayout.EAST);
        return header;
    }

    private JPanel buildCenter() {
        JPanel bg = new JPanel(new GridBagLayout());
        bg.setOpaque(true);
        bg.setBackground(new Color(0xF6F7F8));
        // top/bottom 90 theo spec (giảm nếu khung nhỏ)
        bg.setBorder(new EmptyBorder(36, 48, 36, 48));

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        content.setMaximumSize(new Dimension(1024, 720));

        GridBagConstraints cc = new GridBagConstraints();
        cc.gridy = 0;
        cc.weighty = 1;
        cc.fill = GridBagConstraints.BOTH;

        cc.gridx = 0;
        cc.weightx = 0.48;
        cc.insets = new Insets(0, 0, 0, 16);
        content.add(buildLoginCard(), cc);

        cc.gridx = 1;
        cc.weightx = 0.52;
        cc.insets = new Insets(0, 16, 0, 0);
        content.add(buildRightSide(), cc);

        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        bg.add(content, c);
        return bg;
    }

    private JPanel buildLoginCard() {
        RoundedPanel card = new RoundedPanel(12);
        card.setFill(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0)),
                BorderFactory.createEmptyBorder(48, 48, 48, 48)
        ));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Đăng nhập Sinh viên");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 36f));
        title.setForeground(new Color(0x0F172A));
        title.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = new JLabel("<html><span style='color:#64748B'>Nhập thông tin đăng nhập của bạn để truy cập môi<br/>trường thi.</span></html>");
        sub.setFont(sub.getFont().deriveFont(Font.PLAIN, 16f));
        sub.setAlignmentX(LEFT_ALIGNMENT);

        top.add(title);
        top.add(Box.createVerticalStrut(8));
        top.add(sub);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(32, 0, 0, 0));
        form.setAlignmentX(LEFT_ALIGNMENT);

        JLabel accLabel = new JLabel("MSSV");
        accLabel.setForeground(new Color(0x334155));
        accLabel.setFont(accLabel.getFont().deriveFont(Font.BOLD, 14f));
        accLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel passLabel = new JLabel("Mật khẩu");
        passLabel.setForeground(new Color(0x334155));
        passLabel.setFont(passLabel.getFont().deriveFont(Font.BOLD, 14f));
        passLabel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel mssvRow = buildAccountOverlay();
        JPanel passRow = buildPasswordOverlay();
        mssvRow.setAlignmentX(LEFT_ALIGNMENT);
        passRow.setAlignmentX(LEFT_ALIGNMENT);

        RoundedPanel hintBox = new RoundedPanel(8);
        hintBox.setFill(new Color(28, 116, 233, (int) (255 * 0.05)));
        hintBox.setLayout(new BorderLayout());
        hintBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(28, 116, 233, (int) (255 * 0.10))),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        hintBox.add(hintLabel, BorderLayout.CENTER);
        hintBox.setAlignmentX(LEFT_ALIGNMENT);
        hintBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        hintLabel.setAlignmentX(LEFT_ALIGNMENT);
        errorLabel.setAlignmentX(LEFT_ALIGNMENT);

        form.add(accLabel);
        form.add(Box.createVerticalStrut(8));
        form.add(mssvRow);
        form.add(Box.createVerticalStrut(24));
        form.add(passLabel);
        form.add(Box.createVerticalStrut(8));
        form.add(passRow);
        form.add(Box.createVerticalStrut(24));
        form.add(hintBox);
        form.add(Box.createVerticalStrut(24));
        JPanel btnRow = new JPanel(new BorderLayout());
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        btnRow.add(loginButton, BorderLayout.CENTER);
        form.add(btnRow);
        form.add(Box.createVerticalStrut(8));
        form.add(errorLabel);

        card.add(top, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildAccountOverlay() {
        JPanel overlay = new JPanel(null);
        overlay.setOpaque(false);
        overlay.setAlignmentX(LEFT_ALIGNMENT);
        overlay.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        overlay.setPreferredSize(new Dimension(420, 56));

        overlay.add(mssvField);

        RoundedPanel icon = new RoundedPanel(6);
        icon.setOpaque(false);
        icon.setFill(new Color(0x94A3B8));
        icon.setPreferredSize(new Dimension(20, 20));
        overlay.add(icon);

        overlay.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int w = overlay.getWidth();
                int h = overlay.getHeight();
                mssvField.setBounds(0, 0, w, h);
                icon.setBounds(16, (h - 20) / 2, 20, 20);
            }
        });

        return overlay;
    }

    private JPanel buildPasswordOverlay() {
        JPanel overlay = new JPanel(null);
        overlay.setOpaque(false);
        overlay.setAlignmentX(LEFT_ALIGNMENT);
        overlay.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        overlay.setPreferredSize(new Dimension(420, 56));

        overlay.add(passwordField);
        overlay.add(eyeToggle);

        RoundedPanel leftIcon = new RoundedPanel(6);
        leftIcon.setFill(new Color(0x94A3B8));
        leftIcon.setPreferredSize(new Dimension(16, 21));
        overlay.add(leftIcon);

        overlay.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int w = overlay.getWidth();
                int h = overlay.getHeight();
                passwordField.setBounds(0, 0, w, h);

                int btn = 24;
                int x = w - btn - 16;
                int y = (h - btn) / 2;
                eyeToggle.setBounds(x, y, btn, btn);

                leftIcon.setBounds(16, (h - 21) / 2, 16, 21);
            }
        });

        return overlay;
    }

    private JPanel buildRightSide() {
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        GradientPanel banner = new GradientPanel();
        banner.setArc(18);
        banner.setLayout(new BorderLayout());
        banner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE6EAF0)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));
        banner.setPreferredSize(new Dimension(520, 170));

        JLabel badge = new JLabel("THỜI GIAN HIỆU LỰC");
        badge.setOpaque(true);
        badge.setBackground(new Color(255, 255, 255, 190));
        badge.setForeground(new Color(0x334155));
        badge.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        badge.setFont(badge.getFont().deriveFont(Font.BOLD, 11f));

        JLabel bannerTitle = new JLabel("Kỳ thi Cuối kỳ 2026");
        bannerTitle.setForeground(new Color(0x0F172A));
        bannerTitle.setFont(bannerTitle.getFont().deriveFont(Font.BOLD, 20f));

        JPanel bannerText = new JPanel();
        bannerText.setOpaque(false);
        bannerText.setLayout(new BoxLayout(bannerText, BoxLayout.Y_AXIS));
        bannerText.add(badge);
        bannerText.add(Box.createVerticalStrut(10));
        bannerText.add(bannerTitle);

        banner.add(bannerText, BorderLayout.SOUTH);

        RoundedPanel guide = new RoundedPanel(18);
        guide.setFill(Color.WHITE);
        guide.setLayout(new BorderLayout());
        guide.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE6EAF0)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));

        JLabel guideTitle = new JLabel("Hướng dẫn Hệ thống");
        guideTitle.setForeground(new Color(0x0F172A));
        guideTitle.setFont(guideTitle.getFont().deriveFont(Font.BOLD, 14.5f));

        JPanel items = new JPanel();
        items.setOpaque(false);
        items.setLayout(new BoxLayout(items, BoxLayout.Y_AXIS));
        items.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        items.add(buildGuideItem("01", "Yêu cầu kết nối ổn định",
                "Đảm bảo thiết bị của bạn được kết nối qua mạng LAN/VPN theo quy định."));
        items.add(Box.createVerticalStrut(10));
        items.add(buildGuideItem("02", "Giao thức Tự động Lưu",
                "Bài làm sẽ được tự động lưu định kỳ để tránh mất dữ liệu."));
        items.add(Box.createVerticalStrut(10));
        items.add(buildGuideItem("03", "Hệ thống Giám sát Hoạt động",
                "Cửa sổ làm bài có thể bị khóa nếu phát hiện thao tác bất thường."));

        guide.add(guideTitle, BorderLayout.NORTH);
        guide.add(items, BorderLayout.CENTER);

        right.add(banner);
        right.add(Box.createVerticalStrut(14));
        right.add(guide);
        right.add(Box.createVerticalGlue());
        return right;
    }

    private JPanel buildGuideItem(String no, String title, String desc) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);

        RoundedPanel bubble = new RoundedPanel(999);
        bubble.setFill(new Color(0xEEF2FF));
        bubble.setLayout(new BorderLayout());
        bubble.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        JLabel n = new JLabel(no);
        n.setForeground(new Color(0x1F6FEB));
        n.setFont(n.getFont().deriveFont(Font.BOLD, 12.5f));
        bubble.add(n, BorderLayout.CENTER);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setForeground(new Color(0x0F172A));
        t.setFont(t.getFont().deriveFont(Font.BOLD, 12.5f));
        JLabel d = new JLabel("<html><span style='color:#64748B'>" + escapeHtml(desc) + "</span></html>");
        d.setFont(d.getFont().deriveFont(Font.PLAIN, 12.2f));
        text.add(t);
        text.add(Box.createVerticalStrut(4));
        text.add(d);

        row.add(bubble, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        return row;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel();
        footer.setOpaque(true);
        footer.setBackground(new Color(0xF6F7F8));
        footer.setBorder(BorderFactory.createEmptyBorder(12, 24, 16, 24));

        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));

        JLabel copyright = new JLabel("© 2026 Hội đồng Khảo thí Đại học. Bảo lưu mọi quyền.", SwingConstants.CENTER);
        copyright.setAlignmentX(CENTER_ALIGNMENT);
        copyright.setForeground(new Color(0x64748B));
        copyright.setFont(copyright.getFont().deriveFont(Font.PLAIN, 14f));

        JLabel sec = new JLabel("BẢO MẬT BỞI HẠ TẦNG UNISHIELD™ & PHÒNG THỦ MẠNG LAN", SwingConstants.CENTER);
        sec.setAlignmentX(CENTER_ALIGNMENT);
        sec.setForeground(new Color(0x64748B));
        sec.setFont(sec.getFont().deriveFont(Font.BOLD, 10f));

        footer.add(copyright);
        footer.add(Box.createVerticalStrut(4));
        footer.add(sec);
        return footer;
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}

