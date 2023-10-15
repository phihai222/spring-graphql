package com.phihai91.springgraphql.payloads;

import lombok.Builder;

public class CommentModel {
    @Builder
    public record Comment(
            String id,
            String userId,
            String content,
            String postId
    ) {
    }

    @Builder
    public record CommentPostInput(
            String postId,
            String content
    ) {
    }
}
