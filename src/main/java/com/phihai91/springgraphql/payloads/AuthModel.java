package com.phihai91.springgraphql.payloads;

import lombok.Builder;

public class AuthModel {
    public record RegistrationUserInput(
            String usernameOrEmail,
            String password) {
    }

    @Builder
    public record RegistrationUserPayload(
            String UUID,
            VerifyOtpPayload credentials
    ) {
    }

    @Builder
    public record VerifyOtpPayload(
            String type,
            String accessToken,
            Long signedDate,
            Long expiredDate
    ) {
    }

    @Builder
    public record LoginUserInput(
            String userOrEmail,
            String password
    ) {}

    @Builder
    public record LoginUserPayload(
            String sentTo,
            String otp
    ){}
}

