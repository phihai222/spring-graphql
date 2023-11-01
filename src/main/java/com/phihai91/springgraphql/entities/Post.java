package com.phihai91.springgraphql.entities;

import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document
@Builder
public record Post(
        @Id
        String id,
        String userId,
        UserInfo userInfo,
        Visibility visibility,
        String content,
        List<String> photoUrls,
        @CreatedDate
        LocalDateTime createdDate,
        @LastModifiedDate
        LocalDateTime updatedDate,
        List<Comment> comments,
        List<Like> likes
) {
}
