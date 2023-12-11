package com.phihai91.springgraphql.entities;

import lombok.Builder;
import lombok.With;

import java.time.LocalDateTime;

@With
@Builder
public record FriendData(
        String id,
        LocalDateTime addedDate
) {
}
