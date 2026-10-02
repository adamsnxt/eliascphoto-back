package com.project.eliascphoto.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtTokenPair {

    private final String accessToken;
    private final String refreshToken;
    private final long accessTokenExpiresIn;
    private final long refreshTokenExpiresIn;
    private final String tokenType;
}
