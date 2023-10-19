package com.phihai91.springgraphql.entities;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Visibility {
    PUBLIC(),
    PRIVATE(),
    FRIEND_ONLY()
}
