package com.phihai91.springgraphql.payloads;

import com.phihai91.springgraphql.entities.Visibility;
import graphql.relay.PageInfo;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class PostModel {
    public record CreatePostInput(
            String content,
            List<String> photoUrls,
            Visibility visibility
    ) {
    }

    @Builder
    public record CreatePostPayload(
            String id,
            Visibility visibility,
            Post post,
            LocalDateTime createdDate
    ) {
    }

    @Builder
    public record Post(
            String id,
            String firstName,
            String lastName,
            String content,
            String userId,
            Visibility visibility,
            List<String> photoUrl,
            List<CommentModel.Comment> comments
    ) {
    }

    @Builder
    public record PostConnection(
            PostEdge edge,
            PageInfo pageInfo
    ) {
    }

    @Builder
    public record PostEdge(
            String cursor,
            Post node
    ) {
    }
}
