package com.slalom.rsocket.demo.repository;

import com.slalom.rsocket.demo.domain.Tweet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Repository
public class TweetInMemoryRepository {

    private final Map<String, Tweet> tweets = new ConcurrentHashMap<>();

    public Mono<Boolean> add(final Tweet tweet) {
        return Mono.fromCallable(
            () -> {
                tweets.put(tweet.getId(), tweet);
                return true;
            });
    }


    public Mono<Tweet> get(final String id) {
        return Mono.fromCallable(() -> tweets.get(id));
    }

    public Flux<Tweet> allTweets() {
        return Flux.fromIterable(tweets.values());
    }

    public List<Tweet> allTweetsNoRx() {
        return new ArrayList<>(tweets.values());
    }
}
