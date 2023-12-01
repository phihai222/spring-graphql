package com.phihai91.springgraphql.services;

import com.mongodb.lang.Nullable;
import com.phihai91.springgraphql.payloads.CommentModel;
import graphql.relay.Connection;
import reactor.core.publisher.Mono;

public interface ICommentService {
    Mono<CommentModel.Comment> createComment(CommentModel.CommentPostInput inputMono);

    Mono<Connection<CommentModel.Comment>> getCommentByPostId(String postId, int first, @Nullable String cursor);
}
