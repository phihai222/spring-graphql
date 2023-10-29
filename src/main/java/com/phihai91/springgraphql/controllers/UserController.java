package com.phihai91.springgraphql.controllers;

import com.phihai91.springgraphql.payloads.UserModel;
import com.phihai91.springgraphql.services.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
public class UserController {
    @Autowired
    private IUserService userService;

    @QueryMapping
    public Mono<UserModel.User> getMyInfo() {
        return userService.getCurrentUserInfo();
    }
}
