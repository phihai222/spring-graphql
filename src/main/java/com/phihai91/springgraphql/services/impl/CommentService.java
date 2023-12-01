package com.phihai91.springgraphql.services.impl;

import com.phihai91.springgraphql.entities.Comment;
import com.phihai91.springgraphql.entities.Post;
import com.phihai91.springgraphql.exceptions.NotFoundException;
import com.phihai91.springgraphql.payloads.CommentModel;
import com.phihai91.springgraphql.repositories.ICommentRepository;
import com.phihai91.springgraphql.repositories.IPostRepository;
import com.phihai91.springgraphql.securities.AppUserDetails;
import com.phihai91.springgraphql.services.ICommentService;
import com.phihai91.springgraphql.ultis.CursorUtils;
import com.phihai91.springgraphql.ultis.UserHelper;
import graphql.relay.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService implements ICommentService {
    @Autowired
    private ICommentRepository commentRepository;
    @Autowired
    private IPostRepository postRepository;
    @Autowired
    private CursorUtils cursorUtils;

    @Override
    @PreAuthorize("hasRole('USER')")
    public Mono<CommentModel.Comment> createComment(CommentModel.CommentPostInput input) {
        Mono<AppUserDetails> appUserDetailsMono = ReactiveSecurityContextHolder.getContext()
                .map(UserHelper::getUserDetails);

        Mono<Post> postRes = postRepository.findById(input.postId())
                .switchIfEmpty(Mono.error(new NotFoundException("Post not found")));

        Mono<Comment> commentSaved = Mono.zip(appUserDetailsMono, postRes)
                .flatMap(tuple -> commentRepository.save(Comment.builder()
                        .content(input.content())
                        .photoUrl(input.imageUrl())
                        .postId(input.postId())
                        .userId(tuple.getT1().getId())
                        .build()));

        return commentSaved.map(comment -> CommentModel.Comment.builder()
                .id(comment.id())
                .content(comment.content())
                .userId(comment.userId())
                .postId(comment.postId())
                .build());
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public Mono<Connection<CommentModel.Comment>> getCommentByPostId(String postId, int first, String cursor) {
        Mono<Post> postRes = postRepository.findById(postId)
                .switchIfEmpty(Mono.error(new NotFoundException("Post not found")));

        Mono<List<Edge<CommentModel.Comment>>> commentRes = getComments(postId, first, cursor)
                .map(comment -> CommentModel.Comment.builder()
                        .id(comment.id())
                        .content(comment.content())
                        .postId(comment.postId())
                        .userId(comment.userId())
                        .imageUrl(comment.photoUrl())
                        .build())
                .map(comment -> new DefaultEdge<>(comment, cursorUtils.from(comment.id())))
                .collect(Collectors.toUnmodifiableList());

        return postRes
                .flatMap(p -> commentRes)
                .map(edges -> {
                    DefaultPageInfo pageInfo = new DefaultPageInfo(
                            cursorUtils.getFirstCursorFrom(edges),
                            cursorUtils.getLastCursorFrom(edges),
                            cursor != null,
                            edges.size() >= first);
                    return new DefaultConnection<>(edges, pageInfo);
                });
    }

    private Flux<Comment> getComments(String postId, int first, String cursor) {
        return cursor == null ? commentRepository.findAllByPostIdStart(postId, first)
                : commentRepository.findAllByPostIdBefore(postId, cursor, first);
    }
}
