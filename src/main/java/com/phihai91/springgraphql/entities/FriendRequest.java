package com.phihai91.springgraphql.entities;

import lombok.Builder;
import lombok.With;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@With
@Builder
public record FriendRequest(
        @Id
        String id,
        String fromUser,
        String toUser,
        Boolean isIgnore,
        String message,
        @CreatedDate
        LocalDateTime createdDate,
        @LastModifiedDate
        LocalDateTime updatedDate
        ) {}
