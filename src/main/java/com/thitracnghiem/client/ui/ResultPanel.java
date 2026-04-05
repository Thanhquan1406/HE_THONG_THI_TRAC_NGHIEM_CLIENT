package com.thitracnghiem.client.ui;

import com.thitracnghiem.client.model.Exam;
import com.thitracnghiem.client.submit.SubmitResult;
import com.thitracnghiem.client.ui.components.RoundedButton;
import com.thitracnghiem.client.ui.components.RoundedPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Màn hình kết quả thi theo thiết kế ketqua.html.
 */
public class ResultPanel extends JPanel {
    private static final Color BG_APP = new Color(0xF6F7F8);
    private static final Color COLOR_PRIMARY = new Color(0x1C74E9);
    private static final Color COLOR_CORRECT = new Color(0x00A651);
    private static final Color COLOR_WRONG = new Color(0xEF4444);
    private static final Color COLOR_GRAY_400 = new Color(0x62748E);
    private static final Color COLOR_GRAY_500 = new Color(0x64748B);
    private static final Color COLOR_GRAY_LIGHT = new Color(0x90A1B9);

    public interface Listener {
        void onLogout();
    }

    private final Listener listener;
    private final JLabel examTitleLabel;
    private final JLabel examSubtitleLabel;
    private final JLabel scoreValueLabel;
    private final JLabel timeValueLabel;
    private final JLabel timeLimitLabel;
    private final JLabel correctCountLabel;
    private final JPanel chipsWrap;
    private final JLabel footerLabel;
    private final JLabel sidebarUserName;
    private final JLabel sidebarMssv;

    public ResultPanel(Listener listener) {
        this.listener = Objects.requireNonNull(listener, "listener");
        setLayout(new BorderLayout());
        setBackground(BG_APP);

        examTitleLabel = new JLabel("-");
        examSubtitleLabel = new JLabel("-");
        scoreValueLabel = new JLabel("-");
        timeValueLabel = new JLabel("-");
        timeLimitLabel = new JLabel("-");
        correctCountLabel = new JLabel("-");
        chipsWrap = new JPanel();
        footerLabel = new JLabel("© 2026 Hệ thống quản lý thi trực tuyến - Hutech");
        sidebarUserName = new JLabel("-");
        sidebarMssv = new JLabel("MSSV: -");

        add(buildSidebar(), BorderLayout.WEST);
        add(buildMainContent(), BorderLayout.CENTER);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(256, 700));
        sidebar.setBackground(Color.WHITE);
        sidebar.setLayout(new BorderLayout());
        sidebar.setBorder(new LineBorder(new Color(0xE2E8F0), 1));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(24, 24, 24, 24));
        top.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xF1F5F9)));

        RoundedPanel logoBox = new RoundedPanel(8);
        logoBox.setFill(COLOR_PRIMARY);
        logoBox.setPreferredSize(new Dimension(34, 30));

        JLabel logoText = new JLabel("UniExam");
        logoText.setFont(logoText.getFont().deriveFont(Font.BOLD, 18f));
        logoText.setForeground(new Color(0x0F172A));

        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        logoRow.setOpaque(false);
        logoRow.add(logoBox);
        logoRow.add(logoText);
        top.add(logoRow, BorderLayout.NORTH);
        sidebar.add(top, BorderLayout.NORTH);

        JPanel userInfo = new JPanel(new BorderLayout());
        userInfo.setOpaque(false);
        userInfo.setBorder(new EmptyBorder(16, 16, 16, 16));
        userInfo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xF1F5F9)),
                new EmptyBorder(16, 16, 16, 16)
        ));
        sidebarUserName.setFont(sidebarUserName.getFont().deriveFont(Font.BOLD, 14f));
        sidebarUserName.setForeground(new Color(0x0F172A));
        sidebarMssv.setFont(sidebarMssv.getFont().deriveFont(Font.PLAIN, 12f));
        sidebarMssv.setForeground(COLOR_GRAY_500);
        JPanel userStack = new JPanel(new BorderLayout(0, 4));
        userStack.setOpaque(false);
        userStack.add(sidebarUserName, BorderLayout.NORTH);
        userStack.add(sidebarMssv, BorderLayout.CENTER);
        userInfo.add(userStack, BorderLayout.CENTER);
        sidebar.add(userInfo, BorderLayout.CENTER);

        JPanel nav = new JPanel(new BorderLayout());
        nav.setOpaque(false);
        nav.setBorder(new EmptyBorder(16, 16, 16, 16));

        RoundedPanel resultItem = new RoundedPanel(8);
        resultItem.setFill(new Color(0x1C, 0x74, 0xE9, 0x1A));
        resultItem.setLayout(new BorderLayout());
        resultItem.setBorder(new EmptyBorder(10, 12, 10, 12));
        JLabel resultLabel = new JLabel("Kết quả");
        resultLabel.setForeground(COLOR_PRIMARY);
        resultLabel.setFont(resultLabel.getFont().deriveFont(Font.BOLD, 14f));
        resultItem.add(resultLabel, BorderLayout.CENTER);
        nav.add(resultItem, BorderLayout.NORTH);

        JPanel centerWrap = new JPanel();
        centerWrap.setLayout(new BoxLayout(centerWrap, BoxLayout.Y_AXIS));
        centerWrap.setOpaque(false);
        centerWrap.add(userInfo);
        centerWrap.add(Box.createVerticalStrut(8));
        centerWrap.add(nav);
        centerWrap.add(Box.createVerticalGlue());
        sidebar.add(centerWrap, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(16, 16, 16, 16));
        bottom.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xF1F5F9)),
                new EmptyBorder(16, 16, 16, 16)
        ));
        JButton logoutBtn = new JButton("Đăng xuất");
        logoutBtn.setForeground(COLOR_GRAY_500);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.addActionListener(e -> listener.onLogout());
        bottom.add(logoutBtn);
        sidebar.add(bottom, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel buildMainContent() {
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(20, 40, 20, 40));

        center.add(buildInfoCard(), BorderLayout.NORTH);
        center.add(Box.createVerticalStrut(20), BorderLayout.CENTER);
        center.add(buildDetailCard(), BorderLayout.CENTER);
        center.add(buildFooter(), BorderLayout.SOUTH);

        JScrollPane scroll = new JScrollPane(center);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_APP);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        main.add(scroll, BorderLayout.CENTER);
        return main;
    }

    private JPanel buildInfoCard() {
        RoundedPanel card = new RoundedPanel(14);
        card.setFill(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xE2E8F0), 1),
                new EmptyBorder(33, 33, 33, 33)
        ));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        examTitleLabel.setFont(examTitleLabel.getFont().deriveFont(Font.BOLD, 30f));
        examTitleLabel.setForeground(new Color(0x0F172B));
        header.add(examTitleLabel, BorderLayout.NORTH);
        examSubtitleLabel.setFont(examSubtitleLabel.getFont().deriveFont(Font.PLAIN, 14f));
        examSubtitleLabel.setForeground(COLOR_GRAY_400);
        examSubtitleLabel.setBorder(new EmptyBorder(8, 0, 0, 0));
        header.add(examSubtitleLabel, BorderLayout.CENTER);
        card.add(header, BorderLayout.NORTH);

        JPanel stats = new JPanel(new FlowLayout(FlowLayout.LEFT, 48, 0));
        stats.setOpaque(false);
        stats.setBorder(new EmptyBorder(24, 0, 0, 0));

        stats.add(buildStatBox("Điểm số", scoreValueLabel, "/ 10", true));
        stats.add(buildTimeStatBox());
        stats.add(buildStatBox("Số câu đúng", correctCountLabel, null, false));

        card.add(stats, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildStatBox(String label, JLabel valueLabel, String suffix, boolean primaryColor) {
        RoundedPanel wrap = new RoundedPanel(14);
        wrap.setFill(Color.WHITE);
        wrap.setLayout(new BorderLayout(0, 4));
        wrap.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xE2E8F0), 1),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel lbl = new JLabel(label);
        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 14f));
        lbl.setForeground(COLOR_GRAY_400);
        wrap.add(lbl, BorderLayout.NORTH);

        JPanel valRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        valRow.setOpaque(false);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 30f));
        valueLabel.setForeground(primaryColor ? new Color(0x155DFC) : new Color(0x0F172B));
        valRow.add(valueLabel);
        if (suffix != null) {
            JLabel suff = new JLabel(suffix);
            suff.setFont(suff.getFont().deriveFont(Font.PLAIN, 18f));
            suff.setForeground(COLOR_GRAY_LIGHT);
            valRow.add(suff);
        }
        wrap.add(valRow, BorderLayout.CENTER);
        wrap.setPreferredSize(new Dimension(220, 110));
        return wrap;
    }

    private JPanel buildTimeStatBox() {
        RoundedPanel wrap = new RoundedPanel(14);
        wrap.setFill(Color.WHITE);
        wrap.setLayout(new BorderLayout(0, 4));
        wrap.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xE2E8F0), 1),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel lbl = new JLabel("Thời gian làm bài");
        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 14f));
        lbl.setForeground(COLOR_GRAY_400);
        wrap.add(lbl, BorderLayout.NORTH);

        JPanel valCol = new JPanel(new BorderLayout(0, 4));
        valCol.setOpaque(false);
        timeValueLabel.setFont(timeValueLabel.getFont().deriveFont(Font.BOLD, 30f));
        timeValueLabel.setForeground(new Color(0x0F172B));
        valCol.add(timeValueLabel, BorderLayout.NORTH);
        timeLimitLabel.setFont(timeLimitLabel.getFont().deriveFont(Font.PLAIN, 12f));
        timeLimitLabel.setForeground(COLOR_GRAY_LIGHT);
        valCol.add(timeLimitLabel, BorderLayout.CENTER);
        wrap.add(valCol, BorderLayout.CENTER);
        wrap.setPreferredSize(new Dimension(220, 110));
        return wrap;
    }

    private JPanel buildDetailCard() {
        RoundedPanel card = new RoundedPanel(14);
        card.setFill(Color.WHITE);
        card.setLayout(new BorderLayout(0, 32));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xE2E8F0), 1),
                new EmptyBorder(32, 32, 32, 32)
        ));

        chipsWrap.setLayout(new GridLayout(0, 10, 8, 8));
        chipsWrap.setOpaque(false);

        JScrollPane chipScroll = new JScrollPane(chipsWrap);
        chipScroll.setBorder(null);
        chipScroll.getViewport().setBackground(Color.WHITE);
        chipScroll.setPreferredSize(new Dimension(800, 220));
        card.add(chipScroll, BorderLayout.CENTER);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 0));
        legend.setOpaque(false);
        legend.add(buildLegendItem(COLOR_CORRECT, "Câu trả lời đúng"));
        legend.add(buildLegendItem(COLOR_WRONG, "Câu trả lời sai"));
        card.add(legend, BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildLegendItem(Color color, String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        RoundedPanel square = new RoundedPanel(6);
        square.setFill(color);
        square.setPreferredSize(new Dimension(16, 16));
        p.add(square);
        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(0x45556C));
        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 14f));
        p.add(lbl);
        return p;
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        footerLabel.setForeground(COLOR_GRAY_500);
        footerLabel.setFont(footerLabel.getFont().deriveFont(Font.PLAIN, 14f));
        p.add(footerLabel);
        p.setBorder(new EmptyBorder(20, 0, 0, 0));
        return p;
    }

    /**
     * Hiển thị kết quả.
     *
     * @param exam   đề thi (có thể null)
     * @param result kết quả từ API
     * @param userName tên sinh viên
     * @param mssv   mã số sinh viên
     */
    public void showResult(Exam exam, SubmitResult result, String userName, String mssv) {
        String title = (exam != null && exam.getTitle() != null) ? exam.getTitle() : "Đề thi";
        examTitleLabel.setText(title);

        sidebarUserName.setText(userName != null ? userName : "-");
        sidebarMssv.setText(mssv != null ? ("MSSV: " + mssv) : "MSSV: -");

        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        examSubtitleLabel.setText("Ngày thi: " + dateStr + " • Mã học phần: - • Giảng viên: -");

        if (result != null) {
            Double score = result.getScore();
            scoreValueLabel.setText(score != null ? String.format("%.1f", score) : "-");

            Integer timeSpent = result.getTimeSpentSeconds();
            if (timeSpent != null) {
                int m = timeSpent / 60;
                int s = timeSpent % 60;
                timeValueLabel.setText(String.format("%02d:%02d", m, s));
            } else {
                timeValueLabel.setText("-");
            }

            Integer duration = result.getDurationSeconds();
            if (duration != null) {
                int dm = duration / 60;
                int ds = duration % 60;
                timeLimitLabel.setText("Giới hạn: " + String.format("%02d:%02d", dm, ds));
            } else {
                timeLimitLabel.setText("");
            }

            Integer correct = result.getCorrectCount();
            Integer total = result.getTotalQuestions();
            if (correct != null && total != null) {
                correctCountLabel.setText(correct + " / " + total);
            } else {
                correctCountLabel.setText("-");
            }

            buildResultChips(result.getResults(), total != null ? total : 0);
        } else {
            scoreValueLabel.setText("-");
            timeValueLabel.setText("-");
            timeLimitLabel.setText("");
            correctCountLabel.setText("-");
            chipsWrap.removeAll();
        }
        revalidate();
        repaint();
    }

    private void buildResultChips(List<Boolean> results, int total) {
        chipsWrap.removeAll();
        if (total <= 0 && (results == null || results.isEmpty())) return;

        int n = (results != null && !results.isEmpty()) ? results.size() : total;
        for (int i = 0; i < n; i++) {
            boolean correct = (results != null && i < results.size()) ? results.get(i) : false;
            RoundedPanel chip = new RoundedPanel(4);
            chip.setFill(correct ? COLOR_CORRECT : COLOR_WRONG);
            chip.setLayout(new BorderLayout());
            JLabel lbl = new JLabel("Câu " + (i + 1), SwingConstants.CENTER);
            lbl.setForeground(Color.WHITE);
            lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 14f));
            chip.add(lbl, BorderLayout.CENTER);
            chip.setPreferredSize(new Dimension(72, 36));
            chipsWrap.add(chip);
        }
        chipsWrap.revalidate();
        chipsWrap.repaint();
    }
}
