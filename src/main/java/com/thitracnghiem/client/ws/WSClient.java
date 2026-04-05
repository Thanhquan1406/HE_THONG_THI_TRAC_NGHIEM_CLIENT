package com.thitracnghiem.client.ws;

import com.thitracnghiem.client.model.Exam;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class WSClient {
    private final String url;
    private final WSListener listener;
    private final WebSocketStompClient stompClient;
    private volatile StompSession session;

    public WSClient(String url, WSListener listener) {
        this.url = Objects.requireNonNull(url, "url");
        this.listener = Objects.requireNonNull(listener, "listener");
        this.stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    public void connectAndSubscribe() throws Exception {
        connectAndSubscribe(Duration.ofSeconds(10));
    }

    public void connectAndSubscribe(Duration timeout) throws Exception {
        try {
            ListenableFuture<StompSession> future = stompClient.connect(url, new StompSessionHandlerAdapter() {});
            this.session = future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            this.session.subscribe("/topic/exam", new ExamFrameHandler());
            this.session.subscribe("/topic/timeout", new TimeoutFrameHandler());
            System.out.println("Đã kết nối WebSocket và subscribe /topic/exam, /topic/timeout");
        } catch (Exception e) {
            System.err.println("Không kết nối được WebSocket tới " + url + ". Lỗi: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Kết nối có retry để app không bị crash khi server chưa bật.
     */
    public void connectAndSubscribeWithRetry(Duration timeoutPerTry, Duration retryDelay) {
        Objects.requireNonNull(timeoutPerTry, "timeoutPerTry");
        Objects.requireNonNull(retryDelay, "retryDelay");

        while (true) {
            try {
                connectAndSubscribe(timeoutPerTry);
                return;
            } catch (Exception ex) {
                try {
                    Thread.sleep(Math.max(500, retryDelay.toMillis()));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    public boolean isConnected() {
        StompSession s = this.session;
        return s != null && s.isConnected();
    }

    public void disconnect() {
        try {
            StompSession s = this.session;
            if (s != null && s.isConnected()) {
                s.disconnect();
            }
        } finally {
            stompClient.stop();
        }
    }

    private class ExamFrameHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(StompHeaders headers) {
            return Exam.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            if (!(payload instanceof Exam)) {
                System.err.println("Payload không phải Exam: " + payload);
                return;
            }

            Exam exam = (Exam) payload;
            System.out.println("Nhận đề thi: " + exam.getTitle());
            listener.onExamReceived(exam);
        }
    }

    private class TimeoutFrameHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(StompHeaders headers) {
            return String.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            String msg = (payload == null) ? "" : payload.toString().trim();
            if (!"HET_GIO".equalsIgnoreCase(msg)) {
                return;
            }

            System.out.println("Hết giờ, tự động nộp bài");
            listener.onTimeout();
        }
    }
}

