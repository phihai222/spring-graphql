package com.phihai91.springgraphql.unit.impl.services;

import com.phihai91.springgraphql.entities.User;
import com.phihai91.springgraphql.payloads.AuthModel;
import com.phihai91.springgraphql.repositories.IUserRepository;
import com.phihai91.springgraphql.services.impl.AuthService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.Predicate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

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
    public void getToken() {
    }

    @Test
    public void login() {
    }

    @Test
    public void getOtp() {
    }

    @Test
    public void verifyOtp() {
    }
}