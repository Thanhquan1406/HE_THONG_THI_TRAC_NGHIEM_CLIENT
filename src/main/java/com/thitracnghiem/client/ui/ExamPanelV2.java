package com.thitracnghiem.client.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitracnghiem.client.model.Answer;
import com.thitracnghiem.client.model.Exam;
import com.thitracnghiem.client.model.Question;
import com.thitracnghiem.client.submit.SubmitResult;
import com.thitracnghiem.client.service.DraftService;
import com.thitracnghiem.client.service.SubmitService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.thitracnghiem.client.ui.components.RoundedButton;
import com.thitracnghiem.client.ui.components.RoundedPanel;

/**
 * UI màn hình "Đề thi" mô phỏng cấu trúc trong HTML bạn gửi:
 * - Sidebar danh sách câu hỏi + chip trạng thái
 * - Khu vực câu hỏi + lựa chọn (radio/checkbox theo loại câu hỏi)
 * - Thanh điều hướng (trước / bỏ chọn / sau)
 * - Box "Lưu ý:"
 *
 * Logic submit/autosave gửi được nhiều đáp án cho mỗi câu hỏi.
 */
public class ExamPanelV2 extends JPanel implements AppView {
    public interface SubmitSuccessListener {
        void onSubmitSuccess(Exam exam, SubmitResult result);
    }

    private static final int DEFAULT_DURATION_SECONDS = 15 * 60;
    private static final int WARNING_SECONDS = 10;
    private static final int AUTOSAVE_INTERVAL_MS = 5_000;

    private static final Color BG_APP = new Color(0xF6F7F8);
    private static final Color COLOR_PRIMARY = new Color(0x1C74E9);
    private static final Color COLOR_GRAY_100 = new Color(0xF1F5F9);
    private static final Color COLOR_GRAY_300 = new Color(0xE2E8F0);
    private static final Color COLOR_GRAY_500 = new Color(0x64748B);
    private static final Color COLOR_NOTE_BG = new Color(0xFFF7ED);
    private static final Color COLOR_REVIEW_BG = new Color(0xFFEDD5);
    private static final Color COLOR_REVIEW_BORDER = new Color(0xFED7AA);

    private final Long studentId;
    private final String mssv;
    private final SubmitService submitService;
    private final DraftService draftService;
    private SubmitSuccessListener submitSuccessListener;

    private final JLabel subjectLabel;
    private final JLabel countdownLabel;
    private final JLabel studentLabel;

    private final JPanel chipsPanel;
    private final JLabel answeredBadge;
    private final JCheckBox reviewCheck;

    private final JLabel questionTitle;
    private final JLabel questionContent;
    private final JPanel optionsContainer;

    private final RoundedButton prevButton;
    private final RoundedButton clearButton;
    private final RoundedButton nextButton;
    private final RoundedPanel noteBox;

    private final RoundedButton saveButton;
    private final RoundedButton submitButton;

    private volatile Exam currentExam;
    private volatile boolean timeUp = false;
    private volatile int currentQuestionIndex = 0;
    private volatile Long currentQuestionId = null;

    private final Map<Long, Set<Long>> selectedAnswersByQuestionId =
            Collections.synchronizedMap(new LinkedHashMap<>());
    private final Map<Long, Boolean> reviewByQuestionId =
            Collections.synchronizedMap(new LinkedHashMap<>());

    private final AtomicBoolean submitting = new AtomicBoolean(false);
    private Timer countdownTimer;
    private Timer autosaveTimer;
    private int remainingSeconds = 0;
    private boolean warned10s = false;

    private ButtonGroup optionGroup = new ButtonGroup();
    private final List<AbstractButton> currentOptionButtons = new ArrayList<>();

    public ExamPanelV2(Long studentId, SubmitService submitService, DraftService draftService) {
        this(studentId, null, submitService, draftService, null);
    }

    public void setSubmitSuccessListener(SubmitSuccessListener listener) {
        this.submitSuccessListener = listener;
    }

    public ExamPanelV2(Long studentId, SubmitService submitService, DraftService draftService, SubmitSuccessListener submitSuccessListener) {
        this(studentId, null, submitService, draftService, submitSuccessListener);
    }

    public ExamPanelV2(Long studentId, String mssv, SubmitService submitService, DraftService draftService, SubmitSuccessListener submitSuccessListener) {
        this.studentId = Objects.requireNonNull(studentId, "studentId");
        this.mssv = mssv;
        this.submitService = Objects.requireNonNull(submitService, "submitService");
        this.draftService = Objects.requireNonNull(draftService, "draftService");
        this.submitSuccessListener = submitSuccessListener;

        setLayout(new BorderLayout(12, 12));
        setBackground(BG_APP);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(12, 12, 6, 12));

        JPanel leftStack = new JPanel();
        leftStack.setOpaque(false);
        leftStack.setLayout(new BoxLayout(leftStack, BoxLayout.Y_AXIS));
        JLabel mônThiSmall = new JLabel("Môn thi");
        mônThiSmall.setForeground(COLOR_GRAY_500);
        mônThiSmall.setFont(mônThiSmall.getFont().deriveFont(Font.BOLD, 14f));

        subjectLabel = new JLabel("Chưa có đề thi");
        subjectLabel.setForeground(new Color(0x0F172A));
        subjectLabel.setFont(subjectLabel.getFont().deriveFont(Font.BOLD, 20f));
        leftStack.add(mônThiSmall);
        leftStack.add(subjectLabel);

        JPanel centerStack = new JPanel();
        centerStack.setOpaque(false);
        centerStack.setLayout(new BoxLayout(centerStack, BoxLayout.Y_AXIS));
        JLabel timeSmall = new JLabel("Thời gian còn lại");
        timeSmall.setForeground(COLOR_GRAY_500);
        timeSmall.setFont(timeSmall.getFont().deriveFont(Font.BOLD, 14f));

        countdownLabel = new JLabel("00:00");
        countdownLabel.setForeground(COLOR_PRIMARY);
        countdownLabel.setFont(countdownLabel.getFont().deriveFont(Font.BOLD, 28f));
        centerStack.add(timeSmall);
        centerStack.add(countdownLabel);

        JPanel rightStack = new JPanel();
        rightStack.setOpaque(false);
        rightStack.setLayout(new BoxLayout(rightStack, BoxLayout.Y_AXIS));
        studentLabel = new JLabel("MSSV: " + (mssv != null && !mssv.isBlank() ? mssv : studentId));
        studentLabel.setForeground(COLOR_GRAY_500);
        studentLabel.setFont(studentLabel.getFont().deriveFont(Font.PLAIN, 12f));
        rightStack.add(studentLabel);

        header.add(leftStack, BorderLayout.WEST);
        header.add(centerStack, BorderLayout.CENTER);
        header.add(rightStack, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Body: sidebar + main
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);

        JPanel sidebar = new JPanel();
        sidebar.setOpaque(true);
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, COLOR_GRAY_300));
        sidebar.setPreferredSize(new Dimension(290, 600));
        sidebar.setLayout(new BorderLayout());

        // sidebar inner top
        JPanel sidebarInner = new JPanel();
        sidebarInner.setOpaque(false);
        sidebarInner.setLayout(new BoxLayout(sidebarInner, BoxLayout.Y_AXIS));
        sidebarInner.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.setBorder(new EmptyBorder(20, 20, 16, 20));
        titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        JLabel sidebarTitle = new JLabel("Danh sách câu hỏi");
        sidebarTitle.setForeground(new Color(0x0F172A));
        sidebarTitle.setFont(sidebarTitle.getFont().deriveFont(Font.BOLD, 17f));

        RoundedPanel badgeWrap = new RoundedPanel(12);
        badgeWrap.setFill(new Color(0xEEF2FF));
        badgeWrap.setLayout(new BorderLayout());
        badgeWrap.setBorder(new EmptyBorder(6, 14, 6, 14));
        answeredBadge = new JLabel("0/0 Câu");
        answeredBadge.setForeground(new Color(0x1E40AF));
        answeredBadge.setFont(answeredBadge.getFont().deriveFont(Font.BOLD, 13f));
        answeredBadge.setHorizontalAlignment(SwingConstants.CENTER);
        badgeWrap.add(answeredBadge, BorderLayout.CENTER);

        titleRow.add(sidebarTitle, BorderLayout.CENTER);
        titleRow.add(badgeWrap, BorderLayout.EAST);
        sidebarInner.add(titleRow);

        chipsPanel = new JPanel();
        chipsPanel.setOpaque(false);
        chipsPanel.setLayout(new GridLayout(0, 4, 10, 10));
        chipsPanel.setBorder(new EmptyBorder(4, 20, 16, 20));
        sidebarInner.add(chipsPanel);

        JPanel legend = new JPanel();
        legend.setOpaque(false);
        legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));
        legend.setBorder(new EmptyBorder(12, 20, 12, 20));
        legend.add(buildLegendItem(COLOR_PRIMARY, "Đã trả lời"));
        legend.add(Box.createVerticalStrut(10));
        legend.add(buildLegendItem(COLOR_GRAY_100, "Chưa trả lời"));

        sidebarInner.add(legend);
        sidebarInner.add(Box.createVerticalGlue());

        saveButton = new RoundedButton("Lưu Bài");
        saveButton.setArc(10);
        saveButton.setPreferredSize(new Dimension(145, 36));
        saveButton.setColors(new Color(0x155DFC), Color.WHITE);
        saveButton.setEnabled(false);
        saveButton.addActionListener(e -> saveDraftNow());

        submitButton = new RoundedButton("NỘP BÀI THI");
        submitButton.setArc(12);
        submitButton.setPreferredSize(new Dimension(248, 48));
        submitButton.setColors(new Color(0x1C74E9), Color.WHITE);
        submitButton.setEnabled(false);
        submitButton.addActionListener(e -> submitExam());

        sidebarInner.add(Box.createVerticalStrut(10));
        sidebarInner.add(saveButton);
        sidebarInner.add(Box.createVerticalStrut(12));
        sidebarInner.add(submitButton);
        sidebarInner.add(Box.createVerticalStrut(12));

        sidebar.add(sidebarInner, BorderLayout.CENTER);
        body.add(sidebar, BorderLayout.WEST);

        // Main question card
        JPanel mainWrap = new JPanel(new BorderLayout());
        mainWrap.setOpaque(false);
        mainWrap.setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 0));

        RoundedPanel questionCard = new RoundedPanel(12);
        questionCard.setFill(Color.WHITE);
        questionCard.setLayout(new BorderLayout());
        questionCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_GRAY_300, 1, true),
                BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));

        JPanel questionHeader = new JPanel(new BorderLayout());
        questionHeader.setOpaque(false);
        questionHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        questionTitle = new JLabel("Câu hỏi 01");
        questionTitle.setForeground(COLOR_PRIMARY);
        questionTitle.setFont(questionTitle.getFont().deriveFont(Font.BOLD, 18f));

        reviewCheck = new JCheckBox("Đánh dấu xem lại");
        reviewCheck.setOpaque(false);
        reviewCheck.setForeground(COLOR_GRAY_500);
        reviewCheck.setFont(reviewCheck.getFont().deriveFont(Font.BOLD, 14f));
        reviewCheck.addActionListener(e -> {
            if (timeUp) return;
            if (currentQuestionId == null) return;
            reviewByQuestionId.put(currentQuestionId, reviewCheck.isSelected());
            updateChipsStyles();
        });

        JPanel headerRight = new JPanel();
        headerRight.setOpaque(false);
        headerRight.setLayout(new BoxLayout(headerRight, BoxLayout.X_AXIS));
        headerRight.add(Box.createHorizontalStrut(8));
        // Theo yêu cầu: bỏ xác nhận/đánh dấu xem lại (không hiển thị)
        reviewCheck.setVisible(false);
        headerRight.add(Box.createHorizontalStrut(0));

        questionHeader.add(questionTitle, BorderLayout.WEST);
        questionHeader.add(headerRight, BorderLayout.EAST);

        optionsContainer = new JPanel();
        optionsContainer.setOpaque(false);
        optionsContainer.setLayout(new BoxLayout(optionsContainer, BoxLayout.Y_AXIS));

        questionContent = new JLabel("");
        questionContent.setFont(questionContent.getFont().deriveFont(Font.PLAIN, 16f));
        questionContent.setForeground(new Color(0x0F172A));
        questionContent.setVerticalAlignment(SwingConstants.TOP);
        questionContent.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

        JPanel questionStack = new JPanel();
        questionStack.setOpaque(false);
        questionStack.setLayout(new BoxLayout(questionStack, BoxLayout.Y_AXIS));
        questionStack.add(questionContent);
        questionStack.add(optionsContainer);

        JScrollPane scroll = new JScrollPane(questionStack);
        scroll.setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);

        // Navigation + note
        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.X_AXIS));
        nav.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));

        prevButton = new RoundedButton("Câu trước");
        prevButton.setArc(8);
        prevButton.setColors(COLOR_GRAY_100, new Color(0x475569).darker());
        prevButton.setPreferredSize(new Dimension(144, 48));
        prevButton.setEnabled(false);
        prevButton.addActionListener(e -> goToQuestion(currentQuestionIndex - 1));

        clearButton = new RoundedButton("Bỏ chọn");
        clearButton.setArc(8);
        clearButton.setColors(COLOR_GRAY_100, new Color(0x0F172A));
        clearButton.setPreferredSize(new Dimension(144, 48));
        clearButton.setEnabled(false);
        clearButton.addActionListener(e -> clearCurrentSelection());

        nextButton = new RoundedButton("Câu sau");
        nextButton.setArc(8);
        nextButton.setColors(COLOR_PRIMARY, Color.WHITE);
        nextButton.setPreferredSize(new Dimension(144, 48));
        nextButton.setEnabled(false);
        nextButton.addActionListener(e -> goToQuestion(currentQuestionIndex + 1));

        nav.add(prevButton);
        nav.add(Box.createHorizontalStrut(16));
        // Theo yêu cầu: bỏ nút "Bỏ chọn" (không hiển thị)
        clearButton.setVisible(false);
        nav.add(Box.createHorizontalStrut(0));
        nav.add(nextButton);

        noteBox = new RoundedPanel(12);
        noteBox.setFill(COLOR_NOTE_BG);
        noteBox.setLayout(new BorderLayout());
        noteBox.setBorder(new LineBorder(COLOR_REVIEW_BORDER, 1, true));
        noteBox.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_REVIEW_BORDER, 1, true),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        noteLabel = new JLabel("<html><b>Lưu ý:</b> Nhớ lưu bài liên tục tránh gặp vấn đề trong quá trình làm bài.</html>");
        noteLabel.setForeground(new Color(0x9A3412));
        noteLabel.setFont(noteLabel.getFont().deriveFont(Font.PLAIN, 14f));
        noteBox.add(noteLabel, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(nav, BorderLayout.NORTH);
        bottom.add(noteBox, BorderLayout.CENTER);

        questionCard.add(questionHeader, BorderLayout.NORTH);
        questionCard.add(scroll, BorderLayout.CENTER);
        questionCard.add(bottom, BorderLayout.SOUTH);

        mainWrap.add(questionCard, BorderLayout.CENTER);
        body.add(mainWrap, BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);
    }

    private JLabel noteLabel;

    private static JPanel buildLegendItem(Color color, String label) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(0, 0, 0, 0));

        RoundedPanel square = new RoundedPanel(6);
        square.setFill(color);
        square.setPreferredSize(new Dimension(12, 12));
        row.add(square, BorderLayout.WEST);

        JLabel l = new JLabel(label);
        l.setForeground(new Color(0x475569));
        l.setFont(l.getFont().deriveFont(Font.BOLD, 12f));
        row.add(l, BorderLayout.CENTER);
        return row;
    }

    private static JPanel buildLegendItem(Color bg, String label, Color border) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);

        JPanel square = new JPanel();
        square.setOpaque(false);
        square.setPreferredSize(new Dimension(12, 12));

        RoundedPanel fill = new RoundedPanel(6);
        fill.setFill(bg);
        fill.setPreferredSize(new Dimension(12, 12));
        fill.setBorder(new LineBorder(border, 1, true));
        row.add(fill, BorderLayout.WEST);

        JLabel l = new JLabel(label);
        l.setForeground(new Color(0x475569));
        l.setFont(l.getFont().deriveFont(Font.BOLD, 12f));
        row.add(l, BorderLayout.CENTER);
        return row;
    }

    @Override
    public void showExam(Exam exam) {
        Objects.requireNonNull(exam, "exam");
        Runnable render = () -> {
            this.currentExam = exam;
            selectedAnswersByQuestionId.clear();
            reviewByQuestionId.clear();
            timeUp = false;
            warned10s = false;

            subjectLabel.setText(safe(exam.getTitle(), "Đề thi"));

            List<Question> questions = exam.getQuestions() == null ? List.of() : exam.getQuestions();
            int total = questions.size();
            answeredBadge.setText("0/" + total + " Câu");
            buildChips(total);

            saveButton.setEnabled(total > 0);
            submitButton.setEnabled(total > 0);

            startTimersForExam(exam);

            if (total > 0) showQuestion(0);
            else {
                questionTitle.setText("Không có câu hỏi");
                questionContent.setText("");
                optionsContainer.removeAll();
                optionsContainer.revalidate();
                optionsContainer.repaint();
            }
        };

        if (SwingUtilities.isEventDispatchThread()) render.run();
        else SwingUtilities.invokeLater(render);
    }

    private void buildChips(int total) {
        chipsPanel.removeAll();
        if (total <= 0) {
            chipsPanel.revalidate();
            chipsPanel.repaint();
            return;
        }

        for (int i = 0; i < total; i++) {
            int idx = i;
            ChipButton chip = new ChipButton(String.format("%02d", idx + 1));
            chip.setFocusPainted(false);
            chip.setPreferredSize(new Dimension(52, 44));
            chip.setFont(chip.getFont().deriveFont(Font.BOLD, 14f));
            chip.setVisual(Color.WHITE, new Color(0x0F172A), COLOR_GRAY_300, 1);
            chip.addActionListener(e -> showQuestion(idx));
            chipsPanel.add(chip);
        }
        chipsPanel.revalidate();
        chipsPanel.repaint();
        updateChipsStyles();
    }

    private void showQuestion(int idx) {
        if (currentExam == null) return;
        List<Question> questions = currentExam.getQuestions() == null ? List.of() : currentExam.getQuestions();
        if (idx < 0 || idx >= questions.size()) return;

        currentQuestionIndex = idx;
        Question q = questions.get(idx);
        currentQuestionId = q == null ? null : q.getId();

        questionTitle.setText("Câu hỏi " + String.format("%02d", idx + 1));

        // Không hiển thị checkbox xác nhận "Đánh dấu xem lại"

        // content
        String content = safe(q == null ? "" : q.getContent(), "");
        questionContent.setText("<html>" + escapeHtml(content).replace("\n", "<br/>") + "</html>");

        // options
        optionsContainer.removeAll();
        optionGroup = new ButtonGroup();
        currentOptionButtons.clear();

        JPanel optionsStack = new JPanel();
        optionsStack.setOpaque(false);
        optionsStack.setLayout(new BoxLayout(optionsStack, BoxLayout.Y_AXIS));

        List<Answer> answers = q == null || q.getAnswers() == null ? List.of() : q.getAnswers();
        boolean choPhepChonNhieu = q != null && "TRAC_NGHIEM_NHIEU_DAP_AN".equalsIgnoreCase(q.getQuestionType());
        int gioiHanChon = (q != null && q.getMaxSelectableAnswers() != null && q.getMaxSelectableAnswers() > 0)
                ? q.getMaxSelectableAnswers()
                : (choPhepChonNhieu ? answers.size() : 1);
        Set<Long> selectedIds = currentQuestionId == null
                ? Set.of()
                : selectedAnswersByQuestionId.getOrDefault(currentQuestionId, Set.of());
        for (int i = 0; i < answers.size(); i++) {
            Answer a = answers.get(i);
            Long answerId = a == null ? null : a.getId();
            String ansText = safe(a == null ? "" : a.getContent(), "");
            String label = toAlphabet(i) + ". " + ansText;

            AbstractButton optionButton;
            if (choPhepChonNhieu) {
                JCheckBox cb = new JCheckBox(label);
                cb.setOpaque(true);
                cb.setFocusable(false);
                cb.setFont(cb.getFont().deriveFont(Font.PLAIN, 16f));
                cb.setForeground(new Color(0x334155));
                cb.setBackground(new Color(0xF8FAFC));
                cb.setBorder(new LineBorder(COLOR_GRAY_300, 1, true));
                optionButton = cb;
            } else {
                JRadioButton rb = new JRadioButton(label);
                rb.setOpaque(true);
                rb.setFocusable(false);
                rb.setFont(rb.getFont().deriveFont(Font.PLAIN, 16f));
                rb.setForeground(new Color(0x334155));
                rb.setBackground(new Color(0xF8FAFC));
                rb.setBorder(new LineBorder(COLOR_GRAY_300, 1, true));
                optionGroup.add(rb);
                optionButton = rb;
            }

            optionButton.putClientProperty("answerId", answerId);
            optionButton.setEnabled(!timeUp);

            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setOpaque(true);
            wrapper.setBackground(new Color(0xF8FAFC));
            wrapper.setBorder(new LineBorder(COLOR_GRAY_300, 1, true));
            wrapper.setPreferredSize(new Dimension(700, 50));
            wrapper.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(COLOR_GRAY_300, 1, true),
                    BorderFactory.createEmptyBorder(14, 16, 14, 16)
            ));
            wrapper.add(optionButton, BorderLayout.CENTER);

            optionsStack.add(wrapper);
            optionsStack.add(Box.createVerticalStrut(14));

            currentOptionButtons.add(optionButton);

            if (answerId != null && selectedIds.contains(answerId)) {
                optionButton.setSelected(true);
            }

            optionButton.addActionListener(e -> {
                if (timeUp || currentQuestionId == null || answerId == null) return;
                Set<Long> selected = selectedAnswersByQuestionId.computeIfAbsent(currentQuestionId, k -> new LinkedHashSet<>());
                if (choPhepChonNhieu) {
                    if (optionButton.isSelected()) {
                        if (!selected.contains(answerId) && selected.size() >= gioiHanChon) {
                            optionButton.setSelected(false);
                            Toolkit.getDefaultToolkit().beep();
                            JOptionPane.showMessageDialog(
                                    this,
                                    "Câu này chỉ được chọn tối đa " + gioiHanChon + " đáp án.",
                                    "Giới hạn lựa chọn",
                                    JOptionPane.WARNING_MESSAGE
                            );
                            return;
                        }
                        selected.add(answerId);
                    } else {
                        selected.remove(answerId);
                    }
                } else {
                    selected.clear();
                    selected.add(answerId);
                }
                if (selected.isEmpty()) {
                    selectedAnswersByQuestionId.remove(currentQuestionId);
                }
                updateAnsweredBadge();
                updateChipsStyles();
            });
        }

        // remove last spacer if any
        optionsContainer.setLayout(new BorderLayout());
        optionsContainer.add(optionsStack, BorderLayout.CENTER);

        // nav
        int total = currentExam.getQuestions() == null ? 0 : currentExam.getQuestions().size();
        prevButton.setEnabled(idx > 0 && !timeUp);
        nextButton.setEnabled(idx < total - 1 && !timeUp);
        clearButton.setEnabled(!timeUp);

        updateAnsweredBadge();
        updateChipsStyles();

        optionsContainer.revalidate();
        optionsContainer.repaint();
    }

    private void updateAnsweredBadge() {
        if (currentExam == null) return;
        int total = currentExam.getQuestions() == null ? 0 : currentExam.getQuestions().size();
        int answered = selectedAnswersByQuestionId.size();
        answeredBadge.setText(answered + "/" + total + " Câu");
    }

    private void updateChipsStyles() {
        if (currentExam == null) return;
        int total = currentExam.getQuestions() == null ? 0 : currentExam.getQuestions().size();
        if (total <= 0) return;
        if (chipsPanel.getComponentCount() < total) return;

        List<Question> questions = currentExam.getQuestions();
        for (int i = 0; i < total; i++) {
            Component comp = chipsPanel.getComponent(i);
            if (!(comp instanceof ChipButton)) continue;
            ChipButton chip = (ChipButton) comp;

            Question q = questions.get(i);
            Long qId = q == null ? null : q.getId();
            boolean isCurrent = i == currentQuestionIndex;
            boolean isReview = qId != null && Boolean.TRUE.equals(reviewByQuestionId.get(qId));
            boolean isAnswered = qId != null && selectedAnswersByQuestionId.containsKey(qId);

            if (isReview) {
                // Cần xem lại: nền vàng
                chip.setVisual(new Color(0xFFEDD5), new Color(0x9A3412), new Color(0xFDBA74), 1);
            } else if (isAnswered) {
                // Đã trả lời: nền xanh, chữ trắng
                chip.setVisual(COLOR_PRIMARY, Color.WHITE, COLOR_PRIMARY, 1);
            } else {
                // Chưa trả lời: nền trắng, chữ đen
                chip.setVisual(Color.WHITE, new Color(0x0F172A), COLOR_GRAY_300, 1);
            }

            // Nhấn câu hiện tại bằng viền đậm (không đổi quy tắc màu trạng thái)
            if (isCurrent) {
                chip.setBorderColor(COLOR_PRIMARY);
                chip.setBorderWidth(2);
            }
        }
    }

    private static class ChipButton extends JButton {
        private Color bg = Color.WHITE;
        private Color fg = new Color(0x0F172A);
        private Color border = new Color(0xE2E8F0);
        private int borderWidth = 1;
        private int arc = 8;

        ChipButton(String text) {
            super(text);
            setFocusPainted(false);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
        }

        void setVisual(Color bg, Color fg, Color border, int borderWidth) {
            this.bg = bg == null ? Color.WHITE : bg;
            this.fg = fg == null ? Color.BLACK : fg;
            this.border = border == null ? new Color(0xE2E8F0) : border;
            this.borderWidth = Math.max(1, borderWidth);
            setForeground(this.fg);
            repaint();
        }

        void setBorderColor(Color c) {
            this.border = c == null ? this.border : c;
            repaint();
        }

        void setBorderWidth(int w) {
            this.borderWidth = Math.max(1, w);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                g2.setColor(border);
                for (int i = 0; i < borderWidth; i++) {
                    g2.drawRoundRect(i, i, getWidth() - 1 - i * 2, getHeight() - 1 - i * 2, arc, arc);
                }
                // Vẽ số câu thủ công để tránh bị cắt thành "..."
                String text = getText();
                if (text != null && !text.isEmpty()) {
                    g2.setColor(fg);
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(text)) / 2;
                    int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(text, x, y);
                }
            } finally {
                g2.dispose();
            }
        }
    }

    private void clearCurrentSelection() {
        if (timeUp) return;
        if (currentQuestionId == null) return;
        selectedAnswersByQuestionId.remove(currentQuestionId);
        updateAnsweredBadge();
        updateChipsStyles();

        // unselect radios
        optionGroup.clearSelection();
        for (AbstractButton button : currentOptionButtons) {
            button.setSelected(false);
        }
        optionsContainer.revalidate();
        optionsContainer.repaint();
    }

    private void goToQuestion(int newIndex) {
        if (currentExam == null) return;
        List<Question> questions = currentExam.getQuestions() == null ? List.of() : currentExam.getQuestions();
        if (newIndex < 0 || newIndex >= questions.size()) return;
        showQuestion(newIndex);
    }

    @Override
    public void onTimeUp() {
        Runnable r = () -> {
            if (timeUp) return;
            timeUp = true;
            stopCountdownTimer();
            stopAutosaveTimer();
            lockAnswerEditing();
            // Auto submit
            submitExam();
        };

        if (SwingUtilities.isEventDispatchThread()) r.run();
        else SwingUtilities.invokeLater(r);
    }

    private void lockAnswerEditing() {
        for (AbstractButton button : currentOptionButtons) button.setEnabled(false);
        reviewCheck.setEnabled(false);
        prevButton.setEnabled(false);
        nextButton.setEnabled(false);
        clearButton.setEnabled(false);

        // also block chips click
        for (int i = 0; i < chipsPanel.getComponentCount(); i++) {
            chipsPanel.getComponent(i).setEnabled(false);
        }
        saveButton.setEnabled(false);
        submitButton.setEnabled(true);
    }

    private void submitExam() {
        if (!submitting.compareAndSet(false, true)) return;

        Exam exam = this.currentExam;
        if (exam == null) {
            JOptionPane.showMessageDialog(this, "Chưa có đề thi để nộp.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            submitting.set(false);
            return;
        }

        Map<Long, Set<Long>> answers = getSelectedAnswersByQuestionId();
        submitButton.setEnabled(false);
        saveButton.setEnabled(false);
        lockAnswerEditing();

        int totalQuestions = exam.getQuestions() == null ? 0 : exam.getQuestions().size();
        int durationSeconds = exam.getDurationSeconds() != null ? exam.getDurationSeconds() : DEFAULT_DURATION_SECONDS;
        int timeSpentSeconds = Math.max(0, durationSeconds - remainingSeconds);

        new Thread(() -> {
            try {
                String resp = submitService.submit(studentId, exam.getId(), answers, totalQuestions, timeSpentSeconds, durationSeconds);
                System.out.println("Submit response: " + resp);

                SubmitResult result = parseSubmitResult(resp);
                if (result == null) {
                    result = buildFallbackResult(totalQuestions, timeSpentSeconds, durationSeconds, answers.size());
                }
                final SubmitResult resultFinal = result;
                SubmitSuccessListener listener = submitSuccessListener;
                Exam examRef = exam;
                SwingUtilities.invokeLater(() -> {
                    if (listener != null) {
                        listener.onSubmitSuccess(examRef, resultFinal);
                    } else {
                        JOptionPane.showMessageDialog(this, "Đã gửi bài thi.\nSố câu đã chọn: " + answers.size(), "Nộp bài", JOptionPane.INFORMATION_MESSAGE);
                    }
                });
            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                        this,
                        "Gửi bài thất bại: " + ex.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                ));
            } finally {
                submitting.set(false);
                SwingUtilities.invokeLater(() -> {
                    submitButton.setEnabled(true);
                    saveButton.setEnabled(!timeUp);
                });
            }
        }, "submit-exam").start();
    }

    private static SubmitResult parseSubmitResult(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return new ObjectMapper().readValue(json, SubmitResult.class);
        } catch (Exception e) {
            return null;
        }
    }

    private static SubmitResult buildFallbackResult(int totalQuestions, int timeSpentSeconds, int durationSeconds, int answeredCount) {
        SubmitResult r = new SubmitResult();
        r.setOk(true);
        r.setTotalQuestions(totalQuestions);
        r.setTimeSpentSeconds(timeSpentSeconds);
        r.setDurationSeconds(durationSeconds);
        r.setCorrectCount(answeredCount);
        r.setScore(totalQuestions > 0 ? (answeredCount * 10.0 / totalQuestions) : 0);
        return r;
    }

    private void saveDraftNow() {
        Exam exam = this.currentExam;
        if (exam == null || exam.getId() == null) {
            JOptionPane.showMessageDialog(this, "Chưa có đề thi để lưu.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (timeUp) {
            JOptionPane.showMessageDialog(this, "Đã hết giờ, không thể lưu.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            Map<Long, Set<Long>> answers = getSelectedAnswersByQuestionId();
            Path p = draftService.saveDraft(studentId, exam.getId(), answers);
            JOptionPane.showMessageDialog(this, "Đã lưu bài tạm.\n" + p.toAbsolutePath(), "Lưu bài", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lưu bài thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Map<Long, Set<Long>> getSelectedAnswersByQuestionId() {
        synchronized (selectedAnswersByQuestionId) {
            Map<Long, Set<Long>> copy = new LinkedHashMap<>();
            selectedAnswersByQuestionId.forEach((k, v) -> copy.put(k, v == null ? Set.of() : new LinkedHashSet<>(v)));
            return copy;
        }
    }

    private void startTimersForExam(Exam exam) {
        stopCountdownTimer();
        stopAutosaveTimer();

        Integer durationFromServer = exam.getDurationSeconds();
        int duration = (durationFromServer != null && durationFromServer > 0) ? durationFromServer : DEFAULT_DURATION_SECONDS;
        remainingSeconds = duration;
        warned10s = false;
        updateCountdownLabel();

        countdownTimer = new Timer(1_000, e -> onCountdownTick());
        countdownTimer.setRepeats(true);
        countdownTimer.start();

        autosaveTimer = new Timer(AUTOSAVE_INTERVAL_MS, e -> autosaveDraftQuietly());
        autosaveTimer.setRepeats(true);
        autosaveTimer.start();
    }

    private void onCountdownTick() {
        if (timeUp) return;
        remainingSeconds = Math.max(0, remainingSeconds - 1);
        updateCountdownLabel();

        if (!warned10s && remainingSeconds == WARNING_SECONDS) {
            warned10s = true;
            Toolkit.getDefaultToolkit().beep();
            countdownLabel.setFont(countdownLabel.getFont().deriveFont(Font.BOLD));
            countdownLabel.setText("00:" + String.format("%02d", WARNING_SECONDS));
            JOptionPane.showMessageDialog(this, "Còn 10 giây! Vui lòng kiểm tra lại đáp án.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        }

        if (remainingSeconds <= 0) onTimeUp();
    }

    private void updateCountdownLabel() {
        int mm = remainingSeconds / 60;
        int ss = remainingSeconds % 60;
        countdownLabel.setText(String.format("%02d:%02d", mm, ss));
        if (!timeUp && remainingSeconds <= WARNING_SECONDS) {
            countdownLabel.setForeground(Color.RED);
        } else {
            countdownLabel.setForeground(COLOR_PRIMARY);
        }
    }

    private void autosaveDraftQuietly() {
        Exam exam = this.currentExam;
        if (exam == null || exam.getId() == null) return;
        if (timeUp) return;

        try {
            Map<Long, Set<Long>> answers = getSelectedAnswersByQuestionId();
            Path p = draftService.saveDraft(studentId, exam.getId(), answers);
            System.out.println("Đã lưu tạm bài: " + p.toAbsolutePath());
        } catch (Exception ex) {
            System.err.println("Lưu tạm thất bại: " + ex.getMessage());
        }
    }

    private void stopCountdownTimer() {
        if (countdownTimer != null) {
            countdownTimer.stop();
            countdownTimer = null;
        }
    }

    private void stopAutosaveTimer() {
        if (autosaveTimer != null) {
            autosaveTimer.stop();
            autosaveTimer = null;
        }
    }

    private static String safe(String s, String fallback) {
        if (s == null) return fallback;
        String t = s.trim();
        return t.isEmpty() ? fallback : t;
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static String toAlphabet(int idx) {
        int zero = idx % 26;
        return String.valueOf((char) ('A' + zero));
    }
}

