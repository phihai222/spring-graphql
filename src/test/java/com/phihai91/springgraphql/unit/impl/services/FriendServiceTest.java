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
                    .userId(new ObjectId().toString())
                    .build()))
            .build();

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

        // when
        when(userRepository.findById(anyString()))
                .thenReturn(Mono.just(currentUserData.withId(input.userId())));

        when(friendRepository.findById(anyString()))
                .thenReturn(Mono.just(friendData));

        // TODO MOCK Failed by something
        when(friendRequestRepository.findFirstByFromUserEqualsAndToUserEquals(anyString(), anyString()))
                .thenReturn(Mono.just(FriendRequest.builder().build()));

        when(friendRequestRepository.deleteById(anyString()))
                .thenReturn(Mono.empty());

        // then
        var setup = friendService.sendRequest(input);

        Predicate<CommonModel.CommonPayload> predicate = res ->
                res.status().equals(CommonModel.CommonStatus.SUCCESS);

        StepVerifier.create(setup)
                .expectNextMatches(predicate)
                .verifyComplete();
    }
}