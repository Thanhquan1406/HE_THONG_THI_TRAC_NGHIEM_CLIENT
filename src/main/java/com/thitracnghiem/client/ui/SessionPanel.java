package com.thitracnghiem.client.ui;

import com.thitracnghiem.client.api.dto.ExamSession;
import com.thitracnghiem.client.ui.components.RoundedButton;
import com.thitracnghiem.client.ui.components.RoundedPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SessionPanel extends JPanel {
    private static final ZoneId ZONE_VN = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter START_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
    private static final Color START_BTN_ACTIVE_BG = new Color(0x1C74E9);
    private static final Color START_BTN_ACTIVE_FG = Color.WHITE;
    private static final Color START_BTN_DISABLED_BG = new Color(0xE2E8F0);
    private static final Color START_BTN_DISABLED_FG = new Color(0x94A3B8);

    public interface Listener {
        void onStart(ExamSession session);

        void onLogout();
    }

    private final JComboBox<ExamSession> sessionCombo = new JComboBox<>();
    private final RoundedButton startButton = new RoundedButton("Bắt đầu Ca thi");
    private final JButton logoutButton = new JButton("Đăng xuất");

    // Header
    private final JLabel headerUserName = new JLabel("-", SwingConstants.RIGHT);
    private final JLabel headerUserCode = new JLabel("-", SwingConstants.RIGHT);

    // Welcome card
    private final JLabel welcomeTitle = new JLabel("Chào mừng, Sinh viên");
    private final JLabel welcomeId = new JLabel("ID: -");
    private final JLabel welcomeDept = new JLabel("Khoa: -");
    private final JLabel welcomeDate = new JLabel("-");
    private final JLabel statusPill = new JLabel("TRẠNG THÁI: ĐÃ ĐĂNG NHẬP");
// Đây là màn hình quy cế thi
    // Session info card
    private final JLabel sessionSubjectValue = new JLabel("-");
    private final JLabel sessionRoomValue = new JLabel("-");
    private final JLabel sessionStartValue = new JLabel("-");
    private final JLabel sessionDurationValue = new JLabel("-");
    private final JLabel sessionTotalValue = new JLabel("-");

    // Waiting card
    private final JLabel waitingTitle = new JLabel("Đang chờ Giám thị kích hoạt...");
    private final JLabel waitingDesc = new JLabel("<html>Ca thi sẽ tự động bắt đầu ngay khi giám thị cho phép. Vui<br/>lòng giữ nguyên màn hình này.</html>");

    // Select session area
    private final JLabel pickLabel = new JLabel("Chọn ca thi");

    private final JLabel errorLabel = new JLabel(" ");
    private boolean busy = false;

    public SessionPanel(Listener listener) {
        Objects.requireNonNull(listener, "listener");
        setLayout(new BorderLayout());
        setBackground(new Color(0xF6F7F8));

        add(buildTopHeader(listener), BorderLayout.NORTH);
        add(buildContent(listener), BorderLayout.CENTER);

        errorLabel.setForeground(new Color(0xD1, 0x43, 0x43));

        // Combo style
        sessionCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        sessionCombo.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel l = new JLabel(value == null ? "" : value.toDisplayText());
            l.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            if (isSelected) {
                l.setOpaque(true);
                l.setBackground(new Color(0x1C74E9));
                l.setForeground(java.awt.Color.WHITE);
            } else {
                l.setOpaque(true);
                l.setBackground(java.awt.Color.WHITE);
                l.setForeground(new Color(0x0F172A));
            }
            return l;
        });

        startButton.addActionListener(e -> {
            ExamSession picked = (ExamSession) sessionCombo.getSelectedItem();
            if (picked == null) return;
            setBusy(true);
            listener.onStart(picked);
        });
        logoutButton.addActionListener(e -> listener.onLogout());

        // Default button look
        startButton.setArc(10);
        startButton.setColors(START_BTN_ACTIVE_BG, START_BTN_ACTIVE_FG);
        startButton.setFont(startButton.getFont().deriveFont(Font.BOLD, 14.5f));
        startButton.setPreferredSize(new Dimension(220, 44));
    }

    public void setStudentHeader(String fullName, String mssv) {
        String name = (fullName == null || fullName.isBlank()) ? "Sinh viên" : fullName;
        String code = (mssv == null || mssv.isBlank()) ? "-" : mssv;
        headerUserName.setText(name);
        headerUserCode.setText(code);
        welcomeTitle.setText("Chào mừng, " + name);
        welcomeId.setText("ID: " + code);
    }

    public void setStudentInfo(String fullName, String mssv, Long studentId) {
        // HTML mẫu có khoa + ngày; hiện API không có -> giữ placeholder.
        welcomeDept.setText("Khoa Khoa học Máy tính");
        welcomeDate.setText("24 thg 10, 2026");
    }

    public void showError(String msg) {
        errorLabel.setText(msg == null || msg.isBlank() ? "Có lỗi, vui lòng thử lại." : msg);
    }

    public void clearError() {
        errorLabel.setText(" ");
    }

    public void setSessions(List<ExamSession> sessions) {
        sessionCombo.removeAllItems();
        if (sessions != null) {
            for (ExamSession s : sessions) sessionCombo.addItem(s);
        }
        if (sessionCombo.getItemCount() > 0) sessionCombo.setSelectedIndex(0);

        // Update session info card theo session đang chọn
        updateSessionInfo((ExamSession) sessionCombo.getSelectedItem());
        refreshStartButtonState((ExamSession) sessionCombo.getSelectedItem());
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
        sessionCombo.setEnabled(!busy);
        logoutButton.setEnabled(!busy);
        refreshStartButtonState((ExamSession) sessionCombo.getSelectedItem());
    }

    private JPanel buildTopHeader(Listener listener) {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(true);
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE2E8F0)),
                BorderFactory.createEmptyBorder(12, 40, 12, 40)
        ));

        RoundedPanel logo = new RoundedPanel(8);
        logo.setFill(new Color(0x1C74E9));
        logo.setPreferredSize(new Dimension(32, 32));

        JLabel brand = new JLabel("Cổng thông tin thi");
        brand.setForeground(new Color(0x0F172A));
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 18f));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        left.setOpaque(false);
        left.add(logo);
        left.add(brand);

        headerUserName.setForeground(new Color(0x0F172A));
        headerUserName.setFont(headerUserName.getFont().deriveFont(Font.BOLD, 14f));
        headerUserCode.setForeground(new Color(0x64748B));
        headerUserCode.setFont(headerUserCode.getFont().deriveFont(Font.PLAIN, 12f));

        JPanel userStack = new JPanel();
        userStack.setOpaque(false);
        userStack.setLayout(new BoxLayout(userStack, BoxLayout.Y_AXIS));
        userStack.add(headerUserName);
        userStack.add(headerUserCode);

        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setContentAreaFilled(true);
        logoutButton.setBackground(new Color(0xF1F5F9));
        logoutButton.setForeground(new Color(0x0F172A));
        logoutButton.setPreferredSize(new Dimension(110, 40));
        logoutButton.addActionListener(e -> listener.onLogout());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setOpaque(false);
        right.add(userStack);
        right.add(logoutButton);

        top.add(left, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);
        return top;
    }

    private JPanel buildContent(Listener listener) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new GridBagLayout());
        container.setBorder(new EmptyBorder(0, 0, 0, 0));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 12, 0);

        JPanel welcome = buildWelcomeCard();
        container.add(welcome, c);

        c.gridy++;
        JPanel columns = buildColumnsGrid();
        container.add(columns, c);

        c.gridy++;
        JPanel pick = buildPickSessionCard();
        c.insets = new Insets(0, 0, 4, 0);
        container.add(pick, c);

        c.gridy++;
        c.insets = new Insets(0, 0, 0, 0);
        errorLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        container.add(errorLabel, c);

        // Scroll (nếu màn hình nhỏ)
        JScrollPane scroll = new JScrollPane(container);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);

        wrap.add(scroll, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildColumnsGrid() {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 0, 0, 0);

        // Left column
        JPanel leftCol = new JPanel();
        leftCol.setOpaque(false);
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        JPanel rules = buildRulesCard();
        rules.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftCol.add(rules);

        // Right column
        JPanel rightCol = new JPanel();
        rightCol.setOpaque(false);
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        JPanel info = buildSessionInfoCard();
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightCol.add(info);

        c.gridx = 0;
        c.weightx = 0.67;
        c.insets = new Insets(0, 0, 0, 10);
        grid.add(leftCol, c);

        c.gridx = 1;
        c.weightx = 0.33;
        c.insets = new Insets(0, 10, 0, 0);
        grid.add(rightCol, c);

        return grid;
    }

    private JPanel buildWelcomeCard() {
        RoundedPanel card = new RoundedPanel(12);
        card.setFill(Color.WHITE);
        card.setLayout(new BorderLayout(16, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xE2E8F0), 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        welcomeTitle.setForeground(new Color(0x0F172A));
        welcomeTitle.setFont(welcomeTitle.getFont().deriveFont(Font.BOLD, 18.5f));

        JPanel meta = new JPanel();
        meta.setOpaque(false);
        meta.setLayout(new FlowLayout(FlowLayout.LEFT, 24, 0));
        meta.add(styleMetaLabel(welcomeId));
        meta.add(styleMetaLabel(welcomeDept));
        meta.add(styleMetaLabel(welcomeDate));

        statusPill.setOpaque(true);
        statusPill.setBackground(new Color(28, 116, 233, (int) (255 * 0.10)));
        statusPill.setForeground(new Color(0x1C74E9));
        statusPill.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        statusPill.setFont(statusPill.getFont().deriveFont(Font.BOLD, 11.5f));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        welcomeTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        meta.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(welcomeTitle);
        left.add(Box.createVerticalStrut(4));
        left.add(meta);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(statusPill);

        card.add(left, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    private JLabel styleMetaLabel(JLabel l) {
        l.setForeground(new Color(0x475569));
        l.setFont(l.getFont().deriveFont(Font.PLAIN, 12.5f));
        return l;
    }

    private JPanel buildRulesCard() {
        RoundedPanel card = new RoundedPanel(12);
        card.setFill(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xE2E8F0), 1, true),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xF1F5F9)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JLabel title = new JLabel("Nội quy & Quy định phòng thi");
        title.setForeground(new Color(0x0F172A));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16.5f));
        header.add(title, BorderLayout.WEST);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(14, 16, 14, 16));
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        List<String> rules = List.of(
                "Đảm bảo bạn có kết nối internet ổn định. Việc thoát khỏi cửa sổ hoặc chuyển tab sẽ bị đánh dấu là hoạt động nghi ngờ.",
                "Nghiêm cấm sử dụng tài liệu/điện thoại trừ khi có quy định cụ thể của giám thị.",
                "Webcam và micrô của bạn phải luôn được bật trong suốt thời gian thi để giám sát tự động.",
                "Không được phép nghỉ giải lao sau khi đồng hồ tính giờ bắt đầu. Đảm bảo bạn đã sẵn sàng trước khi ca thi bắt đầu."
        );

        for (int i = 0; i < rules.size(); i++) {
            body.add(buildNumberedRule(i + 1, rules.get(i)));
            if (i != rules.size() - 1) body.add(Box.createVerticalStrut(10));
        }

        card.add(header, BorderLayout.NORTH);
        // Giới hạn chiều cao để màn tổng ít phải cuộn
        JScrollPane rulesScroll = new JScrollPane(body);
        rulesScroll.setBorder(BorderFactory.createEmptyBorder());
        rulesScroll.setOpaque(false);
        rulesScroll.getViewport().setOpaque(false);
        rulesScroll.getVerticalScrollBar().setUnitIncrement(16);
        rulesScroll.setPreferredSize(new Dimension(10, 260));
        card.add(rulesScroll, BorderLayout.CENTER);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return card;
    }

    private JPanel buildNumberedRule(int no, String text) {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);

        RoundedPanel bubble = new RoundedPanel(999);
        bubble.setFill(new Color(0xF1F5F9));
        bubble.setPreferredSize(new Dimension(28, 28));
        bubble.setLayout(new BorderLayout());
        JLabel n = new JLabel(String.valueOf(no), SwingConstants.CENTER);
        n.setForeground(new Color(0x0F172A));
        n.setFont(n.getFont().deriveFont(Font.BOLD, 13f));
        bubble.add(n, BorderLayout.CENTER);

        JLabel t = new JLabel("<html><span style='color:#475569; font-size:12.5px; line-height:18px;'>" + escapeHtml(text) + "</span></html>");
        row.add(bubble, BorderLayout.WEST);
        row.add(t, BorderLayout.CENTER);
        return row;
    }

    private JPanel buildWaitingCard() {
        RoundedPanel card = new RoundedPanel(12);
        card.setFill(new Color(0x1C74E9));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));

        JLabel icon = new JLabel(" ");
        icon.setOpaque(true);
        icon.setBackground(Color.WHITE);
        icon.setPreferredSize(new Dimension(32, 40));
        icon.setMaximumSize(new Dimension(32, 40));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        waitingTitle.setForeground(Color.WHITE);
        waitingTitle.setFont(waitingTitle.getFont().deriveFont(Font.BOLD, 20f));
        waitingTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        waitingDesc.setForeground(new Color(255, 255, 255, 230));
        waitingDesc.setFont(waitingDesc.getFont().deriveFont(Font.PLAIN, 14f));
        waitingDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel dots = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        dots.setOpaque(false);
        dots.add(dot());
        dots.add(dot());
        dots.add(dot());

        card.add(icon);
        card.add(Box.createVerticalStrut(16));
        card.add(waitingTitle);
        card.add(Box.createVerticalStrut(8));
        card.add(waitingDesc);
        card.add(Box.createVerticalStrut(16));
        card.add(dots);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return card;
    }

    private JPanel dot() {
        JPanel d = new JPanel();
        d.setOpaque(true);
        d.setBackground(Color.WHITE);
        d.setPreferredSize(new Dimension(8, 8));
        d.setMaximumSize(new Dimension(8, 8));
        d.setBorder(new LineBorder(Color.WHITE, 1, true));
        return d;
    }

    private JPanel buildSessionInfoCard() {
        RoundedPanel card = new RoundedPanel(12);
        card.setFill(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new LineBorder(new Color(0xE2E8F0), 1, true));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xF1F5F9)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        JLabel title = new JLabel("Thông tin Ca thi");
        title.setForeground(new Color(0x0F172A));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16.5f));
        header.add(title, BorderLayout.WEST);

        JPanel img = new JPanel();
        img.setOpaque(true);
        img.setBackground(new Color(0xE2E8F0));
        img.setPreferredSize(new Dimension(302, 92));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(10, 16, 14, 16));
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        body.add(kv("Môn học", sessionSubjectValue, true));
        body.add(Box.createVerticalStrut(8));
        JPanel two = new JPanel();
        two.setOpaque(false);
        two.setLayout(new BoxLayout(two, BoxLayout.X_AXIS));
        two.add(kv("Phòng thi", sessionRoomValue, false));
        two.add(Box.createHorizontalStrut(16));
        two.add(kv("Giờ bắt đầu", sessionStartValue, false));
        body.add(two);
        body.add(Box.createVerticalStrut(10));
        body.add(new JSeparatorLike());
        body.add(Box.createVerticalStrut(8));
        body.add(kvInline("Thời lượng:", sessionDurationValue));
        body.add(Box.createVerticalStrut(5));
        body.add(kvInline("Tổng điểm:", sessionTotalValue));

        card.add(header, BorderLayout.NORTH);
        card.add(img, BorderLayout.CENTER);
        card.add(body, BorderLayout.SOUTH);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return card;
    }

    private JPanel kv(String k, JLabel v, boolean big) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel kk = new JLabel(k.toUpperCase());
        kk.setForeground(new Color(0x94A3B8));
        kk.setFont(kk.getFont().deriveFont(Font.BOLD, 12f));
        v.setForeground(new Color(0x0F172A));
        v.setFont(v.getFont().deriveFont(big ? Font.BOLD : Font.BOLD, big ? 18f : 16f));
        p.add(kk);
        p.add(Box.createVerticalStrut(4));
        p.add(v);
        return p;
    }

    private JPanel kvInline(String k, JLabel v) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel kk = new JLabel(k);
        kk.setForeground(new Color(0x64748B));
        kk.setFont(kk.getFont().deriveFont(Font.PLAIN, 14f));
        v.setForeground(new Color(0x0F172A));
        v.setFont(v.getFont().deriveFont(Font.BOLD, 14f));
        row.add(kk, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        return row;
    }

    private JPanel buildSystemCheckCard() {
        RoundedPanel card = new RoundedPanel(12);
        card.setFill(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new LineBorder(new Color(0xE2E8F0), 1, true));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(14, 16, 14, 16));
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Kiểm tra Hệ thống");
        title.setForeground(new Color(0x0F172A));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        body.add(title);
        body.add(Box.createVerticalStrut(10));
        body.add(checkItem("Webcam"));
        body.add(Box.createVerticalStrut(8));
        body.add(checkItem("Micrô"));
        body.add(Box.createVerticalStrut(8));
        body.add(checkItem("Internet"));

        card.add(body, BorderLayout.CENTER);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return card;
    }

    private JPanel checkItem(String name) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(true);
        row.setBackground(new Color(0xF0FDF4));
        row.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JLabel l = new JLabel(name);
        l.setForeground(new Color(0x15803D));
        l.setFont(l.getFont().deriveFont(Font.PLAIN, 14f));
        JLabel ok = new JLabel("✓");
        ok.setForeground(new Color(0x15803D));
        ok.setFont(ok.getFont().deriveFont(Font.BOLD, 14f));

        row.add(l, BorderLayout.WEST);
        row.add(ok, BorderLayout.EAST);
        return row;
    }

    private JPanel buildPickSessionCard() {
        RoundedPanel card = new RoundedPanel(12);
        card.setFill(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xE2E8F0), 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        pickLabel.setForeground(new Color(0x0F172A));
        pickLabel.setFont(pickLabel.getFont().deriveFont(Font.BOLD, 16f));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(pickLabel, BorderLayout.WEST);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(new EmptyBorder(8, 0, 0, 0));
        center.add(sessionCombo);
        center.add(Box.createVerticalStrut(10));
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(startButton);
        center.add(btnRow);

        card.add(top, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // when change selection, update info card
        sessionCombo.addActionListener(e -> {
            ExamSession selected = (ExamSession) sessionCombo.getSelectedItem();
            updateSessionInfo(selected);
            refreshStartButtonState(selected);
        });
        return card;
    }

    private void updateSessionInfo(ExamSession s) {
        String subject = (s == null || s.getSubject() == null) ? "-" : s.getSubject();
        String name = (s == null || s.getName() == null) ? "-" : s.getName();
        sessionSubjectValue.setText(subject + (name.equals("-") ? "" : (" (" + name + ")")));
        String diaDiem = (s == null || s.getLocation() == null || s.getLocation().isBlank()) ? "-" : s.getLocation().trim();
        sessionRoomValue.setText(diaDiem);
        sessionStartValue.setText(formatStartTime(s == null ? null : s.getStartTimeEpochMs()));
        Integer dur = (s == null) ? null : s.getDurationSeconds();
        sessionDurationValue.setText((dur == null) ? "-" : (Math.max(1, dur / 60) + " Phút"));
        sessionTotalValue.setText("-");
    }

    private void refreshStartButtonState(ExamSession selectedSession) {
        boolean hasSession = sessionCombo.getItemCount() > 0;
        boolean isOpen = hasSession && isOpenSession(selectedSession);
        boolean canStart = !busy && hasSession && isOpen;
        startButton.setEnabled(canStart);
        if (canStart) {
            startButton.setText("Bắt đầu Ca thi");
            startButton.setColors(START_BTN_ACTIVE_BG, START_BTN_ACTIVE_FG);
            return;
        }

        startButton.setColors(START_BTN_DISABLED_BG, START_BTN_DISABLED_FG);
        if (!hasSession) {
            startButton.setText("Không có ca thi khả dụng");
        } else if (isPendingSession(selectedSession)) {
            startButton.setText("Ca thi chưa bắt đầu");
        } else {
            startButton.setText("Bắt đầu Ca thi");
        }
    }

    private boolean isOpenSession(ExamSession session) {
        if (session == null || session.getStatus() == null) return false;
        return "OPEN".equalsIgnoreCase(session.getStatus().trim());
    }

    private boolean isPendingSession(ExamSession session) {
        if (session == null || session.getStatus() == null) return false;
        return "PENDING".equalsIgnoreCase(session.getStatus().trim());
    }

    private String formatStartTime(Long epochMillis) {
        if (epochMillis == null) {
            return "-";
        }
        return Instant.ofEpochMilli(epochMillis)
                .atZone(ZONE_VN)
                .toLocalDateTime()
                .format(START_TIME_FORMAT);
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static class JSeparatorLike extends JPanel {
        JSeparatorLike() {
            setOpaque(true);
            setBackground(new Color(0xF1F5F9));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            setPreferredSize(new Dimension(10, 1));
        }
    }
}

