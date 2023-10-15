package com.phihai91.springgraphql.controllers;

import com.phihai91.springgraphql.payloads.FriendModel;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
public class FriendController {
    @MutationMapping
    FriendModel.AddFriendPayload addFriend(@Argument FriendModel.AddFriendInput input) {
        return FriendModel.AddFriendPayload.builder()
                .requestId("RequestID")
                .friendId(input.userId())
                .message(input.message())
                .build();
    }

    @MutationMapping
    FriendModel.AcceptOrRejectFriendPayload acceptOrRejectFriend(@Argument FriendModel.AcceptOrRejectFriendInput input) {
        return FriendModel.AcceptOrRejectFriendPayload.builder()
                .requestId(input.requestId())
                .status(FriendModel.Status.COMPLETED)
                .build();
    }
}
