package com.phihai91.springgraphql.controllers;

import com.phihai91.springgraphql.payloads.CommentModel;
import com.phihai91.springgraphql.services.impl.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
public class CommentController {
    @Autowired
    private CommentService commentService;
    @MutationMapping
    Mono<CommentModel.Comment> commentPost(@Argument CommentModel.CommentPostInput input) {
        return commentService.createComment(input);
    }
}
