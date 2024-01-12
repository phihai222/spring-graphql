package com.phihai91.springgraphql.controllers.REST;

import com.phihai91.springgraphql.entities.File;
import com.phihai91.springgraphql.payloads.CommonModel;
import com.phihai91.springgraphql.services.IFileStorageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/files")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class FileController {

    @Autowired
    private IFileStorageService storageService;

    @PostMapping(value = "/upload-single", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public Mono<ResponseEntity<File>> uploadFile(@RequestPart("file") Mono<FilePart> filePartMono, @RequestHeader HttpHeaders httpHeaders) {
        long contentLength = httpHeaders.getContentLength();

        return storageService.save(filePartMono, contentLength)
                .flatMap(fileName -> storageService.saveFileData(fileName))
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/{id}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<Mono<byte[]>> getFile(@PathVariable String id) {
        Flux<DataBuffer> file = storageService.load(id);
        var result = DataBufferUtils.join(file)
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return bytes;
                });
        return ResponseEntity.ok().body(result);
    }

    @DeleteMapping("/files/{id}")
    public ResponseEntity<Mono<CommonModel.CommonPayload>> deleteFile(@PathVariable String id) {
        var result = storageService.delete(id)
                .map(aBoolean -> CommonModel.CommonPayload.builder()
                        .status(aBoolean ? CommonModel.CommonStatus.SUCCESS : CommonModel.CommonStatus.FAILED)
                        .message(aBoolean ? "Delete Successfully" : "Delete Failed")
                        .build());
        return ResponseEntity.ok().body(result);
    }
}
