package com.phihai91.springgraphql.payloads;

import lombok.AllArgsConstructor;
import lombok.Builder;

public class FriendModel {
    @Builder
    public record AddFriendInput(
            String userId,
            String message
    ) {
    }

    @Builder
    public record AddFriendPayload(
            String requestId,
            String friendId,
            String message
    ) {
    }

    @Builder
    public record AcceptOrRejectFriendInput(
            String requestId,
            Boolean status
    ) {
    }

    @Builder
    public record AcceptOrRejectFriendPayload(
            String requestId,
            Status status
    ) {
    }

    @AllArgsConstructor
    public enum Status {
        COMPLETED()
    }
}
