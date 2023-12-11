package com.phihai91.springgraphql.entities;

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
        List<FriendData> friends
) {
}
