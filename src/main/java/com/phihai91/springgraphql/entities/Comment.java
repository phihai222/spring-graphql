package com.phihai91.springgraphql.entities;

import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Builder
public record Comment(
        @Id
        String id,
        String userId,
        String photoUrl,
        String content,
        @CreatedDate
        LocalDateTime createdDate,
        @LastModifiedDate
        LocalDateTime updatedDate
) {
}
