package com.phihai91.springgraphql.controllers;

import com.phihai91.springgraphql.entities.Post;
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

    //Note: view https://github.com/danvega/sessionz/tree/main

    @QueryMapping
    Mono<Connection<Post>> getMyPosts(
            @Argument Integer first,
            @Argument String after
    ) {
        return postService.getMyPosts(first, after);
    }
}
