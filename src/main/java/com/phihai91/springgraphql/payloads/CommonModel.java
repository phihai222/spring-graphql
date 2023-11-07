package com.phihai91.springgraphql.payloads;


import lombok.Builder;

public class CommonModel {
    @Builder
    public record CommonPayload(
            CommonStatus status,
            String message
    ) {}

    public enum CommonStatus {
        SUCCESS,
        FAILED
    }
}

