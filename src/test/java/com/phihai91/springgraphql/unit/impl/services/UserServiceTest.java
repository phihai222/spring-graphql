package com.phihai91.springgraphql.unit.impl.services;

import com.phihai91.springgraphql.entities.User;
import com.phihai91.springgraphql.entities.UserInfo;
import com.phihai91.springgraphql.exceptions.BadRequestException;
import com.phihai91.springgraphql.exceptions.ForbiddenException;
import com.phihai91.springgraphql.exceptions.NotFoundException;
import com.phihai91.springgraphql.payloads.CommonModel;
import com.phihai91.springgraphql.payloads.UserModel;
import com.phihai91.springgraphql.repositories.IUserRepository;
import com.phihai91.springgraphql.securities.AppUserDetails;
import com.phihai91.springgraphql.services.IAuthService;
import com.phihai91.springgraphql.services.IRedisService;
import com.phihai91.springgraphql.services.impl.UserService;
import com.phihai91.springgraphql.ultis.UserHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IAuthService authService;

    @Mock
    private IRedisService redisService;

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

    @BeforeAll
    public static void init() {
        // When
        mockStatic(ReactiveSecurityContextHolder.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(ReactiveSecurityContextHolder.getContext())
                .thenReturn(Mono.just(securityContext));

        mockStatic(UserHelper.class);
        when(UserHelper.getUserDetails(any()))
                .thenReturn(userDetails);
    }

    @Test
    public void given_currentUser_when_accountExisted_then_returnUserInfo() {
        // When
        when(this.userRepository.findById(anyString()))
                .thenReturn(Mono.just(currentUserData));

        var setup = userService.getCurrentUserInfo();

        // Then
        Predicate<UserModel.User> predicate = u -> u.id().equals(currentUserData.id());

        StepVerifier.create(setup)
                .expectNextMatches(predicate)
                .verifyComplete();
    }

    @Test
    public void given_changeEmailWith2MFAEnable_when_setMFAWithOutEmail_then_error() {
        // Given
        UserModel.UpdateUserInput input = UserModel.UpdateUserInput.builder()
                .username(currentUserData.username())
                .email("newEmail@gmail.com")
                .build();

        when(userRepository.findById(anyString()))
                .thenReturn(Mono.just(currentUserData));

        var setup = userService.updateUserInfo(input);

        // Then
        StepVerifier.create(setup)
                .expectError(ForbiddenException.class)
                .verify();
    }

    @Test
    public void given_changeEmailWith2MFADisable_when_invalidEmail_then_error() {
        // Given
        UserModel.UpdateUserInput input = UserModel.UpdateUserInput.builder()
                .username(currentUserData.username())
                .email("newEmail@gmail.com")
                .build();

        // When
        when(userRepository.findById(anyString()))
                .thenReturn(Mono.just(currentUserData.withTwoMFA(false)));

        when(userRepository.existsUserByEmailEquals(input.email()))
                .thenReturn(Mono.just(true));

        var setup = userService.updateUserInfo(input);

        // Then
        StepVerifier.create(setup)
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    public void given_changeUserWith2MFADisable_when_invalidEmail_then_error() {
        // Given
        UserModel.UpdateUserInput input = UserModel.UpdateUserInput.builder()
                .username("newUser")
                .email(currentUserData.email())
                .build();

        // When
        when(userRepository.findById(anyString()))
                .thenReturn(Mono.just(currentUserData.withTwoMFA(false)));

        when(userRepository.existsUserByEmailEquals(input.email()))
                .thenReturn(Mono.just(false));

        when(userRepository.existsUserByUsernameEquals(input.username()))
                .thenReturn(Mono.just(true));

        // Then
        var setup = userService.updateUserInfo(input);

        StepVerifier.create(setup)
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    public void given_changeUserWith2MFADisable_when_valid_then_returnNewData() {
        // Given
        UserModel.UpdateUserInput input = UserModel.UpdateUserInput.builder()
                .username("newUser")
                .email("newEmail")
                .build();

        // When
        when(userRepository.findById(anyString()))
                .thenReturn(Mono.just(currentUserData.withTwoMFA(false)));

        when(userRepository.existsUserByEmailEquals(input.email()))
                .thenReturn(Mono.just(false));

        when(userRepository.existsUserByUsernameEquals(input.username()))
                .thenReturn(Mono.just(false));

        when(userRepository.save(any()))
                .thenReturn(Mono.just(currentUserData
                        .withUsername(input.email())
                        .withEmail(input.email())));

        // Then
        var setup = userService.updateUserInfo(input);

        Predicate<UserModel.User> predicate = u -> u.username().equals(input.email()) &&
                u.email().equals(input.email());


        StepVerifier.create(setup)
                .expectNextMatches(predicate)
                .verifyComplete();
    }

    @Test
    public void given_activeOTP_when_emailNotSet_returnError() {
        // when
        when(userRepository.findById(anyString()))
                .thenReturn(Mono.just(currentUserData.withEmail(null)));

        // then
        var setup = userService.setTwoMFA();

        StepVerifier.create(setup)
                .expectError(ForbiddenException.class)
                .verify();
    }

    @Test
    public void give_activeOTP_when_emailValid_returnSuccess() {
        // when
        when(userRepository.findById(anyString()))
                .thenReturn(Mono.just(currentUserData));

        when(authService.getOtp())
                .thenReturn("000000");

        when(redisService.saveOtp(anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(true));

        // then
        var setup = userService.setTwoMFA();

        Predicate<UserModel.SetTwoMFAPayload> predicate = res ->
                res.otp().equals("000000") &&
                        res.userId().equals(currentUserData.id())
                        && res.sentTo().equals(currentUserData.email());

        StepVerifier.create(setup)
                .expectNextMatches(predicate)
                .verifyComplete();
    }

    @Test
    public void given_otp_when_invalid_returnError() {
        // when
        when(redisService.verifyOtp(anyString(), anyString()))
                .thenReturn(Mono.just(false));

        // then
        var setup = userService.verifyTwoMFOtp("000000");

        StepVerifier.create(setup)
                .expectError(BadRequestException.class)
                .verify();

    }

    @Test
    public void given_otp_when_valid_returnResult() {
        // when
        when(redisService.verifyOtp(anyString(), anyString()))
                .thenReturn(Mono.just(true));

        when(userRepository.findById(anyString()))
                .thenReturn(Mono.just(currentUserData));

        when(redisService.removeOTP(anyString()))
                .thenReturn(Mono.just(true));
        when(userRepository.save(any()))
                .thenReturn(Mono.just(currentUserData));

        // then
        var setup = userService.verifyTwoMFOtp("000000");

        Predicate<CommonModel.CommonPayload> predicate = res ->
                res.message().equals("Success");

        StepVerifier.create(setup)
                .expectNextMatches(predicate)
                .verifyComplete();
    }

    @Test
    public void give_username_when_notfound_returnError() {
        // given
        String username = "NotFoundUsername";

        // when
        when(userRepository.findByUsernameEqualsOrEmailEquals(anyString(), anyString()))
                .thenReturn(Mono.empty());

        // then
        var setup = userService.getUserByUsername(username);

        StepVerifier.create(setup)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    public void give_username_when_found_returnUserData() {
        // when
        when(userRepository.findByUsernameEqualsOrEmailEquals(anyString(), anyString()))
                .thenReturn(Mono.just(currentUserData));

        // then
        var setup = userService.getUserByUsername(currentUserData.username());

        Predicate<UserModel.User> predicate = u ->
                u.id().equals(userDetails.getId()) && u.username().equals(userDetails.getUsername());

        StepVerifier.create(setup)
                .expectNextMatches(predicate)
                .verifyComplete();
    }

    @Test
    public void give_userIdList_then_emptyData_returnEmptyList() {
        // give
        List<String> ids = List.of("notFoundId");

        // when
        when(userRepository.findAllByIdInAndOrderById(any()))
                .thenReturn(Flux.empty());

        // then
        var setup = userService.getAllUserByIds(ids);

        StepVerifier.create(setup)
                .verifyComplete();
    }

    @Test
    public void give_userIdList_then_oneMatch_returnList() {
        // give
        List<String> ids = List.of(currentUserData.id());

        // when
        when(userRepository.findAllByIdInAndOrderById(any()))
                .thenReturn(Flux.just(currentUserData));

        // then
        var setup = userService.getAllUserByIds(ids);

        Predicate<UserModel.User> predicate = u ->
                u.id().equals(currentUserData.id());

        StepVerifier.create(setup)
                .expectNextMatches(predicate)
                .expectNextCount(0)
                .verifyComplete();
    }
}
