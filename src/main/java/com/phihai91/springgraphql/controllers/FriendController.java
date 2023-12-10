package com.phihai91.springgraphql.controllers;

import com.phihai91.springgraphql.payloads.CommonModel;
import com.phihai91.springgraphql.payloads.FriendModel;
import com.phihai91.springgraphql.services.IFriendService;
import graphql.relay.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
public class FriendController {
    @Autowired
    private IFriendService friendService;

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
    ){
        return friendService.getFriendRequest(first, after);
    }
}
