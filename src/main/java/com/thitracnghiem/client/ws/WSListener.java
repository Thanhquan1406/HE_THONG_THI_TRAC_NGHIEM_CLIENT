package com.thitracnghiem.client.ws;

import com.thitracnghiem.client.model.Exam;

public interface WSListener {
    void onExamReceived(Exam exam);

    void onTimeout();
}

