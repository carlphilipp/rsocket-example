package com.slalom.rsocket.demo.routes;

import com.slalom.rsocket.demo.domain.Tweet;
import com.slalom.rsocket.demo.repository.TweetInMemoryRepository;
import com.slalom.rsocket.demo.repository.TweetMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Controller
public class TweetRoutes {

    private final TweetInMemoryRepository tweetRepository;
    private final TweetMongoRepository tweetMongoRepository;

    @MessageMapping("reset")
    public Mono<Void> resetDb() {
        return tweetRepository.reset();
    }

    @MessageMapping("addTweet")
    public Mono<Tweet> addTweet(final Tweet tweet) {
        return tweetMongoRepository.save(tweet)
            .doOnNext(tweet1 -> {
                System.out.println(tweet1);
            })
            .then(Mono.just(UUID.randomUUID().toString()))
            .map(uuid -> tweet.toBuilder().id(uuid).build())
            .flatMap(tweetRepository::add)
            .map(id -> tweet.toBuilder().id(id).build());
    }

    @MessageMapping("getTweet")
    public Mono<Tweet> getTweet(final String id) {
        return tweetRepository.get(id);
    }

    @MessageMapping("streamOfTweet")
    public Flux<Tweet> requestStream() {
        return tweetMongoRepository.getAllByIdNotNull().delayElements(Duration.ofMillis(500L));
        //return tweetRepository.allTweets().delayElements(Duration.ofMillis(500L));
    }

    @SuppressWarnings("unchecked")
    @MessageMapping("channelOfTweet")
    public Flux<List<Tweet>> requestChannel(final Publisher<Tweet> clientPublisher) {
        return Flux.merge(clientPublisher, tweetRepository.allTweetsInFlux())
            .flatMap(object -> object instanceof Tweet
                ? addTweet((Tweet) object).flatMap(bool -> Mono.empty())
                : Mono.just((List<Tweet>) object));
    }
}
