package com.maayn.iamservice.domain.dto;


public class JwtResponse {

    private String accessToken;
    private String tokenType;

    public JwtResponse() {}

    public JwtResponse(String accessToken, String tokenType) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }
}