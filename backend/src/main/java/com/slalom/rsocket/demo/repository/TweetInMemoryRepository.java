package com.slalom.rsocket.demo.repository;

import com.slalom.rsocket.demo.domain.Tweet;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.*;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Repository
public class TweetInMemoryRepository {

    private final Map<String, Tweet> tweets = new ConcurrentHashMap<>();
    private final DirectProcessor<List<Tweet>> directProcessor = DirectProcessor.create();
    private final EmitterProcessor<List<Tweet>> emitterProcessor = EmitterProcessor.create();

   /* {
        emitterProcessor.onNext(new ArrayList<>(tweets.values()));
    }*/

    public Mono<Boolean> add(final Tweet tweet) {
        return Mono.fromCallable(
            () -> {
                tweets.put(tweet.getId(), tweet);
                return true;
            })
            .doOnNext(aBoolean -> emitterProcessor.onNext(new ArrayList<>(tweets.values())));
    }


    public Mono<Tweet> get(final String id) {
        return Mono.fromCallable(() -> tweets.get(id));
    }

    public Flux<Tweet> allTweets() {
        return Flux.fromIterable(tweets.values());
    }

    public Mono<List<Tweet>> allTweetsAsList() {
        //return emitterProcessor.next();
        return Mono.fromCallable(() -> new ArrayList<>(tweets.values()));
    }

    public List<Tweet> allTweetsNoRx() {
        //return emitterProcessor.next();
        return new ArrayList<>(tweets.values());
    }
}
