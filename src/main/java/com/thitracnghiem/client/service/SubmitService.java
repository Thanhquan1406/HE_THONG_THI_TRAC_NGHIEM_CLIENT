package com.thitracnghiem.client.service;

import com.thitracnghiem.client.submit.AnswerSubmit;
import com.thitracnghiem.client.submit.SubmitHttpClient;
import com.thitracnghiem.client.submit.SubmitRequest;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SubmitService {
    private final String submitApiUrl;
    private final SubmitHttpClient httpClient;

    public SubmitService(String submitApiUrl) {
        this(submitApiUrl, new SubmitHttpClient());
    }

    public SubmitService(String submitApiUrl, SubmitHttpClient httpClient) {
        this.submitApiUrl = Objects.requireNonNull(submitApiUrl, "submitApiUrl");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
    }

    public String submit(Long studentId, Long examId, Map<Long, Set<Long>> selectedAnswersByQuestionId) throws IOException {
        return submit(studentId, examId, selectedAnswersByQuestionId, null, null, null);
    }

    /**
     * @param totalQuestions   tổng số câu (tùy chọn)
     * @param timeSpentSeconds thời gian làm bài (giây)
     * @param durationSeconds  thời gian giới hạn (giây)
     */
    public String submit(Long studentId, Long examId, Map<Long, Set<Long>> selectedAnswersByQuestionId,
                        Integer totalQuestions, Integer timeSpentSeconds, Integer durationSeconds) throws IOException {
        SubmitRequest req = new SubmitRequest();
        req.setStudentId(studentId);
        req.setExamId(examId);
        req.setTotalQuestions(totalQuestions);
        req.setTimeSpentSeconds(timeSpentSeconds);
        req.setDurationSeconds(durationSeconds);

        List<AnswerSubmit> answers = selectedAnswersByQuestionId.entrySet().stream()
                .map(e -> new AnswerSubmit(
                        e.getKey(),
                        e.getValue() == null ? List.of() : e.getValue().stream().collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
        req.setAnswers(answers);
        return httpClient.submit(submitApiUrl, req);
    }
}

