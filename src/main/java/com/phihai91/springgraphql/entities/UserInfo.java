package com.phihai91.springgraphql.entities;

import io.soabase.recordbuilder.core.RecordBuilder;
import lombok.Builder;

@Builder
@RecordBuilder
public record UserInfo(
        String firstName,
        String lastName,
        String avatarUrl
) {
}
