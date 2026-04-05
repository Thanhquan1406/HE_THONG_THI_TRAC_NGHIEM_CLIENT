package com.thitracnghiem.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Exam {
    private Long id;
    private String title;
    /**
     * Thời lượng làm bài (giây). Field này là optional; nếu server không gửi thì client dùng mặc định.
     */
    private Integer durationSeconds;
    private List<Question> questions = new ArrayList<>();

    public Exam() {
    }

    public Exam(Long id, String title, List<Question> questions) {
        this.id = id;
        this.title = title;
        this.questions = (questions == null) ? new ArrayList<>() : questions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = (questions == null) ? new ArrayList<>() : questions;
    }
}

