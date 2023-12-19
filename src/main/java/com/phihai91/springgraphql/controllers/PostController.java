package com.phihai91.springgraphql.controllers;

import com.phihai91.springgraphql.payloads.CommonModel;
import com.phihai91.springgraphql.payloads.PostModel;
import com.phihai91.springgraphql.services.IPostService;
import graphql.relay.Connection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@Slf4j
public class PostController {
    @Autowired
    private IPostService postService;

    @MutationMapping
    Mono<PostModel.CreatePostPayload> createPost(@Argument PostModel.CreatePostInput input) {
        return postService.createPost(input);
    }

    @QueryMapping
    Mono<Connection<PostModel.Post>> getMyPosts(
            @Argument Integer first,
            @Argument String after
    ) {
        return postService.getPostsByUser(null, first, after);
    }

    @QueryMapping
    Mono<Connection<PostModel.Post>> getPostByUsername(
            @Argument String username,
            @Argument Integer first,
            @Argument String after
    ) {
        return postService.getPostsByUser(username, first, after);
    }

    @MutationMapping
    Mono<CommonModel.CommonPayload> deletePost(@Argument String input){
        return postService.deletePost(input);
    }
}
