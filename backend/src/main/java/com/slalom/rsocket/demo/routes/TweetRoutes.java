package com.slalom.rsocket.demo.routes;

import com.slalom.rsocket.demo.domain.Tweet;
import com.slalom.rsocket.demo.repository.TweetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Controller
public class TweetRoutes {

    private final TweetRepository tweetRepository;

    @MessageMapping("addTweet")
    public Mono<String> addTweet(final Tweet tweet) {
        final String id = UUID.randomUUID().toString();
        tweet.setId(id);
        return tweetRepository.add(tweet).then(Mono.just(id));
    }

    @MessageMapping("getTweet")
    public Mono<Tweet> getTweet(final String id) {
        return tweetRepository.get(id);
    }

    @MessageMapping("streamOfTweet")
    public Flux<Tweet> requestStream() {
        return tweetRepository.allTweets();
    }
}
