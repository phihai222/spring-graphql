package com.phihai91.springgraphql.services;

import com.mongodb.lang.Nullable;
import com.phihai91.springgraphql.entities.Post;
import com.phihai91.springgraphql.payloads.PostModel;
import graphql.relay.Connection;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IPostService {
    Mono<PostModel.CreatePostPayload> createPost(PostModel.CreatePostInput input);
    Flux<PostModel.CreatePostPayload> getMyPosts();
    Flux<PostModel.CreatePostPayload> getMyPosts(Pageable pageable);
    Mono<Connection<Post>> getMyPosts(int first, @Nullable String cursor);
}
