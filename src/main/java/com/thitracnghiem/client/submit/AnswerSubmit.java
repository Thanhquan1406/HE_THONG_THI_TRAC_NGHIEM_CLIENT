package com.thitracnghiem.client.submit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnswerSubmit {
    private Long questionId;
    private Long answerId;
    private List<Long> answerIds = new ArrayList<>();

    public AnswerSubmit() {
    }

    public AnswerSubmit(Long questionId, Long answerId) {
        this.questionId = questionId;
        this.answerId = answerId;
        if (answerId != null) {
            this.answerIds.add(answerId);
        }
    }

    public AnswerSubmit(Long questionId, List<Long> answerIds) {
        this.questionId = questionId;
        this.answerIds = (answerIds == null) ? new ArrayList<>() : new ArrayList<>(answerIds);
        this.answerId = this.answerIds.isEmpty() ? null : this.answerIds.get(0);
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Long answerId) {
        this.answerId = answerId;
    }

    public List<Long> getAnswerIds() {
        return answerIds;
    }

    public void setAnswerIds(List<Long> answerIds) {
        this.answerIds = (answerIds == null) ? new ArrayList<>() : new ArrayList<>(answerIds);
    }
}

