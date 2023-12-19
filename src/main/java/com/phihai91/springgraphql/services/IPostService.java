package com.phihai91.springgraphql.services;

import com.mongodb.lang.Nullable;
import com.phihai91.springgraphql.payloads.CommonModel;
import com.phihai91.springgraphql.payloads.PostModel;
import graphql.relay.Connection;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IPostService {
    Mono<PostModel.CreatePostPayload> createPost(PostModel.CreatePostInput input);
    Flux<PostModel.CreatePostPayload> getPostsByUser();
    Flux<PostModel.CreatePostPayload> getPostsByUser(Pageable pageable);
    Mono<Connection<PostModel.Post>> getPostsByUser(String userId, int first, @Nullable String cursor);
    Mono<CommonModel.CommonPayload> deletePost(String input);
}
