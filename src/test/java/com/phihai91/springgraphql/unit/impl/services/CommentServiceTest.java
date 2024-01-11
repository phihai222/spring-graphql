package com.phihai91.springgraphql.unit.impl.services;

import com.phihai91.springgraphql.entities.*;
import com.phihai91.springgraphql.exceptions.NotFoundException;
import com.phihai91.springgraphql.payloads.CommentModel;
import com.phihai91.springgraphql.repositories.ICommentRepository;
import com.phihai91.springgraphql.repositories.IPostRepository;
import com.phihai91.springgraphql.securities.AppUserDetails;
import com.phihai91.springgraphql.services.impl.CommentService;
import com.phihai91.springgraphql.ultis.CursorUtils;
import com.phihai91.springgraphql.ultis.UserHelper;
import graphql.relay.DefaultConnectionCursor;
import graphql.relay.DefaultEdge;
import graphql.relay.Edge;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;
    @Mock
    private ICommentRepository commentRepository;
    @Mock
    private IPostRepository postRepository;
    @Mock
    private CursorUtils cursorUtils;

    private static final GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");

    private static final User currentUserData = User.builder()
            .id("507f1f77bcf86cd799439011")
            .username("phihai91")
            .email("phihai91@gmail.com")
            .twoMFA(true)
            .roles(List.of())
            .registrationDate(LocalDateTime.now())
            .userInfo(UserInfo.builder().build())
            .build();

    private static final AppUserDetails userDetails = AppUserDetails.builder()
            .id("507f1f77bcf86cd799439011")
            .username("phihai91")
            .email("phihai91@gmail.com")
            .twoMFA(true)
            .authorities(List.of(authority))
            .build();

    private static final Comment comment = Comment.builder()
            .content("Content")
            .createdDate(LocalDateTime.now())
            .userId(currentUserData.id())
            .postId(new Object().toString())
            .id(new ObjectId().toString())
            .build();

    private final Edge<Comment> edge = new DefaultEdge<>(comment, new DefaultConnectionCursor(comment.id()));
    private static MockedStatic<ReactiveSecurityContextHolder> reactiveSecurityMocked;
    private static MockedStatic<UserHelper> userHelperMocked;

    @BeforeAll
    public static void init() {
        // When
        reactiveSecurityMocked = mockStatic(ReactiveSecurityContextHolder.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(ReactiveSecurityContextHolder.getContext())
                .thenReturn(Mono.just(securityContext));

        userHelperMocked = mockStatic(UserHelper.class);
        when(UserHelper.getUserDetails(any()))
                .thenReturn(userDetails);
    }

    @AfterAll
    public static void close() {
        reactiveSecurityMocked.close();
        userHelperMocked.close();
    }

    @Test
    public void given_commentPayload_when_postNotFound_returnError() {
        // given
        CommentModel.CommentPostInput input = CommentModel.CommentPostInput.builder()
                .postId(new ObjectId().toString())
                .content("Content")
                .build();
        // when
        when(postRepository.findById(anyString()))
                .thenReturn(Mono.empty());

        // then
        var setup = commentService.createComment(input);

        StepVerifier.create(setup)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    public void given_commentPayload_when_postExisted_returnComment() {
        // given
        CommentModel.CommentPostInput input = CommentModel.CommentPostInput.builder()
                .postId(comment.postId())
                .content("Content")
                .build();
        // when
        when(postRepository.findById(anyString()))
                .thenReturn(Mono.just(Post.builder()
                        .id(comment.postId())
                        .build()));

        when(commentRepository.save(any()))
                .thenReturn(Mono.just(comment));

        // then
        var setup = commentService.createComment(input);

        Predicate<CommentModel.Comment> predicate = c ->
                c.postId().equals(input.postId());

        StepVerifier.create(setup)
                .expectNextMatches(predicate)
                .verifyComplete();
    }


}
