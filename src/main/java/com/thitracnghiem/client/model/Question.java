package com.thitracnghiem.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Question {
    private Long id;
    private String content;
    private String questionType;
    private Integer maxSelectableAnswers;
    private List<Answer> answers = new ArrayList<>();

    public Question() {
    }

    public Question(Long id, String content, List<Answer> answers) {
        this.id = id;
        this.content = content;
        this.answers = (answers == null) ? new ArrayList<>() : answers;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public Integer getMaxSelectableAnswers() {
        return maxSelectableAnswers;
    }

    public void setMaxSelectableAnswers(Integer maxSelectableAnswers) {
        this.maxSelectableAnswers = maxSelectableAnswers;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = (answers == null) ? new ArrayList<>() : answers;
    }
}

