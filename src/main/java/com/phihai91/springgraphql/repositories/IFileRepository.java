package com.phihai91.springgraphql.repositories;

import com.phihai91.springgraphql.entities.File;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface IFileRepository extends ReactiveMongoRepository<File, String> {
}
