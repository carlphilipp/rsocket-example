package com.slalom.rsocket.demo.routes;

import com.slalom.rsocket.demo.domain.Tweet;
import com.slalom.rsocket.demo.repository.TweetInMemoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Controller
public class TweetRoutes {

    private EmitterProcessor<List<Tweet>> emitterProcessor = EmitterProcessor.create();

    private final TweetInMemoryRepository tweetRepository;

    @MessageMapping("addTweet")
    public Mono<String> addTweet(final Tweet tweet) {
        final String id = UUID.randomUUID().toString();
        tweet.setId(id);
        return tweetRepository.add(tweet)
            .map(aBoolean -> {
                emitterProcessor.onNext(tweetRepository.allTweetsNoRx());
                return aBoolean;
            })
            .then(Mono.just(id));
    }

    @MessageMapping("getTweet")
    public Mono<Tweet> getTweet(final String id) {
        return tweetRepository.get(id);
    }

    @MessageMapping("streamOfTweet")
    public Flux<Tweet> requestStream() {
        return tweetRepository.allTweets();
    }

    @MessageMapping("channelOfTweet")
    public Flux<List<Tweet>> requestChannel(final Publisher<Tweet> tweetPublisher) {
        return Flux.concat(tweetPublisher, emitterProcessor)
            .doOnNext(tweet -> log.info("[Server] Receiving {}", tweet))
            .flatMap(object -> {
                if (object instanceof Tweet) {
                    return addTweet((Tweet) object).flatMap(bool -> tweetRepository.allTweetsAsList());
                } else {
                    return Mono.just((List<Tweet>) object);
                }
            });
    }
}
