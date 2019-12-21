package com.slalom.rsocket.demo.repository;

import com.slalom.rsocket.demo.domain.Tweet;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Repository
public class TweetRepository {

    private final ReactiveRedisOperations<String, Tweet> operations;

    public Mono<Boolean> add(final Tweet tweet) {
        return operations.opsForValue().set(tweet.getId(), tweet);
    }

    public Mono<Tweet> get(final String id) {
        return operations.opsForValue().get(id);
    }

    public Flux<Tweet> allTweets() {
        return operations
            .keys("*")
            .flatMap(operations.opsForValue()::get);
    }
}
