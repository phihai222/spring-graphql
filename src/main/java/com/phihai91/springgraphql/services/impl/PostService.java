package com.phihai91.springgraphql.services.impl;

import com.phihai91.springgraphql.entities.Post;
import com.phihai91.springgraphql.entities.Visibility;
import com.phihai91.springgraphql.repositories.IPostRepository;
import com.phihai91.springgraphql.repositories.IUserRepository;
import com.phihai91.springgraphql.securities.AppUserDetails;
import com.phihai91.springgraphql.payloads.PostModel;
import com.phihai91.springgraphql.services.IPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class PostService implements IPostService {
    @Autowired
    private IPostRepository postRepository;

    @Autowired
    private IUserRepository userRepository;

    @Override
    @PreAuthorize("hasRole('USER')")
    public Mono<PostModel.CreatePostPayload> createPost(PostModel.CreatePostInput input) {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> {
                    AppUserDetails userDetails = (AppUserDetails) securityContext.getAuthentication().getPrincipal();
                    return userRepository.findById(userDetails.getId());
                })
                .flatMap(user -> postRepository.save(Post.builder()
                        .content(input.content())
                        .userId(user.id())
                        .photoUrls(input.photoUrls().isEmpty() ? null : input.photoUrls()) //TODO Check photo null or empty string
                        .visibility(Visibility.PUBLIC)
                        .userInfo(user.userInfo())
                        .build()))
                .map(post -> PostModel.CreatePostPayload.builder()
                        .id(post.id())
                        .post(PostModel.Post.builder()
                                .firstName(post.userInfo().firstName())
                                .lastName(post.userInfo().firstName())
                                .photoUrl(post.photoUrls())
                                .content(post.content())
                                .build())
                        .build());
    }
}
