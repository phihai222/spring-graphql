package com.phihai91.springgraphql.securities;

import com.phihai91.springgraphql.entities.User;
import com.phihai91.springgraphql.payloads.AuthModel;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

@Component
@Slf4j
public class JwtTokenProvider {
    @Value("${jwtSecret}")
    private String jwtSecret;

    @Value("${jwtExpirationMs}")
    private int jwtExpirationMs;

    private SecretKey secretKey;

    private static final String AUTHORITIES_KEY = "roles";

    @PostConstruct
    public void init() {
        var secret = Base64.getEncoder()
                .encodeToString(jwtSecret.getBytes());
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public AuthModel.VerifyOtpPayload createToken(User user) {
        List<GrantedAuthority> authorities = user.roles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());

        var claimsBuilder = Jwts.claims().subject(user.id());

        if (!authorities.isEmpty()) {
            claimsBuilder.add(AUTHORITIES_KEY, authorities.stream()
                    .map(GrantedAuthority::getAuthority).collect(joining(",")));
        }

        var claims = claimsBuilder.build();

        Date now = new Date();
        Date validity = new Date(now.getTime() + this.jwtExpirationMs);

        return AuthModel.VerifyOtpPayload.builder()
                .accessToken(Jwts.builder()
                        .subject(user.id())
                        .claims(claims)
                        .issuedAt(now)
                        .expiration(validity)
                        .signWith(secretKey, Jwts.SIG.HS256).compact())
                .expiredDate(validity.getTime())
                .signedDate(now.getTime())
                .type("Bearer")
                .build();
    }
}
