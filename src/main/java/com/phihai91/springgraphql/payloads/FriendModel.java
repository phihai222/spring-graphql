package com.phihai91.springgraphql.payloads;

import lombok.Builder;
import lombok.With;

import java.time.LocalDateTime;

public class FriendModel {
    @Builder
    @With
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
            String cursor,
            UserModel.User info,
            LocalDateTime addedDate
    ) {
    }
}
