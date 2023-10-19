package com.phihai91.springgraphql.entities;

import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Builder
public record UserPost(
        String id,

        @CreatedDate
        LocalDateTime createdDate,
        Visibility visibility
) {
}
