package com.phihai91.springgraphql.payloads;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FileInfoDTO {
    private String name;
    private String url;
}
