package com.phihai91.springgraphql.entities;

import com.phihai91.springgraphql.payloads.AuthModel;
import com.phihai91.springgraphql.securities.AppUserDetails;
import lombok.Builder;
import lombok.With;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Document
@Builder
@With
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
        List<Role> roles
) {
    public AuthModel.RegistrationUserPayload toGetRegistrationUserPayload(String userId) {
        return AuthModel.RegistrationUserPayload.builder()
                .id(userId)
                .build();
    }

    public AppUserDetails toAppUserDetails() {
        return AppUserDetails.builder()
                .id(id)
                .email(email)
                .twoMF(twoMF)
                .username(username)
                .authorities(roles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.name()))
                        .collect(Collectors.toList()))
                .build();
    }
}
