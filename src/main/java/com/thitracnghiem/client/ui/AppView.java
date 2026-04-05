package com.thitracnghiem.client.ui;

import com.thitracnghiem.client.model.Exam;

public interface AppView {
    void showExam(Exam exam);

    void onTimeUp();
}

