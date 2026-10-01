package com.bloodbank.auth.dto;

public record LoginResponse(String accessToken, String tokenType, long expiresIn, AuthUserResponse user) {}
