package com.phihai91.springgraphql.entities;

import lombok.Builder;
import lombok.With;

@Builder
@With
public record UserInfo(
        String firstName,
        String lastName,
        String avatarUrl
) {
}
