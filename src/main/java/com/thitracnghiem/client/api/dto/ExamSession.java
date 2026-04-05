package com.thitracnghiem.client.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExamSession {
    private Long id;
    private String name;
    private String subject;
    private Integer durationSeconds;
    private String location;
    private Long startTimeEpochMs;
    private String status;

    public ExamSession() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Long getStartTimeEpochMs() {
        return startTimeEpochMs;
    }

    public void setStartTimeEpochMs(Long startTimeEpochMs) {
        this.startTimeEpochMs = startTimeEpochMs;
    }

    public String toDisplayText() {
        String n = (name == null || name.isBlank()) ? ("Ca " + id) : name;
        String s = (subject == null || subject.isBlank()) ? "" : (" - " + subject);
        return n + s + " (" + (status == null ? "UNKNOWN" : status) + ")";
    }
}

