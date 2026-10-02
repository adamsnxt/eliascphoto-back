package com.project.eliascphoto.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.project.eliascphoto.config.JwtProperties;

@Service
public class JwtTokenService {

    private static final String TOKEN_USE_CLAIM = "token_use";
    private static final String ACCESS_TOKEN_USE = "access";
    private static final String REFRESH_TOKEN_USE = "refresh";

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final JwtProperties properties;

    public JwtTokenService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, JwtProperties properties) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.properties = properties;
    }

    public JwtTokenPair createTokenPair(String userName) {
        if (!StringUtils.hasText(userName)) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio");
        }

        Instant issuedAt = Instant.now();
        Instant accessExpiresAt = issuedAt.plus(properties.getAccessTokenTtl());
        Instant refreshExpiresAt = issuedAt.plus(properties.getRefreshTokenTtl());

        String accessToken = createToken(userName, ACCESS_TOKEN_USE, issuedAt, accessExpiresAt);
        String refreshToken = createToken(userName, REFRESH_TOKEN_USE, issuedAt, refreshExpiresAt);

        return new JwtTokenPair(
                accessToken,
                refreshToken,
                properties.getAccessTokenTtl().toSeconds(),
                properties.getRefreshTokenTtl().toSeconds(),
                "Bearer");
    }

    public Jwt readAccessToken(String token) {
        return readToken(token, ACCESS_TOKEN_USE);
    }

    public Jwt readRefreshToken(String token) {
        return readToken(token, REFRESH_TOKEN_USE);
    }

    private String createToken(String userName, String tokenUse, Instant issuedAt, Instant expiresAt) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.getIssuer())
                .subject(userName)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                .claim(TOKEN_USE_CLAIM, tokenUse)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private Jwt readToken(String token, String expectedUse) {
        Jwt jwt = jwtDecoder.decode(token);
        if (!expectedUse.equals(jwt.getClaimAsString(TOKEN_USE_CLAIM))) {
            throw new BadJwtException("El token no es del tipo esperado");
        }
        return jwt;
    }
}
