package com.phihai91.springgraphql.repositories;

import com.phihai91.springgraphql.entities.Comment;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICommentRepository extends ReactiveMongoRepository<Comment, String> {
}
