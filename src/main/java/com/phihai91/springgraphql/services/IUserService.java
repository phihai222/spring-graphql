package com.phihai91.springgraphql.services;

import com.phihai91.springgraphql.payloads.UserModel;
import reactor.core.publisher.Mono;

public interface IUserService {
    Mono<UserModel.User> getCurrentUserInfo();
    Mono<UserModel.User> updateUserInfo(UserModel.UpdateUserInput input);
    Mono<UserModel.SetTwoMFPayload> setTwoMF();
}
