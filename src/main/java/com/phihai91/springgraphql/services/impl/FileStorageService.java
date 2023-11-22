package com.phihai91.springgraphql.services.impl;

import com.phihai91.springgraphql.entities.File;
import com.phihai91.springgraphql.entities.Visibility;
import com.phihai91.springgraphql.repositories.IFileRepository;
import com.phihai91.springgraphql.securities.AppUserDetails;
import com.phihai91.springgraphql.services.IFileStorageService;
import com.phihai91.springgraphql.ultis.UserHelper;
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
                        filePartMono : Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Image"))
                )
                .flatMap(filePart -> {
                    String ext = FilenameUtils.getExtension(filePart.filename());
                    String newFileName = UUID.randomUUID() + "." + ext;
                    return filePart.transferTo(root.resolve(newFileName)).then(Mono.just(newFileName));
                });
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public Flux<DataBuffer> load(String id) {
        final Path root = Paths.get(this.fileSrc);
        Mono<File> result = fileRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "File not found")));

        return result.map(File::name)
                .<Flux<DataBuffer>>handle((filename, sink) -> {
                    try {
                        Path file = root.resolve(filename);
                        Resource resource = new UrlResource(file.toUri());

                        if (resource.exists() || resource.isReadable()) {
                            sink.next(DataBufferUtils.read(resource, new DefaultDataBufferFactory(), 4096));
                        } else {
                            sink.error(new RuntimeException("Could not read the file!"));
                        }
                    } catch (MalformedURLException e) {
                        sink.error(new RuntimeException("Error: " + e.getMessage()));
                    }
                }).flatMapMany(dataBufferFlux -> dataBufferFlux);
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public Mono<File> saveFileData(String fileName) {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> (AppUserDetails) securityContext.getAuthentication().getPrincipal())
                .flatMap(userDetails -> fileRepository.save(File.builder()
                        .name(fileName)
                        .visibility(Visibility.PUBLIC)
                        .ext(FilenameUtils.getExtension(fileName))
                        .isBinding(false)
                        .createdBy(userDetails.getId())
                        .build()));
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public Mono<Boolean> delete(String id) {
        final Path root = Paths.get(this.fileSrc);

        Mono<AppUserDetails> appUserDetailsMono = ReactiveSecurityContextHolder.getContext()
                .map(UserHelper::getUserDetails);

        Mono<File> fileDataResult = fileRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "File not found")));

        return fileDataResult
                .zipWith(appUserDetailsMono, (file, userDetails) -> file.createdBy().equals(userDetails.getId()))
                .map(aBoolean -> aBoolean); //TODO delete file in server

//        final Path root = Paths.get(this.fileSrc);
//        try {
//            Path file = root.resolve(id);
//            return Files.deleteIfExists(file);
//        } catch (IOException e) {
//            throw new RuntimeException("Error: " + e.getMessage());
//        }
    }
}
