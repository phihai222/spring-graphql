package com.phihai91.springgraphql.entities;

import lombok.Builder;
import lombok.With;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@With
@Builder
public record FriendData(
        @Id
        String id,
        String userId,
        LocalDateTime addedDate
) {
}
