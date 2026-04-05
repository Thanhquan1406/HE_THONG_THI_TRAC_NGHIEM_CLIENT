package com.thitracnghiem.client.submit;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SubmitHttpClient {
    private final ObjectMapper objectMapper;

    public SubmitHttpClient() {
        this.objectMapper = new ObjectMapper();
    }

    public SubmitHttpClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Gửi bài thi qua HTTP POST và in response từ server.
     *
     * @return response body (String)
     */
    public String submit(String endpointUrl, SubmitRequest request) throws IOException {
        String json = objectMapper.writeValueAsString(request);

        HttpURLConnection conn = (HttpURLConnection) new URL(endpointUrl).openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(20_000);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");

        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        conn.setFixedLengthStreamingMode(body.length);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body);
        }

        int status = conn.getResponseCode();
        InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
        String respBody = readAll(is);

        System.out.println("HTTP " + status);
        System.out.println("Response: " + respBody);
        return respBody;
    }

    private static String readAll(InputStream is) throws IOException {
        if (is == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString().trim();
        }
    }
}

