package com.thitracnghiem.client.api.dto;

public class LoginRequest {
    private String mssv;
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String mssv, String password) {
        this.mssv = mssv;
        this.password = password;
    }

    public String getMssv() {
        return mssv;
    }

    public void setMssv(String mssv) {
        this.mssv = mssv;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

