package com.thitracnghiem.client.submit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

/**
 * Kết quả nộp bài từ API.
 * Server có thể trả về {@code score}, {@code correctCount}, {@code results}, ...
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubmitResult {
    private Boolean ok;
    private Double score;
    private Integer correctCount;
    private Integer totalQuestions;
    private Integer timeSpentSeconds;
    private Integer durationSeconds;
    private List<Boolean> results;

    public Boolean getOk() {
        return ok;
    }

    public void setOk(Boolean ok) {
        this.ok = ok;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    @JsonProperty("correctCount")
    public Integer getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(Integer correctCount) {
        this.correctCount = correctCount;
    }

    @JsonProperty("totalQuestions")
    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    @JsonProperty("timeSpentSeconds")
    public Integer getTimeSpentSeconds() {
        return timeSpentSeconds;
    }

    public void setTimeSpentSeconds(Integer timeSpentSeconds) {
        this.timeSpentSeconds = timeSpentSeconds;
    }

    @JsonProperty("durationSeconds")
    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public List<Boolean> getResults() {
        return results == null ? Collections.emptyList() : results;
    }

    public void setResults(List<Boolean> results) {
        this.results = results;
    }
}
