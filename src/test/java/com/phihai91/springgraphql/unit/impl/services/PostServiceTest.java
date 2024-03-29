package com.phihai91.springgraphql.unit.impl.services;

import com.phihai91.springgraphql.entities.*;
import com.phihai91.springgraphql.exceptions.ForbiddenException;
import com.phihai91.springgraphql.exceptions.NotFoundException;
import com.phihai91.springgraphql.payloads.CommonModel;
import com.phihai91.springgraphql.payloads.PostModel;
import com.phihai91.springgraphql.repositories.IPostRepository;
import com.phihai91.springgraphql.repositories.IUserRepository;
import com.phihai91.springgraphql.securities.AppUserDetails;
import com.phihai91.springgraphql.services.IFriendService;
import com.phihai91.springgraphql.services.impl.PostService;
import com.phihai91.springgraphql.ultis.CursorUtils;
import com.phihai91.springgraphql.ultis.UserHelper;
import graphql.relay.Connection;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class PostServiceTest {
    @InjectMocks
    private PostService postService;

    @Mock
    private IPostRepository postRepository;

    @Mock
    private CursorUtils cursorUtils;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IFriendService friendService;

    private static final GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");

    private final User currentUserData = User.builder()
            .id("507f1f77bcf86cd799439011")
            .username("phihai91")
            .email("phihai91@gmail.com")
            .twoMFA(true)
            .roles(List.of())
            .registrationDate(LocalDateTime.now())
            .userInfo(UserInfo.builder().build())
            .build();

    private final User friendData = User.builder()
            .id("507f1f77bcf86cd799439022")
            .username("friendUsername")
            .email("friendUsername@gmail.com")
            .twoMFA(false)
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

    private static final Post postData = Post.builder()
            .id(new ObjectId().toString())
            .userId("507f1f77bcf86cd799439011")
            .visibility(Visibility.PUBLIC)
            .content("Content")
            .comments(List.of())
            .userInfo(UserInfo.builder().build())
            .createdDate(LocalDateTime.now())
            .build();

    private final Edge<Post> edge = new DefaultEdge<>(postData, new DefaultConnectionCursor(postData.id()));

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
    public void give_postData_when_dataCreated_then_returnNewData() {
        // given
        var input = PostModel.CreatePostInput.builder()
                .content(postData.content())
                .visibility(Visibility.PUBLIC)
                .build();

        // when
        when(userRepository.findById(anyString()))
                .thenReturn(Mono.just(currentUserData));

        when(postRepository.save(any()))
                .thenReturn(Mono.just(postData));

        // then
        var setup = postService.createPost(input);

        Predicate<PostModel.CreatePostPayload> predicate = p ->
                p.post().content().equals(input.content()) && p.post().userId().equals(currentUserData.id());

        StepVerifier.create(setup)
                .expectNextMatches(predicate)
                .verifyComplete();
    }

    @Test
    public void give_currentLoginUser_when_postExisted_then_return() {
        // when
        when(postRepository.findAllByUserIdEquals(anyString()))
                .thenReturn(Flux.just(postData));

        // then
        var setup = postService.getPostsByUser();

        Predicate<PostModel.CreatePostPayload> predicate = p ->
                p.post().userId().equals(currentUserData.id());

        StepVerifier.create(setup)
                .expectNextMatches(predicate)
                .verifyComplete();
    }

    @Test
    public void give_currentUserIdAndPageAble_when_postExisted_returnPost() {
        // given
        PageRequest pageable = PageRequest.of(
                1,
                1,
                Sort.by(Sort.Direction.DESC, "createdDate"));

        // when
        when(postRepository.findAllByUserId(anyString(), any()))
                .thenReturn(Flux.just(postData));

        // then
        var setup = postService.getPostsByUser(pageable);

        Predicate<PostModel.CreatePostPayload> predicate = p ->
                p.post().userId().equals(currentUserData.id());

        StepVerifier.create(setup)
                .expectNextMatches(predicate)
                .verifyComplete();
    }

    @Test
    public void given_nullUserId_when_logged_then_ReturnData() {
        // when
        when(postRepository.findAllByUserIdStart(anyString(), anyInt()))
                .thenReturn(Flux.just(postData));

        when(cursorUtils.from(anyString()))
                .thenReturn(edge.getCursor());

        when(cursorUtils.getFirstCursorFrom(any()))
                .thenReturn(edge.getCursor());

        when(cursorUtils.getLastCursorFrom(any()))
                .thenReturn(edge.getCursor());

        // then
        var setup = postService.getPostsByUser(null, 1, null);

        Predicate<Connection<PostModel.Post>> predicate = c ->
                c.getPageInfo().isHasNextPage() &&
                        c.getEdges().size() == 1;

        StepVerifier.create(setup)
                .expectNextMatches(predicate)
                .verifyComplete();
    }

    @Test
    public void given_nullUserId_when_logged_then_ReturnDataWithCursor() {
        // when
        when(postRepository.findAllByUserIdBefore(anyString(), anyString(), anyInt()))
                .thenReturn(Flux.just(postData));

        when(cursorUtils.from(anyString()))
                .thenReturn(edge.getCursor());

        when(cursorUtils.getFirstCursorFrom(any()))
                .thenReturn(edge.getCursor());

        when(cursorUtils.getLastCursorFrom(any()))
                .thenReturn(edge.getCursor());

        // then
        var setup = postService.getPostsByUser(null, 1, postData.id());

        Predicate<Connection<PostModel.Post>> predicate = c ->
                c.getPageInfo().isHasNextPage() &&
                        c.getEdges().size() == 1;

        StepVerifier.create(setup)
                .expectNextMatches(predicate)
                .verifyComplete();
    }

    @Test
    public void given_friendId_when_isNotfound_then_ReturnError() {
        // given
        when(userRepository.findByUsernameEqualsOrEmailEquals(anyString(), anyString()))
                .thenReturn(Mono.empty());

        // when
        var setup = postService.getPostsByUser(friendData.id(), 1, null);

        StepVerifier.create(setup)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    public void given_userId_when_isNotFriend_then_ReturnDataWithCursor() {
        // when
        when(userRepository.findByUsernameEqualsOrEmailEquals(anyString(), anyString()))
                .thenReturn(Mono.just(friendData));

        when(friendService.checkIsAlreadyFriend(anyString(), anyString()))
                .thenReturn(Mono.just(false));

        when(postRepository.findAllByUserIdStartWithVisibility(anyString(), anyList(), anyInt()))
                .thenReturn(Flux.just(postData));

        when(cursorUtils.from(anyString()))
                .thenReturn(edge.getCursor());

        when(cursorUtils.getFirstCursorFrom(any()))
                .thenReturn(edge.getCursor());

        when(cursorUtils.getLastCursorFrom(any()))
                .thenReturn(edge.getCursor());

        // then
        var setup = postService.getPostsByUser(friendData.id(), 1, null);

        Predicate<Connection<PostModel.Post>> predicate = c ->
                c.getPageInfo().isHasNextPage() &&
                        c.getEdges().size() == 1 &&
                        c.getEdges().get(0).getNode().visibility().equals(Visibility.PUBLIC);

        StepVerifier.create(setup)
                .expectNextMatches(predicate)
                .verifyComplete();
    }

    @Test
    public void given_userId_when_isFriend_then_ReturnDataStartWithCursor() {
        // when
        when(userRepository.findByUsernameEqualsOrEmailEquals(anyString(), anyString()))
                .thenReturn(Mono.just(friendData));

        when(friendService.checkIsAlreadyFriend(anyString(), anyString()))
                .thenReturn(Mono.just(true));

        when(postRepository.findAllByUserIdStartWithVisibility(anyString(), anyList(), anyInt()))
                .thenReturn(Flux.just(postData.withVisibility(Visibility.FRIEND_ONLY)));

        when(cursorUtils.from(anyString()))
                .thenReturn(edge.getCursor());

        when(cursorUtils.getFirstCursorFrom(any()))
                .thenReturn(edge.getCursor());

        when(cursorUtils.getLastCursorFrom(any()))
                .thenReturn(edge.getCursor());

        // then
        var setup = postService.getPostsByUser(friendData.id(), 1, null);

        Predicate<Connection<PostModel.Post>> predicate = c ->
                c.getPageInfo().isHasNextPage() &&
                        c.getEdges().size() == 1 && c.getEdges().size() == 1 &&
                        c.getEdges().get(0).getNode().visibility().equals(Visibility.FRIEND_ONLY);

        StepVerifier.create(setup)
                .expectNextMatches(predicate)
                .verifyComplete();
    }

    @Test
    public void given_userId_when_isFriend_then_ReturnDataBeforeWithCursor() {
        // when
        when(userRepository.findByUsernameEqualsOrEmailEquals(anyString(), anyString()))
                .thenReturn(Mono.just(friendData));

        when(friendService.checkIsAlreadyFriend(anyString(), anyString()))
                .thenReturn(Mono.just(true));

        when(postRepository.findAllByUserIdBeforeWithVisibility(anyString(), anyList(), anyString(), anyInt()))
                .thenReturn(Flux.just(postData.withVisibility(Visibility.FRIEND_ONLY)));

        // mock cursor
        when(cursorUtils.from(anyString()))
                .thenReturn(edge.getCursor());

        when(cursorUtils.getFirstCursorFrom(any()))
                .thenReturn(edge.getCursor());

        when(cursorUtils.getLastCursorFrom(any()))
                .thenReturn(edge.getCursor());

        // then
        var setup = postService.getPostsByUser(friendData.id(), 1, postData.id());

        Predicate<Connection<PostModel.Post>> predicate = c ->
                c.getPageInfo().isHasNextPage() &&
                        c.getEdges().size() == 1 && c.getEdges().size() == 1 &&
                        c.getEdges().get(0).getNode().visibility().equals(Visibility.FRIEND_ONLY);

        StepVerifier.create(setup)
                .expectNextMatches(predicate)
                .verifyComplete();
    }

    @Test
    public void given_currentUser_then_friendListEmpty_return_timelinePostStart() {
        // given
        Friend friendData = Friend.builder()
                .id(currentUserData.id())
                .friends(List.of())
                .build();

        // when
        when(friendService.getFriendList(anyString()))
                .thenReturn(Mono.just(friendData));

        when(postRepository.findAllByUserIdsStartWithVisibility(any(), any(), anyInt()))
                .thenReturn(Flux.just(postData.withVisibility(Visibility.PRIVATE)));

        // mock cursor
        when(cursorUtils.from(anyString()))
                .thenReturn(edge.getCursor());

        when(cursorUtils.getFirstCursorFrom(any()))
                .thenReturn(edge.getCursor());

        when(cursorUtils.getLastCursorFrom(any()))
                .thenReturn(edge.getCursor());

        // then
        var setup = postService.getPostTimeline(1, null);

        Predicate<Connection<PostModel.Post>> predicate = c ->
                c.getPageInfo().isHasNextPage() &&
                        c.getEdges().size() == 1 && c.getEdges().size() == 1 &&
                        c.getEdges().get(0).getNode().visibility().equals(Visibility.PRIVATE);

        StepVerifier.create(setup)
                .expectNextMatches(predicate)
                .verifyComplete();
    }

    @Test
    public void given_currentUser_then_friendListEmpty_return_timelinePostBefor() {
        // given
        Friend friendData = Friend.builder()
                .id(currentUserData.id())
                .friends(List.of())
                .build();

        // when
        when(friendService.getFriendList(anyString()))
                .thenReturn(Mono.just(friendData));

        when(postRepository.findAllByUserIdsBeforeWithVisibility(any(), any(), anyInt(), anyString()))
                .thenReturn(Flux.just(postData.withVisibility(Visibility.PRIVATE)));

        // mock cursor
        when(cursorUtils.from(anyString()))
                .thenReturn(edge.getCursor());

        when(cursorUtils.getFirstCursorFrom(any()))
                .thenReturn(edge.getCursor());

        when(cursorUtils.getLastCursorFrom(any()))
                .thenReturn(edge.getCursor());

        // then
        var setup = postService.getPostTimeline(1, postData.id());

        Predicate<Connection<PostModel.Post>> predicate = c ->
                c.getPageInfo().isHasNextPage() &&
                        c.getEdges().size() == 1 && c.getEdges().size() == 1 &&
                        c.getEdges().get(0).getNode().visibility().equals(Visibility.PRIVATE);

        StepVerifier.create(setup)
                .expectNextMatches(predicate)
                .verifyComplete();
    }

    @Test
    public void given_postId_when_notFoundForDelete_returnError() {
        // given
        String input = new ObjectId().toString();

        // when
        when(postRepository.findById(anyString()))
                .thenReturn(Mono.empty());

        // then
        var setup = postService.deletePost(input);

        StepVerifier.create(setup)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    public void given_postId_when_dontHavePermission_returnError() {
        // given
        String input = new ObjectId().toString();

        // when
        when(postRepository.findById(anyString()))
                .thenReturn(Mono.just(postData.withUserId(input)));

        // then
        var setup = postService.deletePost(input);

        StepVerifier.create(setup)
                .expectError(ForbiddenException.class)
                .verify();
    }
    @Test
    public void given_postId_when_ok_returnSuccess() {
        // when
        when(postRepository.findById(anyString()))
                .thenReturn(Mono.just(postData));

        when(postRepository.deleteById(anyString()))
                .thenReturn(Mono.empty());

        // then
        var setup = postService.deletePost(postData.id());

        Predicate<CommonModel.CommonPayload> predicate = c ->
                c.status().equals(CommonModel.CommonStatus.SUCCESS);

        StepVerifier.create(setup)
                .expectNextMatches(predicate)
                .verifyComplete();
    }

}
