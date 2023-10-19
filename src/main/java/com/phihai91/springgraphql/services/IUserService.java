package com.phihai91.springgraphql.services;

import com.phihai91.springgraphql.payloads.AuthModel;
import reactor.core.publisher.Mono;

public interface IUserService {
    Mono<AuthModel.RegistrationUserPayload> registrationUser(AuthModel.RegistrationUserInput input);
}
