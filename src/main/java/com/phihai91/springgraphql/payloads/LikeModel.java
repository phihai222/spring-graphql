package com.phihai91.springgraphql.payloads;

import lombok.Builder;

public class LikeModel {
    @Builder
    public record LikePostInput(
            String postId
    ) {
    }

    @Builder
    public record LikePostPayload(
            String id,
            Boolean liked
    ) {
    }
}
