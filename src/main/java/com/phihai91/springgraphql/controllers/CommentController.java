package com.phihai91.springgraphql.controllers;

import com.phihai91.springgraphql.payloads.CommentModel;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
public class CommentController {
    @MutationMapping
    CommentModel.Comment commentPost(@Argument CommentModel.CommentPostInput input) {
        return CommentModel.Comment.builder()
                .content(input.content())
                .id("commentId")
                .userId("currentUser")
                .postId(input.postId())
                .build();
    }
}
