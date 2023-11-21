package com.phihai91.springgraphql.services.impl;

import com.phihai91.springgraphql.entities.File;
import com.phihai91.springgraphql.repositories.IFileRepository;
import com.phihai91.springgraphql.securities.AppUserDetails;
import com.phihai91.springgraphql.services.IFileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Autowired
    private IFileRepository fileRepository;

    @Value("${maxSizeUpload}")
    private long maxSizeUpload;

    @Value("${fileSrc}")
    private String fileSrc;

    @Override
    @PreAuthorize("hasRole('USER')")
    public Mono<String> save(Mono<FilePart> filePartMono, long contentLength) {
        final Path root = Paths.get(fileSrc);
        if (contentLength > maxSizeUpload)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image size must not larger than 2MB");

        return filePartMono
                .map(Part::content) //Get Content
                .map(dataBufferFlux -> {
                    try {
                        var is = getInputStreamFromFluxDataBuffer(dataBufferFlux);
                        return detectDocTypeUsingDetector(is);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .flatMap(mimeType -> mimeType.contains("image") ?
                        filePartMono : Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid Image"))
                )
                .flatMap(filePart -> {
                    String ext = FilenameUtils.getExtension(filePart.filename());
                    String newFileName = UUID.randomUUID() + "." + ext;
                    return filePart.transferTo(root.resolve(newFileName)).then(Mono.just(newFileName));
                });
    }

    @Override
    public Flux<DataBuffer> load(String filename) {
        final Path root = Paths.get(this.fileSrc);
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
        final Path root = Paths.get(this.fileSrc);
        try {
            Path file = root.resolve(filename);
            return Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }
}
