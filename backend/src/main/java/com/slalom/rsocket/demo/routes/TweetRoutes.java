package com.slalom.rsocket.demo.routes;

import com.slalom.rsocket.demo.domain.Tweet;
import com.slalom.rsocket.demo.repository.TweetInMemoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
@Controller
public class TweetRoutes {

    private DirectProcessor<List<Tweet>> repoProcessor = DirectProcessor.create();

    private final TweetInMemoryRepository tweetRepository;

    @MessageMapping("addTweet")
    public Mono<Tweet> addTweet(final Tweet tweet) {
        return Mono.just(UUID.randomUUID().toString())
            .map(uuid -> tweet.toBuilder().id(uuid).build())
            .flatMap(tweetRepository::add)
            .map(id -> tweet.toBuilder().id(id).build())
            .doOnNext(res -> repoProcessor.onNext(tweetRepository.allTweetsAsList()));
    }

    @MessageMapping("getTweet")
    public Mono<Tweet> getTweet(final String id) {
        return tweetRepository.get(id);
    }

    @MessageMapping("streamOfTweet")
    public Flux<Tweet> requestStream() {
        return tweetRepository.allTweets();
    }

    @SuppressWarnings("unchecked")
    @MessageMapping("channelOfTweet")
    public Flux<List<Tweet>> requestChannel(final Publisher<Tweet> clientPublisher) {
        return Flux.merge(repoProcessor, clientPublisher)
            .flatMap(object -> object instanceof Tweet
                ? addTweet((Tweet) object).flatMap(bool -> Mono.empty()) // Add tweet and stop processing. addTweet will publish a new event
                : Mono.just((List<Tweet>) object));
    }
}
