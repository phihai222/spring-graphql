package com.phihai91.springgraphql.payloads;

import com.phihai91.springgraphql.entities.Visibility;
import lombok.Builder;

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
            Post post
    ) {
    }

    @Builder
    public record Post(
            String id,
            String firstName,
            String lastName,
            String content,
            Visibility visibility,
            List<String> photoUrl,
            List<CommentModel.Comment> comments
    ) {
    }
}
