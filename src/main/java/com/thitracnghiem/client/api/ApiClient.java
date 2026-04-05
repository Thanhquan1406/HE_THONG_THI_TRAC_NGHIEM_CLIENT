package com.thitracnghiem.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ApiClient {
    private final String baseUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ApiClient(String baseUrl) {
        this.baseUrl = trimTrailingSlash(baseUrl);
    }

    public <T> T postJson(String path, Object body, Class<T> responseType) throws IOException {
        String json = objectMapper.writeValueAsString(body);
        HttpURLConnection conn = open(path);
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(20_000);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        conn.setFixedLengthStreamingMode(bytes.length);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(bytes);
        }

        return readResponse(conn, responseType);
    }

    public <T> T getJson(String path, Class<T> responseType) throws IOException {
        HttpURLConnection conn = open(path);
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(20_000);
        conn.setRequestProperty("Accept", "application/json");
        return readResponse(conn, responseType);
    }

    private HttpURLConnection open(String path) throws IOException {
        String p = path.startsWith("/") ? path : ("/" + path);
        return (HttpURLConnection) new URL(baseUrl + p).openConnection();
    }

    private <T> T readResponse(HttpURLConnection conn, Class<T> responseType) throws IOException {
        int status = conn.getResponseCode();
        InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
        String body = readAll(is);
        if (status < 200 || status >= 300) {
            throw new IOException("HTTP " + status + ": " + body);
        }
        if (responseType == String.class) {
            return responseType.cast(body);
        }
        return objectMapper.readValue(body, responseType);
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

    private static String trimTrailingSlash(String s) {
        if (s == null) return "";
        String t = s.trim();
        while (t.endsWith("/")) t = t.substring(0, t.length() - 1);
        return t;
    }
}

