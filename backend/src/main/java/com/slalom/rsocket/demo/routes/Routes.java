package com.slalom.rsocket.demo.routes;

import com.slalom.rsocket.demo.domain.SimplePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RequiredArgsConstructor
@Slf4j
@Controller
public class Routes {

    @MessageMapping("requestResponse")
    public Mono<String> requestResponse() {
        log.info("requestResponse");
        return Mono.just("hello world!");
    }

    @MessageMapping("fireAndForget")
    public Mono<Void> fireAndForget(final String payload) {
        log.info("fireAndForget {}", payload);
        return Mono.empty();
    }

    @MessageMapping("requestStream")
    public Flux<SimplePayload> requestStream(final String payload) {
        log.info("requestStream {}", payload);
        return Flux.range(1, 2)
            .delayElements(Duration.ofSeconds(2))
            .map(s -> SimplePayload.builder().message(payload + " " + s).build());
    }

    @MessageMapping("requestChannel")
    public Flux<String> requestChannel(final Publisher<String> payloads) {
        log.info("requestChannel");
        return Flux.from(payloads)
            .delayElements(Duration.ofSeconds(1))
            .map(s -> s + " updated")
            .doOnNext(s -> log.info("Sending back: " + s));
    }
}
