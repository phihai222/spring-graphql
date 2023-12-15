package com.phihai91.springgraphql.services;

import com.phihai91.springgraphql.payloads.CommonModel;
import com.phihai91.springgraphql.payloads.UserModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IUserService {
    Mono<UserModel.User> getCurrentUserInfo();
    Mono<UserModel.User> updateUserInfo(UserModel.UpdateUserInput input);
    Mono<UserModel.SetTwoMFAPayload> setTwoMFA();
    Mono<CommonModel.CommonPayload> verifyTwoMFOtp(String otp);
    Mono<UserModel.User> getUserByUsername(String username);
    Flux<UserModel.User> getAllUserByIds(List<String> ids);
}
