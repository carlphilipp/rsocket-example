package com.slalom.rsocket.demo.repository;

import com.slalom.rsocket.demo.domain.Tweet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
@Repository
public class TweetInMemoryRepository implements TweetRepository {

    private Map<String, Tweet> tweets = new ConcurrentHashMap<>();

    @Override
    public Mono<String> add(final Tweet tweet) {
        return Mono.fromCallable(
            () -> {
                tweets.put(tweet.getId(), tweet);
                return tweet;
            })
            .doOnNext(aBoolean -> log.info("Save tweet {}", tweet))
            .map(Tweet::getId);
    }

    @Override
    public Mono<Tweet> get(final String id) {
        return Mono.fromCallable(() -> tweets.get(id));
    }

    @Override
    public Flux<Tweet> allTweets() {
        return Flux.fromIterable(tweets.values());
    }

    @Override
    public List<Tweet> allTweetsAsList() {
        return new ArrayList<>(tweets.values());
    }

    @Override
    public void reset() {
        tweets.clear();
    }
}
