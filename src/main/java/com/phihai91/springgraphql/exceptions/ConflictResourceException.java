package com.phihai91.springgraphql.exceptions;

public class ConflictResourceException extends RuntimeException{
    public ConflictResourceException(String message) {
        super(message);
    }
}
