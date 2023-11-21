package com.phihai91.springgraphql.controllers;

import com.phihai91.springgraphql.entities.File;
import com.phihai91.springgraphql.payloads.FileInfoDTO;
import com.phihai91.springgraphql.payloads.ResponseMessage;
import com.phihai91.springgraphql.services.IFileStorageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
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
    public Mono<ResponseEntity<File>> uploadFile(@RequestPart("file") Mono<FilePart> filePartMono, @RequestHeader("Content-Length") long contentLength) {
        return storageService.save(filePartMono, contentLength)
                .flatMap(fileName -> storageService.saveFileData(fileName))
                .map(ResponseEntity::ok);
    }

    //TODO /upload-multi API
    @GetMapping("/files")
    public ResponseEntity<Flux<FileInfoDTO>> getListFiles() {
        Flux<FileInfoDTO> fileInfoStream = storageService.loadAll().map(s -> {
            String url = UriComponentsBuilder.newInstance().path("/files/{filename}").buildAndExpand(s).toUriString();
            return new FileInfoDTO(s, url);
        });

        return ResponseEntity.status(HttpStatus.OK).body(fileInfoStream);
    }

    @GetMapping(value = "/files/{id}", produces = MediaType.IMAGE_JPEG_VALUE)
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

    @DeleteMapping("/files/{filename:.+}")
    public Mono<ResponseEntity<ResponseMessage>> deleteFile(@PathVariable String filename) {
        String message;

        try {
            boolean existed = storageService.delete(filename);

            if (existed) {
                message = "Delete the file successfully: " + filename;
                return Mono.just(ResponseEntity.ok().body(new ResponseMessage(message)));
            }

            message = "The file does not exist!";
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMessage(message)));
        } catch (Exception e) {
            message = "Could not delete the file: " + filename + ". Error: " + e.getMessage();
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseMessage(message)));
        }
    }
}
