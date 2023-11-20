package com.phihai91.springgraphql.controllers;

import com.phihai91.springgraphql.entities.File;
import com.phihai91.springgraphql.payloads.FileInfoDTO;
import com.phihai91.springgraphql.payloads.ResponseMessage;
import com.phihai91.springgraphql.services.IFileStorageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
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
    public Mono<ResponseEntity<File>> uploadFile(@RequestPart("file") Mono<FilePart> filePartMono) {
        return storageService.save(filePartMono)
                .flatMap(fileName -> storageService.saveFileData(fileName))
                .map(ResponseEntity::ok);
    }

    @PostMapping(value = "/upload-multi", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Flux<ResponseMessage>> uploadFile(@RequestPart("file") Flux<FilePart> filePartMono) {
        var res = Flux.just(new ResponseMessage("ok"));
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @GetMapping("/files")
    public ResponseEntity<Flux<FileInfoDTO>> getListFiles() {
        Flux<FileInfoDTO> fileInfoStream = storageService.loadAll().map(s -> {
            String url = UriComponentsBuilder.newInstance().path("/files/{filename}").buildAndExpand(s).toUriString();
            return new FileInfoDTO(s, url);
        });

        return ResponseEntity.status(HttpStatus.OK).body(fileInfoStream);
    }

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Flux<DataBuffer>> getFile(@PathVariable String filename) {
        Flux<DataBuffer> file = storageService.load(filename);

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM).body(file);
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
