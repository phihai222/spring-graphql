package com.phihai91.springgraphql.controllers;

import com.phihai91.springgraphql.payloads.LikeModel;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
public class LikeController {
    @MutationMapping
    LikeModel.LikePostPayload likePost(@Argument LikeModel.LikePostInput input) {
        return LikeModel.LikePostPayload.builder()
                .id(input.postId())
                .liked(true)
                .build();
    }
}
