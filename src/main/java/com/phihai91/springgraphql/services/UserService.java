package com.phihai91.springgraphql.services;

import com.phihai91.springgraphql.entities.User;
import com.phihai91.springgraphql.payloads.AuthModel;
import com.phihai91.springgraphql.repositories.IUserRepository;
import com.phihai91.springgraphql.securities.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
public class UserService implements IUserService {
    @Autowired
    private IUserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ReactiveAuthenticationManager authenticationManager;

    @Override
    public Mono<AuthModel.RegistrationUserPayload> registrationUser(AuthModel.RegistrationUserInput input) {
        //TODO Validate duplicate user

        User newUser = User.builder()
                .username(input.usernameOrEmail())
                .password(passwordEncoder.encode(input.password()))
                .roles(List.of("ADMIN"))
                .build();

        return userRepository.save(newUser)
                .log()
                .map(user -> user.toGetRegistrationUserPayload(user.id()));
    }

    @Override
    public Mono<AuthModel.VerifyOtpPayload> getToken(Mono<AuthModel.RegistrationUserInput> authRequest) {
        return authRequest
                .flatMap(login -> this.authenticationManager
                        .authenticate(new UsernamePasswordAuthenticationToken(
                                login.usernameOrEmail(), login.password()))
                        .log()
                        .map(this.jwtTokenProvider::createToken))
                        .log()
                .map(jwt -> {
                    HttpHeaders httpHeaders = new HttpHeaders();
                    httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
                    return AuthModel.VerifyOtpPayload.builder()
                            .accessToken(jwt)
                            .expiredDate(2323232L)
                            .signedDate(23232323L)
                            .build();
                });
    }
}
