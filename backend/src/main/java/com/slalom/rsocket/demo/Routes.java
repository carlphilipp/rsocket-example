package com.slalom.rsocket.demo;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Controller
public class Routes {

    @MessageMapping("requestResponse")
    public Mono<String> requestResponse() {
        return Mono.just("hello world!");
    }

    @MessageMapping("fireAndForget")
    public Mono<Void> fireAndForget(String payload) {
        log.info("fireAndForget {}", payload);
        return Mono.empty();
    }

    @MessageMapping("requestStream")
    public Flux<String> requestStream(String payload) {
        log.info("requestStream {}", payload);
        return Flux.just("1", "2")
            .delayElements(Duration.ofSeconds(1))
            .map(s -> payload + " " + s);
    }

    @MessageMapping("requestChannel")
    public Flux<String> requestChannel(Publisher<String> payloads) {
        log.info("requestChannel in Server");
        return Flux.from(payloads)
            .delayElements(Duration.ofSeconds(1))
            .map(s -> s + " updated")
            .doOnNext(s -> log.info("Sending back: " + s));
    }
}
