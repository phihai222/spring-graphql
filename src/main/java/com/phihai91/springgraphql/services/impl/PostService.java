package com.phihai91.springgraphql.services.impl;

import com.phihai91.springgraphql.payloads.PostModel;
import com.phihai91.springgraphql.services.IPostService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class PostService implements IPostService {
    @Override
    @PreAuthorize("hasRole('USER')")
    public Mono<PostModel.CreatePostPayload> createPost(PostModel.CreatePostInput input) {
        return Mono.just(PostModel.CreatePostPayload.builder()
                .id("postId")
                .post(PostModel.Post.builder()
                        .id("postId")
                        .comments(List.of())
                        .content(input.content())
                        .fullUserName("Hai")
                        .photoUrl(input.photoUrls())
                        .build())
                .build());
    }
}
