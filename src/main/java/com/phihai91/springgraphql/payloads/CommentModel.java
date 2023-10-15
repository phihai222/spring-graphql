package com.phihai91.springgraphql.payloads;

public class CommentModel {
    public record Comment(
            String id,
            String userId,
            String content
    ) {}

    public record CommentPostInput() {
    }

    public record CommentPostPayload() {
    }
}
