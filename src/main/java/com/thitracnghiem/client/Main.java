package com.thitracnghiem.client;

import com.thitracnghiem.client.api.ApiClient;
import com.thitracnghiem.client.service.DraftService;
import com.thitracnghiem.client.service.SubmitService;
import com.thitracnghiem.client.ui.MainFrame;
import com.thitracnghiem.client.ws.WSClient;

public class Main {
    public static void main(String[] args) throws Exception {
        String wsUrl = (args.length > 0 && args[0] != null && !args[0].isBlank())
                ? args[0].trim()
                : envOrDefault("WS_URL", "ws://localhost:8080/ws");

        String submitUrl = (args.length > 1 && args[1] != null && !args[1].isBlank())
                ? args[1].trim()
                : envOrDefault("SUBMIT_API_URL", "http://localhost:8080/api/submit");

        String apiBaseUrl = (args.length > 2 && args[2] != null && !args[2].isBlank())
                ? args[2].trim()
                : envOrDefault("API_BASE_URL", "http://localhost:8080");

        ApiClient api = new ApiClient(apiBaseUrl);

        SubmitService submitService = new SubmitService(submitUrl);
        DraftService draftService = new DraftService();

        MainFrame frame = new MainFrame(api, submitService, draftService);
        AppController controller = new AppController(frame);
        WSClient wsClient = new WSClient(wsUrl, controller);
        frame.setWsClient(wsClient);
        Runtime.getRuntime().addShutdownHook(new Thread(wsClient::disconnect));

        frame.start();
    }

    private static String envOrDefault(String name, String fallback) {
        try {
            String v = System.getenv(name);
            if (v == null) return fallback;
            String t = v.trim();
            return t.isEmpty() ? fallback : t;
        } catch (Exception ignored) {
            return fallback;
        }
    }
}

