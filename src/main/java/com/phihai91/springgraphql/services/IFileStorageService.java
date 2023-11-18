package com.phihai91.springgraphql.services;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface IFileStorageService {

    Mono<String> save(Mono<FilePart> filePartMono);

    Flux<DataBuffer> load(String filename);

    boolean delete(String filename);

    Stream<Path> loadAll();
}
