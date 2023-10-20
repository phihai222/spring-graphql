package com.phihai91.springgraphql.services;

import com.phihai91.springgraphql.entities.User;
import com.phihai91.springgraphql.payloads.AuthModel;
import com.phihai91.springgraphql.repositories.IUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Override
    public Mono<AuthModel.RegistrationUserPayload> registrationUser(AuthModel.RegistrationUserInput input) {
        User newUser = User.builder()
                .username(input.usernameOrEmail())
                .password(passwordEncoder.encode(input.password()))
                .roles(List.of("ADMIN"))
                .build();

        return userRepository.save(newUser)
                .log()
                .map(user -> user.toGetRegistrationUserPayload(user.id()));
    }
}
