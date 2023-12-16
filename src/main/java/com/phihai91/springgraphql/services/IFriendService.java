package com.phihai91.springgraphql.services;

import com.phihai91.springgraphql.entities.Friend;
import com.phihai91.springgraphql.payloads.CommonModel;
import com.phihai91.springgraphql.payloads.FriendModel;
import graphql.relay.Connection;
import reactor.core.publisher.Mono;

public interface IFriendService {
    Mono<CommonModel.CommonPayload> sendRequest(FriendModel.AddFriendInput input);

    Mono<Friend> updateFriendData(String userId, String friendId);

    Mono<CommonModel.CommonPayload> ignoreFriendRequest(String userId);

    Mono<Connection<FriendModel.FriendRequest>> getFriendRequest(Integer first, String after);

    Mono<Connection<FriendModel.Friend>> getFriendList(Integer first, String after);

    Mono<CommonModel.CommonPayload> unfriend(String userId);
}
