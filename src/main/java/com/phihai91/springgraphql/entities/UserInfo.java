package com.phihai91.springgraphql.entities;

import lombok.Builder;

@Builder
public record UserInfo(
        String firstName,
        String lastName,
        String avatarUrl
) {
}
