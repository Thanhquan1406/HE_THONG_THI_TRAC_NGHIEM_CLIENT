package com.thitracnghiem.client.submit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SubmitRequest {
    private Long studentId;
    private Long examId;
    private List<AnswerSubmit> answers = new ArrayList<>();
    /** Tổng số câu (tùy chọn, dùng cho kết quả) */
    private Integer totalQuestions;
    /** Thời gian làm bài tính bằng giây (tùy chọn) */
    private Integer timeSpentSeconds;
    /** Thời gian giới hạn (giây) (tùy chọn) */
    private Integer durationSeconds;

    public SubmitRequest() {
    }

    public SubmitRequest(Long studentId, Long examId, List<AnswerSubmit> answers) {
        this.studentId = studentId;
        this.examId = examId;
        this.answers = (answers == null) ? new ArrayList<>() : answers;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public List<AnswerSubmit> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerSubmit> answers) {
        this.answers = (answers == null) ? new ArrayList<>() : answers;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Integer getTimeSpentSeconds() {
        return timeSpentSeconds;
    }

    public void setTimeSpentSeconds(Integer timeSpentSeconds) {
        this.timeSpentSeconds = timeSpentSeconds;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }
}

