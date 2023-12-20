package com.phihai91.springgraphql.services.impl;

import com.phihai91.springgraphql.entities.Post;
import com.phihai91.springgraphql.entities.User;
import com.phihai91.springgraphql.entities.Visibility;
import com.phihai91.springgraphql.exceptions.ForbiddenException;
import com.phihai91.springgraphql.exceptions.NotFoundException;
import com.phihai91.springgraphql.payloads.CommonModel;
import com.phihai91.springgraphql.repositories.IPostRepository;
import com.phihai91.springgraphql.repositories.IUserRepository;
import com.phihai91.springgraphql.payloads.PostModel;
import com.phihai91.springgraphql.securities.AppUserDetails;
import com.phihai91.springgraphql.services.IFriendService;
import com.phihai91.springgraphql.services.IPostService;
import com.phihai91.springgraphql.ultis.CursorUtils;
import com.phihai91.springgraphql.ultis.UserHelper;
import graphql.relay.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PostService implements IPostService {
    @Autowired
    private IPostRepository postRepository;

    @Autowired
    private CursorUtils cursorUtils;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IFriendService friendService;

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
    public Flux<PostModel.CreatePostPayload> getPostsByUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(UserHelper::getUserDetails)
                .flatMap(appUserDetails -> postRepository.findAllByUserIdEquals(appUserDetails.getId()).collectList())
                .flatMapMany(Flux::fromIterable)
                .map(Post::toCreatePostPayload);
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public Flux<PostModel.CreatePostPayload> getPostsByUser(Pageable pageable) {
        Mono<AppUserDetails> appUserDetailsMono = ReactiveSecurityContextHolder.getContext()
                .map(UserHelper::getUserDetails);

        Flux<Post> posts = appUserDetailsMono
                .flatMapMany(u -> postRepository.findAllByUserId(u.getId(), pageable));

        return posts.map(Post::toCreatePostPayload);
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public Mono<Connection<PostModel.Post>> getPostsByUser(String username, int first, String cursor) {
        Mono<AppUserDetails> currentUser = ReactiveSecurityContextHolder.getContext()
                .map(UserHelper::getUserDetails);

        // If user is null, return the current user.
        Mono<AppUserDetails> targetUser = username == null ? currentUser : userRepository.findByUsernameEqualsOrEmailEquals(username, username)
                .map(User::toAppUserDetails)
                // If user not null, check user existed or not.
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")));

        Flux<PostModel.Post> posts = currentUser.zipWith(targetUser, (u1, u2) ->
                        // Detect user try to get themselves data or get from other user
                        u1.getId().equals(u2.getId()) ? getPost(u1.getId(), first, cursor)
                                : getPostWithVisibility(u1.getId(), u2.getId(), first, cursor))
                // convert Mono<Flux<T> to Flux<T>
                .flatMapMany(postFlux -> postFlux);

        return posts
                .map(post -> (Edge<PostModel.Post>) new DefaultEdge<>(post, cursorUtils.from(post.id())))
                .collect(Collectors.toUnmodifiableList())
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
        log.info("Get posts by current user");
        return cursor == null ? postRepository.findAllByUserIdStart(userId, first).map(Post::toPostPayload)
                : postRepository.findAllByUserIdBefore(userId, cursor, first).map(Post::toPostPayload);
    }

    private Flux<PostModel.Post> getPostWithVisibility(String userId, String friendId, int first, String cursor) {
        log.info("Current user id:: " + userId);
        log.info("Get post of user:: " + friendId);
        // Check both user are already friend or not
        return friendService.checkIsAlreadyFriend(userId, friendId)
                .flatMapMany(isFriend -> cursor == null ?
                        // If not define cursor, get from index 0
                        postRepository.findAllByUserIdStartWithVisibility(
                                friendId,
                                // If already friend, get post share with public and friend. If not, public only
                                isFriend ? List.of(Visibility.FRIEND_ONLY, Visibility.PUBLIC) : List.of(Visibility.PUBLIC),
                                first)
                        : postRepository.findAllByUserIdBeforeWithVisibility(
                        friendId,
                        // If already friend, get post share with public and friend. If not, public only
                        isFriend ? List.of(Visibility.FRIEND_ONLY, Visibility.PUBLIC) : List.of(Visibility.PUBLIC),
                        cursor, first))
                .map(Post::toPostPayload);
    }
}
