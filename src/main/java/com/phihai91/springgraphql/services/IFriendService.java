package com.phihai91.springgraphql.services;

import com.phihai91.springgraphql.payloads.CommonModel;
import com.phihai91.springgraphql.payloads.FriendModel;
import reactor.core.publisher.Mono;

public interface IFriendService {
    Mono<CommonModel.CommonPayload> sendRequest(FriendModel.AddFriendInput input);
}
