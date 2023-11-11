package com.phihai91.springgraphql.services;

import com.phihai91.springgraphql.payloads.PostModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IPostService {
    Mono<PostModel.CreatePostPayload> createPost(PostModel.CreatePostInput input);
    Flux<PostModel.CreatePostPayload> getMyPosts();
}
