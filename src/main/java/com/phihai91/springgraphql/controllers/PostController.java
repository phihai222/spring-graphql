package com.phihai91.springgraphql.controllers;

import com.phihai91.springgraphql.payloads.PostModel;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class PostController {
    @MutationMapping
    PostModel.CreatePostPayload createPost(@Argument PostModel.CreatePostInput input) {
        return PostModel.CreatePostPayload.builder()
                .id("postId")
                .post(PostModel.Post.builder()
                        .id("postId")
                        .comments(List.of())
                        .content(input.content())
                        .fullUserName("Hai")
                        .photoUrl(input.photoUrls())
                        .build())
                .build();
    }
}
