package com.thitracnghiem.client.ui;

import com.thitracnghiem.client.api.ApiClient;
import com.thitracnghiem.client.api.dto.ExamSession;
import com.thitracnghiem.client.api.dto.LoginRequest;
import com.thitracnghiem.client.api.dto.LoginResponse;
import com.thitracnghiem.client.model.Exam;
import com.thitracnghiem.client.service.DraftService;
import com.thitracnghiem.client.service.SubmitService;
import com.thitracnghiem.client.submit.SubmitResult;
import com.thitracnghiem.client.ws.WSClient;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MainFrame extends JFrame implements AppView {
    private static final String CARD_LOGIN = "login";
    private static final String CARD_SESSIONS = "sessions";
    private static final String CARD_EXAM = "exam";
    private static final String CARD_RESULT = "result";

    private final ApiClient api;
    private volatile WSClient wsClient;
    private final SubmitService submitService;
    private final DraftService draftService;

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    private final LoginPanel loginPanel;
    private final SessionPanel sessionPanel;
    private final ResultPanel resultPanel;
    private JPanel examPanelComponent;
    private AppView examView;

    private volatile LoginResponse currentUser;

    public MainFrame(ApiClient api, SubmitService submitService, DraftService draftService) {
        super("Hệ thống thi online");
        setSystemLookAndFeelQuietly();

        this.api = Objects.requireNonNull(api, "api");
        this.submitService = Objects.requireNonNull(submitService, "submitService");
        this.draftService = Objects.requireNonNull(draftService, "draftService");

        loginPanel = new LoginPanel(this::onLogin);
        sessionPanel = new SessionPanel(new SessionPanel.Listener() {
            @Override
            public void onStart(ExamSession session) {
                onStartSession(session);
            }

            @Override
            public void onLogout() {
                logout();
            }
        });

        resultPanel = new ResultPanel(this::logout);

        root.add(loginPanel, CARD_LOGIN);
        root.add(sessionPanel, CARD_SESSIONS);
        root.add(resultPanel, CARD_RESULT);
        // examPanel sẽ tạo sau khi login (cần studentId)

        setContentPane(root);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 700));
        setLocationRelativeTo(null);
        showLogin();
    }

    public void setWsClient(WSClient wsClient) {
        this.wsClient = wsClient;
    }

    public void setExamPanel(JPanel examPanel) {
        Objects.requireNonNull(examPanel, "examPanel");
        if (!(examPanel instanceof AppView)) {
            throw new IllegalArgumentException("examPanel must implement AppView");
        }
        this.examPanelComponent = examPanel;
        this.examView = (AppView) examPanel;
        root.add(this.examPanelComponent, CARD_EXAM);
    }

    public void start() {
        setVisible(true);
    }

    private void showLogin() {
        SwingUtilities.invokeLater(() -> {
            loginPanel.setBusy(false);
            loginPanel.clearError();
            cards.show(root, CARD_LOGIN);
        });
    }

    private void showSessions() {
        SwingUtilities.invokeLater(() -> {
            sessionPanel.setBusy(false);
            sessionPanel.clearError();
            cards.show(root, CARD_SESSIONS);
        });
    }

    private void showExamCard() {
        SwingUtilities.invokeLater(() -> cards.show(root, CARD_EXAM));
    }

    private void showResultCard() {
        SwingUtilities.invokeLater(() -> cards.show(root, CARD_RESULT));
    }

    private void onSubmitSuccess(Exam exam, SubmitResult result) {
        LoginResponse user = this.currentUser;
        String userName = user != null ? user.getFullName() : null;
        String mssv = user != null ? user.getMssv() : null;
        resultPanel.showResult(exam, result, userName, mssv);
        showResultCard();
    }

    private void onLogin(String mssv, String password) {
        new Thread(() -> {
            try {
                LoginResponse user = api.postJson("/api/auth/login", new LoginRequest(mssv, password), LoginResponse.class);
                this.currentUser = user;

                // Tạo màn hình làm bài đúng studentId sau login
                Long studentId = user.getStudentId();
                if (studentId != null) {
                    ExamPanelV2 examPanel = new ExamPanelV2(studentId, user.getMssv(), submitService, draftService, this::onSubmitSuccess);
                    setExamPanel(examPanel);
                }

                List<ExamSession> sessions = fetchSessions(user.getStudentId());
                SwingUtilities.invokeLater(() -> {
                    sessionPanel.setStudentHeader(user.getFullName(), user.getMssv());
                    sessionPanel.setStudentInfo(user.getFullName(), user.getMssv(), user.getStudentId());
                    sessionPanel.setSessions(sessions);
                    showSessions();
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    loginPanel.setBusy(false);
                    loginPanel.showError(ex.getMessage());
                });
            }
        }, "login-thread").start();
    }

    private List<ExamSession> fetchSessions(Long studentId) throws Exception {
        ExamSession[] sessions = api.getJson("/api/sessions?studentId=" + studentId, ExamSession[].class);
        if (sessions == null) {
            return List.of();
        }
        return Arrays.stream(sessions)
                .filter(Objects::nonNull)
                .filter(s -> s.getName() != null && !s.getName().trim().isEmpty())
                .filter(s -> {
                    String status = s.getStatus();
                    if (status == null) return true;
                    String normalized = status.trim().toUpperCase();
                    return !"CLOSED".equals(normalized) && !"CANCELLED".equals(normalized);
                })
                .collect(Collectors.toList());
    }

    private void onStartSession(ExamSession session) {
        LoginResponse user = this.currentUser;
        if (user == null || user.getStudentId() == null) {
            logout();
            return;
        }
        WSClient wc = this.wsClient;
        if (wc == null) {
            SwingUtilities.invokeLater(() -> sessionPanel.setBusy(false));
            return;
        }

        new Thread(() -> {
            try {
                wc.connectAndSubscribeWithRetry(Duration.ofSeconds(5), Duration.ofSeconds(2));
                waitUntilConnected(wc, Duration.ofSeconds(10));

                Long studentId = user.getStudentId();
                Long sessionId = session.getId();
                api.postJson("/api/sessions/" + sessionId + "/start?studentId=" + studentId + "&questions=10",
                        java.util.Collections.emptyMap(),
                        String.class
                );
                showExamCard();
                SwingUtilities.invokeLater(() -> sessionPanel.setBusy(false));
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    sessionPanel.setBusy(false);
                    sessionPanel.showError(ex.getMessage());
                });
            }
        }, "start-session-thread").start();
    }

    private void logout() {
        this.currentUser = null;
        showLogin();
    }

    @Override
    public void showExam(Exam exam) {
        if (examView != null) {
            showExamCard();
            examView.showExam(exam);
        }
    }

    @Override
    public void onTimeUp() {
        if (examView != null) {
            examView.onTimeUp();
        }
    }

    private static void waitUntilConnected(WSClient client, Duration maxWait) {
        long deadline = System.currentTimeMillis() + Math.max(0, maxWait.toMillis());
        while (System.currentTimeMillis() < deadline) {
            if (client.isConnected()) return;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private static void setSystemLookAndFeelQuietly() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }
}

