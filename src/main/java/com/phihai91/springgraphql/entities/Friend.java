package com.phihai91.springgraphql.entities;

import com.phihai91.springgraphql.payloads.FriendModel;
import lombok.Builder;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@With
@Builder
public record Friend(
        @Id
        String id,
        List<FriendData> friends,
        FriendData friend
) {
        public FriendModel.Friend toFriendPayload() {
                return FriendModel.Friend.builder()
                        .id(friend.id())
                        .addedDate(friend.addedDate())
                        .build();
        }
}
