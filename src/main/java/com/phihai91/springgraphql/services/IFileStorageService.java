package com.phihai91.springgraphql.services;

import com.phihai91.springgraphql.entities.File;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IFileStorageService {

    Mono<String> save(Mono<FilePart> filePartMono, long contentLength);

    Flux<DataBuffer> load(String filename);

    Mono<Boolean>  delete(String filename);

    Mono<File> saveFileData(String fileName);
}
