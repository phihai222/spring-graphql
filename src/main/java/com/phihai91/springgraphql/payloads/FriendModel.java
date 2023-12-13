package com.phihai91.springgraphql.payloads;

import lombok.Builder;
import lombok.With;

import java.time.LocalDateTime;

public class FriendModel {
    @Builder
    public record AddFriendInput(
            String userId,
            String message
    ) {
    }

    @Builder
    @With
    public record FriendRequest(
            String id,
            String fromUser,
            String toUser,
            String message,
            LocalDateTime createdDate

    ) {
    }

    @Builder
    public record Friend(
            String id,
            UserModel.User info,
            LocalDateTime addedDate
    ) {
    }
}
