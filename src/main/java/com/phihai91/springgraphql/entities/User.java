package com.phihai91.springgraphql.entities;

import com.phihai91.springgraphql.payloads.AuthModel;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
        List<String> friends,
        List<UserPost> posts,
        Boolean active,

        List<String> roles
) {
        public User {
                roles = new ArrayList<>();
                active = true;
        }

        public AuthModel.RegistrationUserPayload toGetRegistrationUserPayload(String userId) {
                return AuthModel.RegistrationUserPayload.builder()
                        .id(userId)
                        .build();
        }
}
