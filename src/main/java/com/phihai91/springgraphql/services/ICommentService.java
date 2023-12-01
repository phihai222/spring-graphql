package com.phihai91.springgraphql.services;

import com.phihai91.springgraphql.payloads.CommentModel;
import reactor.core.publisher.Mono;

public interface ICommentService {
    Mono<CommentModel.Comment> createComment(CommentModel.CommentPostInput inputMono);
}
