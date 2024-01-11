package com.phihai91.springgraphql.unit.impl.services;

import com.phihai91.springgraphql.entities.*;
import com.phihai91.springgraphql.exceptions.BadRequestException;
import com.phihai91.springgraphql.exceptions.NotFoundException;
import com.phihai91.springgraphql.payloads.CommonModel;
import com.phihai91.springgraphql.payloads.FriendModel;
import com.phihai91.springgraphql.repositories.IFriendRepository;
import com.phihai91.springgraphql.repositories.IFriendRequestRepository;
import com.phihai91.springgraphql.repositories.IUserRepository;
import com.phihai91.springgraphql.securities.AppUserDetails;
import com.phihai91.springgraphql.services.impl.FriendService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FriendServiceTest {

    @InjectMocks
    private FriendService friendService;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IFriendRequestRepository friendRequestRepository;

    @Mock
    private IFriendRepository friendRepository;

    @Mock
    private CursorUtils cursorUtils;

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

    private static final AppUserDetails userDetails = AppUserDetails.builder()
            .id("507f1f77bcf86cd799439011")
            .username("phihai91")
            .email("phihai91@gmail.com")
            .twoMFA(true)
            .authorities(List.of(authority))
            .build();

    private final Friend friendData = Friend.builder()
            .id(currentUserData.id())
            .friends(List.of(FriendData.builder()
                    .id(new ObjectId().toString())
                    .userId(new ObjectId().toString())
                    .build()))
            .build();

    private final FriendRequest friendRequest = FriendRequest.builder()
            .id(new ObjectId().toString())
            .isIgnore(false)
            .message("I want to add you")
            .fromUser(new ObjectId().toString())
            .toUser(currentUserData.id())
            .build();

    private final Edge<FriendRequest> edge = new DefaultEdge<>(friendRequest, new DefaultConnectionCursor(friendRequest.id()));
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
    public void give_requestInput_when_userNotFound_then_returnError() {
        // given
        FriendModel.AddFriendInput input = FriendModel.AddFriendInput.builder()
                .userId(new ObjectId().toString())
                .message("Fake message")
                .build();

        // when
        when(userRepository.findById(anyString()))
                .thenReturn(Mono.empty());

        when(friendRepository.findById(anyString()))
                .thenReturn(Mono.empty());

        // then
        var setup = friendService.sendRequest(input);

        StepVerifier.create(setup)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    public void give_requestInput_when_alreadyFriend_then_returnError() {
        // given
        FriendModel.AddFriendInput input = FriendModel.AddFriendInput.builder()
                .userId(new ObjectId().toString())
                .message("Fake message")
                .build();

        // when
        when(userRepository.findById(anyString()))
                .thenReturn(Mono.just(currentUserData.withId(input.userId())));

        when(friendRepository.findById(anyString()))
                .thenReturn(Mono.just(friendData.withFriends(List.of(
                        FriendData.builder()
                                .userId(input.userId())
                                .build()
                ))));

        // then
        var setup = friendService.sendRequest(input);

        StepVerifier.create(setup)
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    public void give_requestInput_when_requestYourself_then_returnError() {
        // given
        FriendModel.AddFriendInput input = FriendModel.AddFriendInput.builder()
                .userId(new ObjectId().toString())
                .message("Fake message")
                .build();

        // when
        when(userRepository.findById(anyString()))
                .thenReturn(Mono.just(currentUserData.withId(input.userId())));

        // then
        var setup = friendService.sendRequest(input.withUserId(currentUserData.id()));

        StepVerifier.create(setup)
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    public void give_requestInput_when_requestExisted_then_withDrawRequest() {
        // given
        FriendModel.AddFriendInput input = FriendModel.AddFriendInput.builder()
                .userId(new ObjectId().toString())
                .message("Fake message")
                .build();

        FriendRequest request = FriendRequest.builder()
                .id(new ObjectId().toString())
                .build();


        // when
        when(userRepository.findById(anyString()))
                .thenReturn(Mono.just(currentUserData.withId(input.userId())));

        when(friendRepository.findById(anyString()))
                .thenReturn(Mono.just(friendData));

        when(friendRequestRepository.findFirstByFromUserEqualsAndToUserEquals(currentUserData.id(), input.userId()))
                .thenReturn(Mono.just(request));

        when(friendRequestRepository.findFirstByFromUserEqualsAndToUserEquals(input.userId(), currentUserData.id()))
                .thenReturn(Mono.just(request));

        when(friendRequestRepository.save(any()))
                .thenReturn(Mono.empty());

        when(friendRequestRepository.deleteById(anyString()))
                .thenReturn(Mono.empty());

        // then
        var setup = friendService.sendRequest(input);

        Predicate<CommonModel.CommonPayload> predicate = res ->
                res.status().equals(CommonModel.CommonStatus.SUCCESS) &&
                        res.message().equals("Withdrew");

        StepVerifier.create(setup)
                .expectNextMatches(predicate)
                .verifyComplete();
    }

    @Test
    public void give_requestInput_when_createNewRequest_then_withdrawRequest() {
        // given
        FriendModel.AddFriendInput input = FriendModel.AddFriendInput.builder()
                .userId(new ObjectId().toString())
                .message("Fake message")
                .build();

        FriendRequest request = FriendRequest.builder()
                .id(new ObjectId().toString())
                .build();

        // when
        when(userRepository.findById(anyString()))
                .thenReturn(Mono.just(currentUserData.withId(input.userId())));

        when(friendRepository.findById(anyString()))
                .thenReturn(Mono.just(friendData));

        when(friendRequestRepository.findFirstByFromUserEqualsAndToUserEquals(currentUserData.id(), input.userId()))
                .thenReturn(Mono.empty());

        when(friendRequestRepository.findFirstByFromUserEqualsAndToUserEquals(input.userId(), currentUserData.id()))
                .thenReturn(Mono.empty());

        when(friendRequestRepository.save(any()))
                .thenReturn(Mono.just(request));

        // then
        var setup = friendService.sendRequest(input);

        Predicate<CommonModel.CommonPayload> predicate = res ->
                res.status().equals(CommonModel.CommonStatus.SUCCESS) &&
                        res.message().equals("Request send");

        StepVerifier.create(setup)
                .expectNextMatches(predicate)
                .verifyComplete();
    }

    @Test
    public void give_requestInput_when_requestFromFriendExisted_then_autoAccept() {
        // given
        FriendModel.AddFriendInput input = FriendModel.AddFriendInput.builder()
                .userId(new ObjectId().toString())
                .message("Fake message")
                .build();

        FriendRequest request = FriendRequest.builder()
                .id(new ObjectId().toString())
                .fromUser(input.userId())
                .toUser(currentUserData.id())
                .build();

        // when
        when(userRepository.findById(anyString()))
                .thenReturn(Mono.just(currentUserData.withId(input.userId())));

        when(friendRequestRepository.findFirstByFromUserEqualsAndToUserEquals(currentUserData.id(), input.userId()))
                .thenReturn(Mono.empty());

        when(friendRequestRepository.findFirstByFromUserEqualsAndToUserEquals(input.userId(), currentUserData.id()))
                .thenReturn(Mono.just(request));

        when(friendRepository.findById(currentUserData.id()))
                .thenReturn(Mono.empty());

        when(friendRepository.findById(input.userId()))
                .thenReturn(Mono.empty());

        when(friendRequestRepository.save(any()))
                .thenReturn(Mono.just(FriendRequest.builder().build()));

        when(friendRequestRepository.deleteById(anyString()))
                .thenReturn(Mono.empty());

        when(friendRepository.save(any()))
                .thenReturn(Mono.just(friendData));

        // then
        var setup = friendService.sendRequest(input);

        Predicate<CommonModel.CommonPayload> predicate = res ->
                res.status().equals(CommonModel.CommonStatus.SUCCESS) &&
                        res.message().equals("Accepted friend");

        StepVerifier.create(setup)
                .expectNextMatches(predicate)
                .verifyComplete();
    }

    @Test
    public void given_userId_when_friendRequestNotExisted_then_returnError() {
        // when
        when(friendRequestRepository.findFirstByFromUserEqualsAndToUserEquals(anyString(), anyString()))
                .thenReturn(Mono.empty());

        // then
        var setup = friendService.ignoreFriendRequest(new ObjectId().toString());

        StepVerifier.create(setup)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    public void given_userId_when_friendRequestExisted_then_updateData() {
        // when
        when(friendRequestRepository.findFirstByFromUserEqualsAndToUserEquals(anyString(), anyString()))
                .thenReturn(Mono.just(FriendRequest.builder()
                        .isIgnore(false)
                        .build()));

        when(friendRequestRepository.save(any()))
                .thenReturn(Mono.just(FriendRequest.builder()
                        .isIgnore(true)
                        .build()));

        // then
        var setup = friendService.ignoreFriendRequest(new ObjectId().toString());

        Predicate<CommonModel.CommonPayload> predicate = c ->
                c.status().equals(CommonModel.CommonStatus.SUCCESS);

        StepVerifier.create(setup)
                .expectNextMatches(predicate)
                .verifyComplete();
    }

    @Test
    public void given_limit_when_haveOneRequest_then_returnPage() {
        // when
        when(friendRequestRepository.findAllByUserIdStart(anyString(), anyInt()))
                .thenReturn(Flux.just(friendRequest));

        when(cursorUtils.from(anyString()))
                .thenReturn(edge.getCursor());

        when(cursorUtils.getFirstCursorFrom(any()))
                .thenReturn(edge.getCursor());

        when(cursorUtils.getLastCursorFrom(any()))
                .thenReturn(edge.getCursor());

        // then
        var setup = friendService.getFriendRequest(1, null);

        Predicate<Connection<FriendModel.FriendRequest>> predicate = fr ->
                fr.getEdges().get(0).getNode().toUser().equals(currentUserData.id())
                && fr.getEdges().size() == 1;

        StepVerifier.create(setup)
                .expectNextMatches(predicate)
                .verifyComplete();
    }
}
