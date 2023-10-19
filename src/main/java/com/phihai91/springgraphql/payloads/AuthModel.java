package com.phihai91.springgraphql.payloads;

import com.phihai91.springgraphql.entities.User;
import lombok.Builder;

public class AuthModel {
    public record RegistrationUserInput(
            String usernameOrEmail,
            String password) {
        public User toUser() {
            return User.builder()
                    .username(usernameOrEmail)
                    .password(password)
                    .build();
        }
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
            String userOrEmail,
            String password
    ) {
    }

    @Builder
    public record LoginUserPayload(
            String sentTo,
            String otp
    ) {
    }

    @Builder
    public record VerifyOtpInput(
            String userOrEmail,
            String otp
    ) {
    }
}

