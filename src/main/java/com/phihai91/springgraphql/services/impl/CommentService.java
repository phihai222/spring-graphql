package com.phihai91.springgraphql.services.impl;

import com.phihai91.springgraphql.entities.Comment;
import com.phihai91.springgraphql.entities.Post;
import com.phihai91.springgraphql.exceptions.NotFoundException;
import com.phihai91.springgraphql.payloads.CommentModel;
import com.phihai91.springgraphql.repositories.ICommentRepository;
import com.phihai91.springgraphql.repositories.IPostRepository;
import com.phihai91.springgraphql.securities.AppUserDetails;
import com.phihai91.springgraphql.services.ICommentService;
import com.phihai91.springgraphql.ultis.UserHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CommentService implements ICommentService {
    @Autowired
    private ICommentRepository commentRepository;
    @Autowired
    private IPostRepository postRepository;

    @Override
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
}
