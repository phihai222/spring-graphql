package com.phihai91.springgraphql.payloads;

import lombok.Builder;

public class UserModel {
    @Builder
    public record User(
            String id,
            String username,
            String firstName,
            String lastName,
            String email,
            Long registrationDate,
            String avatarUrl

    ) {
    }

    @Builder
    public record UpdateUserInput(
            String firstName,
            String lastName,
            String avatarUrl,
            String email
    ) {
    }

    @Builder
    public record SetTwoMFPayload(
            String userId,
            String sentTo,
            String otp
    ) {}
}
