package com.slalom.rsocket.demo.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Test 4 different type of rsocket interaction")
class InteractionTest {

    @DisplayName("Request response")
    @Test
    void shouldObtainRequestResponse() {
        // given
        RSocketRequester rSocketRequester = RSocketRequester.builder()
            .connectTcp("localhost", 7000)
            .block();

        // when
        final String result = rSocketRequester
            .route("requestResponse")
            .retrieveMono(String.class)
            .block();

        // then
        assertThat(result).isEqualTo("hello world!");
    }

    @DisplayName("Fire and Forget")
    @Test
    void shouldFireAndForget() {
        // given
        RSocketRequester rSocketRequester = RSocketRequester.builder()
            .connectTcp("localhost", 7000)
            .block();

        // when
        final Void result = rSocketRequester
            .route("fireAndForget")
            .data("hello")
            .retrieveMono(Void.class)
            .block();

        // then
        assertThat(result).isNull();
    }

    @DisplayName("Request stream")
    @Test
    void shouldRequestStream() {
        // given
        RSocketRequester rSocketRequester = RSocketRequester.builder()
            .connectTcp("localhost", 7000)
            .block();

        // when
        final List<String> result = rSocketRequester
            .route("requestStream")
            .data("hello")
            .retrieveFlux(String.class)
            .collectList()
            .block();

        // then
        assertThat(result).contains("hello 1", "hello 2");
    }

    @DisplayName("Request channel")
    @Test
    void shouldRequestChannel() {
        // given
        RSocketRequester rSocketRequester = RSocketRequester.builder()
            .connectTcp("localhost", 7000)
            .block();
        Flux<String> payloads = Flux.range(0, 5)
            .map(i -> "Welcome to Rsocket #" + i);

        // when
        final List<String> result = rSocketRequester
            .route("requestChannel")
            .data(payloads)
            .retrieveFlux(String.class)
            .doOnNext(msg -> System.out.println("received messages::" + msg))
            .collectList()
            .block();

        // then
        assertThat(result).contains(
            "Welcome to Rsocket #0 updated",
            "Welcome to Rsocket #1 updated",
            "Welcome to Rsocket #2 updated",
            "Welcome to Rsocket #3 updated",
            "Welcome to Rsocket #4 updated"
        );
    }
}
