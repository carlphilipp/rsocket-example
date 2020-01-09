package com.slalom.rsocket.demo.routes;

import com.slalom.rsocket.demo.domain.Tweet;
import com.slalom.rsocket.demo.repository.TweetMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Controller
public class TweetRoutes {

    private final TweetMongoRepository tweetMongoRepository;

    @MessageMapping("reset")
    public Mono<Void> resetDb() {
        return tweetMongoRepository.deleteAll();
    }

    @MessageMapping("addTweet")
    public Mono<Tweet> addTweet(final Tweet tweet) {
        return Mono.just(UUID.randomUUID().toString())
            .map(id -> tweet.toBuilder().id(id).build())
            .flatMap(tweetMongoRepository::save);
    }

    @MessageMapping("getTweet")
    public Mono<Tweet> getTweet(final String id) {
        return tweetMongoRepository.findById(id);
    }

    @MessageMapping("streamOfTweet")
    public Flux<Tweet> requestStream() {
        return tweetMongoRepository.getAllByIdNotNull();
    }

    @MessageMapping("channelOfTweet")
    public Flux<Tweet> requestChannel(final Publisher<Tweet> clientPublisher) {
        return Flux.merge(clientPublisher, tweetMongoRepository.getAllByIdNotNull())
            .flatMap(tweet -> tweet.getId() == null
                ? addTweet(tweet)
                : Mono.just(tweet));
    }
}
