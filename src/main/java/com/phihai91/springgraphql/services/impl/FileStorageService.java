package com.phihai91.springgraphql.services.impl;

import com.phihai91.springgraphql.entities.File;
import com.phihai91.springgraphql.repositories.IFileRepository;
import com.phihai91.springgraphql.securities.AppUserDetails;
import com.phihai91.springgraphql.services.IFileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static com.phihai91.springgraphql.ultis.FileHelper.detectDocTypeUsingDetector;
import static com.phihai91.springgraphql.ultis.FileHelper.getInputStreamFromFluxDataBuffer;

@Service
@Slf4j
public class FileStorageService implements IFileStorageService {
    private final Path root = Paths.get("uploads");

    @Autowired
    private IFileRepository fileRepository;

    @Override
    @PreAuthorize("hasRole('USER')")
    public Mono<String> save(Mono<FilePart> filePartMono) {
        //TODO Dead Read End Bug need to fix reactor.core.Exceptions$ErrorCallbackNotImplemented: java.io.IOException: Read end dead
        // Operator called default onErrorDropped
        return filePartMono
                .map(Part::content) //Get Content
                .<Mono<FilePart>>handle((bs, sink) -> {
                    try {
                        var is = getInputStreamFromFluxDataBuffer(bs); //Convert Content to InputStream
                        var mimeType = detectDocTypeUsingDetector(is); //Get Mimetype by Tika
                        if (mimeType.contains("image")) {
                            sink.next(filePartMono);
                            return;
                        }

                        sink.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Image"));
                    } catch (IOException e) {
                        sink.error(new RuntimeException(e));
                    }
                })
                .flatMap(mediaType -> filePartMono)
                .flatMap(filePart -> {
                    String ext = FilenameUtils.getExtension(filePart.filename());
                    String newFileName = UUID.randomUUID() + "." + ext;
                    return filePart.transferTo(root.resolve(newFileName)).then(Mono.just(newFileName));
                });
    }

    @Override
    public Flux<DataBuffer> load(String filename) {
        try {
            Path file = root.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return DataBufferUtils.read(resource, new DefaultDataBufferFactory(), 4096);
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public Flux<String> loadAll() {
        return Flux.just("image1", "image2");
//        try {
//            return Files.walk(this.root, 1)
//                    .filter(path -> !path.equals(this.root))
//                    .map(this.root::relativize);
//        } catch (IOException e) {
//            throw new RuntimeException("Could not load the files!");
//        }
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public Mono<File> saveFileData(String fileName) {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> (AppUserDetails) securityContext.getAuthentication().getPrincipal())
                .flatMap(userDetails -> fileRepository.save(File.builder()
                        .name(fileName)
                        .ext(FilenameUtils.getExtension(fileName))
                        .isBinding(false)
                        .createdBy(userDetails.getId())
                        .build()));
    }

    @Override
    public boolean delete(String filename) {
        try {
            Path file = root.resolve(filename);
            return Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }
}
