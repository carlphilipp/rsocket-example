package com.slalom.rsocket.demo.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slalom.rsocket.demo.domain.Tweet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Test tweets")
@SpringBootTest
public class TweetTest {

    private static final String ID_REGEX = "[\\w\\d]{8}-[\\w\\d]{4}-[\\w\\d]{4}-[\\w\\d]{4}-[\\w\\d]{12}";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final MediaType[] SUPPORTED_TYPES = {MediaType.APPLICATION_JSON, new MediaType("application", "*+json")};

    private RSocketRequester rSocketRequester;
    @Autowired
    private RedisServer redisServer;

    @PostConstruct
    void postConstruct() {
        rSocketRequester = RSocketRequester.builder()
            .rsocketStrategies(RSocketStrategies.builder()
                .encoder(new Jackson2JsonEncoder(MAPPER, SUPPORTED_TYPES))
                .decoder(new Jackson2JsonDecoder(MAPPER, SUPPORTED_TYPES))
                .build())
            .dataMimeType(MediaType.APPLICATION_JSON)
            .connectTcp("localhost", 7000)
            .block();
    }

    @BeforeEach
    void beforeEach() {
        redisServer.stop();
        redisServer.start();
    }

    @DisplayName("Add a tweet")
    @Test
    void shouldAddATweet() {
        // given
        Tweet tweet = Tweet.builder().author("carl").content("Hello rsocket").build();

        // when
        Mono<String> mono = rSocketRequester.route("addTweet").data(tweet).retrieveMono(String.class);

        // then
        StepVerifier.create(mono)
            .expectNextMatches(result -> {
                assertThat(result).isNotBlank();
                assertThat(result).matches(ID_REGEX);
                return true;
            })
            .verifyComplete();
    }

    @DisplayName("Get a tweet")
    @Test
    void shouldGetATweet() {
        // given
        Tweet tweet = Tweet.builder().author("carl").content("Hello rsocket get tweet").build();
        final String id = rSocketRequester
            .route("addTweet")
            .data(tweet)
            .retrieveMono(String.class)
            .block();

        // when
        final Tweet actual = rSocketRequester
            .route("getTweet")
            .data(id)
            .retrieveMono(Tweet.class)
            .block();

        // then
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(actual.getAuthor()).isEqualTo("carl");
        assertThat(actual.getContent()).isEqualTo("Hello rsocket get tweet");
    }

    @DisplayName("Get a stream of tweet")
    @Test
    void shouldGetAStreamOfTweet() {
        // given
        Tweet tweet = Tweet.builder().author("carl").content("Hello rsocket get tweet").build();
        rSocketRequester
            .route("addTweet")
            .data(tweet)
            .retrieveMono(String.class)
            .doOnNext(s -> System.out.println("ID found: " + s))
            .block();

        Tweet tweet2 = Tweet.builder().author("carl").content("Hello rsocket get tweet2").build();
        rSocketRequester
            .route("addTweet")
            .data(tweet2)
            .retrieveMono(String.class)
            .doOnNext(s -> System.out.println("ID found: " + s))
            .block();

        // when
        rSocketRequester
            .route("streamOfTweet")
            .retrieveFlux(Tweet.class)
            .doOnNext(System.out::println)
            .subscribe(tweet1 -> System.out.println("success"));
    }

    @DisplayName("Get a channel of tweet")
    @Test
    void shouldGetAChannelOfTweet() {
        // given
        Flux<Tweet> tweets = Flux.range(0, 5)
            .map(i -> Tweet.builder().author("carl").content("Hello rsocket get tweet " + i).build())
            .delayElements(Duration.ofMillis(100))
            .doOnNext(System.out::println);

        // when
        Flux<List<Tweet>> mono = rSocketRequester
            .route("channelOfTweet")
            .data(tweets)
            .retrieveFlux(new ParameterizedTypeReference<List<Tweet>>() {
            })
            .doOnNext(System.out::println);

        // then
        StepVerifier.create(mono)
            .expectNextMatches(t -> {
                assertThat(t).hasSize(1);
                return true;
            })
            .expectNextMatches(t -> {
                assertThat(t).hasSize(2);
                return true;
            })
            .expectNextMatches(t -> {
                assertThat(t).hasSize(3);
                return true;
            })
            .expectNextMatches(t -> {
                assertThat(t).hasSize(4);
                return true;
            })
            .expectNextMatches(t -> {
                assertThat(t).hasSize(5);
                return true;
            })
            .verifyComplete();
    }
}
