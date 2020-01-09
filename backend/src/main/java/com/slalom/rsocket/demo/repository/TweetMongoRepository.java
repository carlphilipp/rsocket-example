package com.slalom.rsocket.demo.repository;

import com.slalom.rsocket.demo.domain.Tweet;
import org.springframework.data.mongodb.repository.Tailable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface TweetMongoRepository extends ReactiveCrudRepository<Tweet, String> {

    @Tailable
    Flux<Tweet> getAllByIdNotNull();
}
