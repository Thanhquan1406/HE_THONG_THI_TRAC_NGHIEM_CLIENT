package com.thitracnghiem.client;

import com.thitracnghiem.client.model.Exam;
import com.thitracnghiem.client.ui.AppView;
import com.thitracnghiem.client.ws.WSListener;

/**
 * Nơi nối WS events -> UI/Services.
 */
public class AppController implements WSListener {
    private final AppView ui;

    public AppController(AppView ui) {
        this.ui = ui;
    }

    @Override
    public void onExamReceived(Exam exam) {
        ui.showExam(exam);
    }

    @Override
    public void onTimeout() {
        ui.onTimeUp();
    }
}

