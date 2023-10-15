package com.phihai91.springgraphql.payloads;

import lombok.Builder;

import java.util.List;

public class PostModel {
    public record CreatePostInput(
            String content,
            List<String> photoUrls
    ) {
    }

    @Builder
    public record CreatePostPayload(
            String id,
            Post post
    ) {
    }

    @Builder
    public record Post(
            String id,
            String fullUserName,
            String content,
            List<String> photoUrl,
            List<CommentModel.Comment> comments
    ) {
    }
}
