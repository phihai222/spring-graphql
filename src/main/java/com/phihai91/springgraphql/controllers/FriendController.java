package com.phihai91.springgraphql.controllers;

import com.phihai91.springgraphql.payloads.CommonModel;
import com.phihai91.springgraphql.payloads.FriendModel;
import com.phihai91.springgraphql.payloads.UserModel;
import com.phihai91.springgraphql.services.IFriendService;
import com.phihai91.springgraphql.services.IUserService;
import graphql.relay.Connection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Controller
@Slf4j
public class FriendController {
    @Autowired
    private IFriendService friendService;

    @Autowired
    private IUserService userService;

    @MutationMapping
    Mono<CommonModel.CommonPayload> requestOrAcceptFriend(@Argument FriendModel.AddFriendInput input) {
        return friendService.sendRequest(input);
    }

    @MutationMapping
    Mono<CommonModel.CommonPayload> ignoreFriendRequest(@Argument String userId) {
        return friendService.ignoreFriendRequest(userId);
    }

    @QueryMapping
    Mono<Connection<FriendModel.FriendRequest>> getMyFriendRequest(
            @Argument Integer first,
            @Argument String after
    ) {
        return friendService.getFriendRequest(first, after);
    }

    @QueryMapping
    Mono<Connection<FriendModel.Friend>> getMyFriendList(
            @Argument Integer first,
            @Argument String after
    ) {
        return friendService.getFriendList(first, after);
    }

    @BatchMapping
    Flux<UserModel.User> info(List<FriendModel.Friend> friend) {
        List<String> ids = friend
                .stream()
                .map(FriendModel.Friend::id)
                .sorted() // Make sure order is the same
                .toList();

        return userService.getAllUserByIds(ids);
    }

    @MutationMapping
    Mono<CommonModel.CommonPayload> unfriend(@Argument String userId) {
        return friendService.unfriend(userId);
    }
}
