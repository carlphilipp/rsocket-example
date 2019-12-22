package com.slalom.rsocket.demo.repository;

import com.slalom.rsocket.demo.domain.Tweet;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface TweetRepository {

    Mono<String> add(final Tweet tweet);

    Mono<Tweet> get(final String id);

    Flux<Tweet> allTweets();

    List<Tweet> allTweetsAsList();

    void reset();
}
