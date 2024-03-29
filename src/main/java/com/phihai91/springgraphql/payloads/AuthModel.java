package com.phihai91.springgraphql.payloads;

import lombok.Builder;

public class AuthModel {
    @Builder
    public record RegistrationUserInput(
            String usernameOrEmail,
            String password) {
    }

    @Builder
    public record RegistrationUserPayload(
            String id,
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
            String usernameOrEmail,
            String password
    ) {
    }

    @Builder
    public record LoginUserPayload(
            String userId,
            String sentTo,
            String otp,
            Boolean twoMFA,
            VerifyOtpPayload credentials
    ) {
    }

    @Builder
    public record VerifyOtpInput(
            String userId,
            String otp
    ) {
    }
}

