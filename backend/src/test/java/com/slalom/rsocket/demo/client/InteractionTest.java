package com.slalom.rsocket.demo.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slalom.rsocket.demo.domain.SimplePayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Test 4 different type of rsocket interaction")
@SpringBootTest
class InteractionTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final MediaType[] SUPPORTED_TYPES = {MediaType.APPLICATION_JSON, new MediaType("application", "*+json")};

    private RSocketRequester rSocketRequester;

    @PostConstruct
    void postConstruct() {
        rSocketRequester = RSocketRequester.builder()
            .rsocketStrategies(RSocketStrategies.builder()
                .encoder(new Jackson2JsonEncoder(MAPPER, SUPPORTED_TYPES))
                .decoder(new Jackson2JsonDecoder(MAPPER, SUPPORTED_TYPES))
                .build())
            .dataMimeType(MediaType.APPLICATION_JSON)
            .connectWebSocket(URI.create("ws://localhost:7000"))
            .block();
    }

    @DisplayName("Request response")
    @Test
    void shouldObtainRequestResponse() {
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
        // when
        final List<SimplePayload> result = rSocketRequester
            .route("requestStream")
            .data("hello")
            .retrieveFlux(new ParameterizedTypeReference<SimplePayload>() {
            })
            .collectList()
            .block();

        // then
        assertThat(result).contains(
            SimplePayload.builder().message("hello 1").build(),
            SimplePayload.builder().message("hello 2").build()
        );
    }

    @DisplayName("Request channel")
    @Test
    void shouldRequestChannel() {
        // given
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
