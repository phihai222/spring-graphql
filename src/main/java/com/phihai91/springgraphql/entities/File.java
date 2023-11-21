package com.phihai91.springgraphql.entities;

import lombok.Builder;
import lombok.With;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Builder
@Document
@With
public record File(
        @Id
        String id,
        String name,
        String ext,
        Boolean isBinding,
        String createdBy,

        Visibility visibility,
        @CreatedDate
        LocalDateTime createdDate,
        @LastModifiedDate
        LocalDateTime updatedDate
) {
}
