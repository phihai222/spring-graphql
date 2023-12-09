package com.phihai91.springgraphql.services.impl;

import com.phihai91.springgraphql.entities.Post;
import com.phihai91.springgraphql.entities.Visibility;
import com.phihai91.springgraphql.exceptions.ForbiddenException;
import com.phihai91.springgraphql.exceptions.NotFoundException;
import com.phihai91.springgraphql.payloads.CommonModel;
import com.phihai91.springgraphql.repositories.IPostRepository;
import com.phihai91.springgraphql.repositories.IUserRepository;
import com.phihai91.springgraphql.payloads.PostModel;
import com.phihai91.springgraphql.securities.AppUserDetails;
import com.phihai91.springgraphql.services.IPostService;
import com.phihai91.springgraphql.ultis.CursorUtils;
import com.phihai91.springgraphql.ultis.UserHelper;
import graphql.relay.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Service
public class PostService implements IPostService {
    @Autowired
    private IPostRepository postRepository;

    @Autowired
    private CursorUtils cursorUtils;

    @Autowired
    private IUserRepository userRepository;

    @Override
    @PreAuthorize("hasRole('USER')")
    public Mono<PostModel.CreatePostPayload> createPost(PostModel.CreatePostInput input) {
        return ReactiveSecurityContextHolder.getContext()
                .map(UserHelper::getUserDetails)
                .flatMap(userDetails -> userRepository.findById(userDetails.getId()))
                .flatMap(user -> {
                    Post newPost = Post.builder()
                            .content(input.content())
                            .userId(user.id())
                            .photoUrls(input.photoUrls())
                            .visibility(input.visibility() == null ? Visibility.PUBLIC : input.visibility())
                            .userInfo(user.userInfo())
                            .build();
                    return postRepository.save(newPost);
                })
                .map(Post::toCreatePostPayload);
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public Flux<PostModel.CreatePostPayload> getMyPosts() {
        return ReactiveSecurityContextHolder.getContext()
                .map(UserHelper::getUserDetails)
                .flatMap(appUserDetails -> postRepository.findAllByUserIdEquals(appUserDetails.getId()).collectList())
                .flatMapMany(Flux::fromIterable)
                .map(Post::toCreatePostPayload);
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public Flux<PostModel.CreatePostPayload> getMyPosts(Pageable pageable) {
        Mono<AppUserDetails> appUserDetailsMono = ReactiveSecurityContextHolder.getContext()
                .map(UserHelper::getUserDetails);

        Flux<Post> posts = appUserDetailsMono
                .flatMapMany(u -> postRepository.findAllByUserId(u.getId(), pageable));

        return posts.map(Post::toCreatePostPayload);
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public Mono<Connection<PostModel.Post>> getMyPosts(int first, String cursor) {
        Mono<AppUserDetails> appUserDetailsMono = ReactiveSecurityContextHolder.getContext()
                .map(UserHelper::getUserDetails);

        return appUserDetailsMono
                .flatMap(appUserDetails ->
                        getPost(appUserDetails.getId(), first, cursor)
                                .map(post -> (Edge<PostModel.Post>) new DefaultEdge<>(post, cursorUtils.from(post.id())))
                                .collect(Collectors.toUnmodifiableList()))
                .map(edges -> {
                    DefaultPageInfo pageInfo = new DefaultPageInfo(
                            cursorUtils.getFirstCursorFrom(edges),
                            cursorUtils.getLastCursorFrom(edges),
                            cursor != null,
                            edges.size() >= first);

                    return new DefaultConnection<>(edges, pageInfo);
                });
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public Mono<CommonModel.CommonPayload> deletePost(String input) {
        Mono<AppUserDetails> appUserDetailsMono = ReactiveSecurityContextHolder.getContext()
                .map(UserHelper::getUserDetails);

        Mono<Post> post = postRepository.findById(input)
                .switchIfEmpty(Mono.error(new NotFoundException("Post not found")));

        Mono<Boolean> checkValidUserId = appUserDetailsMono.zipWith(post, (u, p) -> u.getId().equals(p.userId()));

        return checkValidUserId
                .flatMap(aBoolean -> aBoolean ? postRepository.deleteById(input) :
                        Mono.error(new ForbiddenException("Invalid permission")))
                .then(Mono.just(CommonModel.CommonPayload.builder()
                        .message("Post deleted")
                        .status(CommonModel.CommonStatus.SUCCESS)
                        .build()));
    }

    private Flux<PostModel.Post> getPost(String userId, int first, String cursor) {
        return cursor == null ? postRepository.findAllByUserIdStart(userId, first).map(Post::toPostPayload)
                : postRepository.findAllByUserIdBefore(userId, cursor, first).map(Post::toPostPayload);
    }
}
