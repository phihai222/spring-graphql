package com.phihai91.springgraphql.unit.impl.services;

import com.phihai91.springgraphql.entities.User;
import com.phihai91.springgraphql.payloads.AuthModel;
import com.phihai91.springgraphql.repositories.IUserRepository;
import com.phihai91.springgraphql.securities.AppUserDetails;
import com.phihai91.springgraphql.securities.JwtTokenProvider;
import com.phihai91.springgraphql.services.impl.AuthService;
import com.phihai91.springgraphql.services.impl.RedisService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private ReactiveAuthenticationManager authenticationManager;

    @Mock
    private RedisService redisService;


    @Test
    @DisplayName("Registration new user")
    public void registrationUser() {
        AuthModel.RegistrationUserInput input = AuthModel.RegistrationUserInput
                .builder()
                .usernameOrEmail("phihai91")
                .password("@Phihai91")
                .build();

        User userRes = User.builder()
                .id(new ObjectId().toString())
                .username(input.usernameOrEmail())
                .build();

        when(passwordEncoder.encode(any()))
                .thenReturn("PasswordEncoded");

        when(this.userRepository.existsUserByUsernameEqualsOrEmailEquals(any(), any()))
                .thenReturn(Mono.just(false));

        when(this.userRepository.save(any()))
                .thenReturn(Mono.just(userRes));

        var setup = authService.registrationUser(input);

        Predicate<AuthModel.RegistrationUserPayload> userPredicate = u -> u.id() != null;

        StepVerifier.create(setup)
                .expectNextMatches(userPredicate)
                .verifyComplete();
    }

    @Test
    @DisplayName("Get Token")
    public void getToken() {
        String userId = new ObjectId().toString();

        when(this.userRepository.findById(anyString()))
                .thenReturn(Mono.just(User.builder()
                        .id(userId)
                        .build()));

        when(this.jwtTokenProvider.createToken(any()))
                .thenReturn(AuthModel.VerifyOtpPayload.builder()
                        .accessToken("accessToken")
                        .build());

        var setup = authService.getToken(userId);

        Predicate<AuthModel.VerifyOtpPayload> otpPredicate = otp -> otp.accessToken() != null;

        StepVerifier.create(setup)
                .expectNextMatches(otpPredicate)
                .verifyComplete();
    }

    @Test
    @DisplayName("Login")
    public void login() {
        AuthModel.LoginUserInput input = AuthModel.LoginUserInput.builder()
                .usernameOrEmail("phihai91")
                .password("@Phihai91")
                .build();

        Authentication authentication = mock(Authentication.class);

        when(authentication.getPrincipal())
                .thenReturn(AppUserDetails.builder()
                        .id(new ObjectId().toString())
                        .twoMFA(true)
                        .username(input.usernameOrEmail())
                        .authorities(List.of())
                        .build());

        when(this.authenticationManager.authenticate(any()))
                .thenReturn(Mono.just(authentication));

        when(this.redisService.saveOtp(any(), any(), any()))
                .thenReturn(Mono.just(true));

        var setup = authService.login(input);

        Predicate<AuthModel.LoginUserPayload> loginUserPayloadPredicate =
                payload -> payload.twoMFA().equals(true);

        StepVerifier.create(setup)
                .expectNextMatches(loginUserPayloadPredicate)
                .verifyComplete();
    }

    @Test
    @DisplayName("Get OTP")
    public void getOtp() {
        var res = authService.getOtp();
        assertEquals(res.length(), 6);
    }

    @Test
    public void verifyOtp() {
    }
}