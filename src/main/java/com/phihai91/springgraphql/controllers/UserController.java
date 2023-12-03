package com.phihai91.springgraphql.controllers;

import com.phihai91.springgraphql.payloads.CommonModel;
import com.phihai91.springgraphql.payloads.UserModel;
import com.phihai91.springgraphql.services.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@Slf4j
public class UserController {
    @Autowired
    private IUserService userService;

    @QueryMapping
    public Mono<UserModel.User> getMyInfo() {
        return userService.getCurrentUserInfo();
    }

    @MutationMapping
    public Mono<UserModel.User> updateUserInfo(@Argument UserModel.UpdateUserInput input) {
        return userService.updateUserInfo(input);
    }

    @MutationMapping
    public Mono<UserModel.SetTwoMFAPayload> setTwoMF() {
        return userService.setTwoMFA();
    }

    @MutationMapping
    public Mono<CommonModel.CommonPayload> verifyTwoMFOtp(@Argument String input) {
        return userService.verifyTwoMFOtp(input);
    }
    @QueryMapping
    public Mono<UserModel.User> getUserByNameOrEmail(@Argument String usernameOrEmail) {
        return userService.getUserByUsername(usernameOrEmail);
    }
}
