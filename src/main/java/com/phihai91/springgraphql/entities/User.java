package com.phihai91.springgraphql.entities;

import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document
@Builder
public record User(
        @Id String id,
        String username,
        String email,
        Boolean twoMF,
        String password,
        @CreatedDate
        LocalDateTime registrationDate,
        UserInfo userInfo,
        List<String> friends
//        List<PostEmbebed> postId

) {

}
