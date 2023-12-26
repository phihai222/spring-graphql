package com.phihai91.springgraphql.unit.impl.services;

import com.phihai91.springgraphql.entities.User;
import com.phihai91.springgraphql.entities.UserInfo;
import com.phihai91.springgraphql.exceptions.BadRequestException;
import com.phihai91.springgraphql.exceptions.ForbiddenException;
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
}
