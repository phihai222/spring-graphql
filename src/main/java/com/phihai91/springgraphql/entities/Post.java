package com.phihai91.springgraphql.entities;

import com.phihai91.springgraphql.payloads.PostModel;
import lombok.Builder;
import lombok.With;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document
@Builder
@With
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
    public PostModel.CreatePostPayload toCreatePostPayload() {
        return PostModel.CreatePostPayload.builder()
                .id(id)
                .visibility(visibility)
                .post(PostModel.Post.builder()
                        .id(id)
                        .firstName(userInfo == null ? null : userInfo.firstName())
                        .lastName(userInfo == null ? null : userInfo.lastName())
                        .photoUrl(photoUrls)
                        .content(content)
                        .visibility(visibility)
                        .userId(userId)
                        .createdDate(createdDate)
                        .build())
                .build();
    }

    public PostModel.Post toPostPayload() {
        return PostModel.Post.builder()
                .id(id)
                .firstName(userInfo == null ? null : userInfo.firstName())
                .lastName(userInfo == null ? null : userInfo.lastName())
                .photoUrl(photoUrls)
                .content(content)
                .visibility(visibility)
                .userId(userId)
                .createdDate(createdDate)
                .build();
    }
}
