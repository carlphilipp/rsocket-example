package com.slalom.rsocket.demo.repository;

import com.slalom.rsocket.demo.domain.Tweet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.DirectProcessor;
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

    private DirectProcessor<List<Tweet>> repoProcessor = DirectProcessor.create();
    private Map<String, Tweet> tweets = new ConcurrentHashMap<>();

    @Override
    public Mono<String> add(final Tweet tweet) {
        return Mono.fromCallable(
            () -> {
                tweets.put(tweet.getId(), tweet);
                return tweet;
            })
            .map(Tweet::getId)
            .doOnNext(tweets -> repoProcessor.onNext(allTweetsAsList()));
    }

    @Override
    public Mono<Tweet> get(final String id) {
        return Mono.fromCallable(() -> tweets.get(id));
    }

    @Override
    public Flux<Tweet> allTweets() {
        return Flux.fromIterable(tweets.values());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Flux<List<Tweet>> allTweetsInFlux() {
        return Flux.merge(repoProcessor, Flux.fromIterable(tweets.values()))
            .flatMap(o -> o instanceof Tweet
                ? add((Tweet) o).thenMany(Flux.empty()) // Add tweet and stop processing. addTweet will publish a new event
                : Flux.just((List<Tweet>) o));
    }

    @Override
    public List<Tweet> allTweetsAsList() {
        return new ArrayList<>(tweets.values());
    }

    @Override
    public Mono<Void> reset() {
        log.info("Reset DB");
        tweets.clear();
        return Mono
            .fromCallable(() -> {
                tweets.clear();
                return "";
            })
            .doOnNext(res -> repoProcessor.onNext(allTweetsAsList()))
            .flatMap(tweets -> Mono.empty());

    }
}
