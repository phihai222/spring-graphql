package com.phihai91.springgraphql.services;

import com.phihai91.springgraphql.payloads.AuthModel;
import com.phihai91.springgraphql.repositories.IUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class UserService implements IUserService {
    @Autowired
    private IUserRepository userRepository;
    @Override
    public Mono<AuthModel.RegistrationUserPayload> registrationUser(AuthModel.RegistrationUserInput input) {
        var newUser = input.toUser();

        return userRepository.save(newUser)
                .log()
                .map(user -> user.toGetRegistrationUserPayload(user.id()));
    }
}
