package com.phihai91.springgraphql.controllers;

import com.phihai91.springgraphql.payloads.PostModel;
import com.phihai91.springgraphql.services.IPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class PostController {
    @Autowired
    private IPostService postService;

    @MutationMapping
    Mono<PostModel.CreatePostPayload> createPost(@Argument PostModel.CreatePostInput input) {
        return postService.createPost(input);
    }

    @QueryMapping
    Flux<PostModel.PostConnection> getMyPosts(
            @Argument("first") Integer first,
            @Argument("after") String after,
            @Argument("last") Integer last,
            @Argument("before") String before
    ) {
        return postService.getMyPosts(first, after, last, before);
    }
}
