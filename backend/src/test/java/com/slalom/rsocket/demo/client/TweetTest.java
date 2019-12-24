package com.slalom.rsocket.demo.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slalom.rsocket.demo.domain.Tweet;
import com.slalom.rsocket.demo.repository.TweetInMemoryRepository;
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

import javax.annotation.PostConstruct;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@DisplayName("Test tweets")
@SpringBootTest
public class TweetTest {

    private static final Logger LOG = java.util.logging.Logger.getLogger(TweetTest.class.getName());

    private static final String ID_REGEX = "[\\w\\d]{8}-[\\w\\d]{4}-[\\w\\d]{4}-[\\w\\d]{4}-[\\w\\d]{12}";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final MediaType[] SUPPORTED_TYPES = {MediaType.APPLICATION_JSON, new MediaType("application", "*+json")};

    private RSocketRequester rSocketRequester;
    @Autowired
    private TweetInMemoryRepository inMemoryRepository;

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

    @BeforeEach
    void beforeEach() {
        inMemoryRepository.reset();
    }

    @DisplayName("Add a tweet")
    @Test
    void shouldAddATweet() {
        // given
        Tweet tweet = buildTweet("Hello rsocket");

        // when
        Mono<Tweet> mono = rSocketRequester.route("addTweet").data(tweet).retrieveMono(Tweet.class);

        // then
        StepVerifier.create(mono)
            .assertNext(result -> {
                assertThat(result.getId()).isNotBlank();
                assertThat(result.getId()).matches(ID_REGEX);
                assertThat(result.getAuthor()).isEqualTo("carl");
                assertThat(result.getContent()).isEqualTo("Hello rsocket");
            })
            .verifyComplete();
    }

    @DisplayName("Get a tweet")
    @Test
    void shouldGetATweet() {
        // given
        final Tweet tweet = saveTweet("Hello rsocket get tweet");

        // when
        Mono<Tweet> mono = rSocketRequester.route("getTweet").data(tweet.getId()).retrieveMono(Tweet.class);

        // then
        StepVerifier.create(mono)
            .assertNext(t -> {
                assertThat(t.getId()).isEqualTo(tweet.getId());
                assertThat(t.getAuthor()).isEqualTo("carl");
                assertThat(t.getContent()).isEqualTo("Hello rsocket get tweet");
            })
            .verifyComplete();
    }

    @DisplayName("Get a stream of tweet")
    @Test
    void shouldGetAStreamOfTweet() {
        // given
        saveTweet("Hello rsocket get tweet");
        saveTweet("Hello rsocket get tweet2");

        // when
        Flux<Tweet> flux = rSocketRequester.route("streamOfTweet").retrieveFlux(Tweet.class);

        // then
        StepVerifier.create(flux)
            .expectNextCount(2)
            .verifyComplete();
    }

    @DisplayName("Get a channel of tweet")
    @Test
    void shouldGetAChannelOfTweet() {
        // given
        saveTweet("My first tweet to save in the DB");
        saveTweet("My second tweet to save in the DB");
        Flux<Tweet> tweets = Flux.range(0, 2)
            .map(i -> buildTweet("Send flux of tweet #" + i))
            .doOnNext(tweet -> LOG.info("[Client] Sending: " + tweet));
        new Thread(() -> {
            try {
                Thread.sleep(1000L);
                saveTweet("Continue tweeting after channel is open");
            } catch (InterruptedException e) {
                fail("Threading issue", e);
            }
        }).start();

        // when
        Flux<List<Tweet>> flux = rSocketRequester
            .route("channelOfTweet")
            .data(tweets)
            .retrieveFlux(new ParameterizedTypeReference<>() {
            });

        // then
        StepVerifier.create(flux.doOnNext(tweet -> LOG.info("[Client] Receiving: " + tweet)))
            .assertNext(res -> assertThat(res).hasSize(3))
            .assertNext(res -> assertThat(res).hasSize(4))
            .assertNext(res -> assertThat(res).hasSize(5))
            .verifyTimeout(Duration.ofMillis(2000L));
    }

    private Tweet saveTweet(String content) {
        return rSocketRequester.route("addTweet")
            .data(buildTweet(content))
            .retrieveMono(Tweet.class)
            .block();
    }

    private Tweet buildTweet(String content) {
        return Tweet.builder().author("carl").content(content).build();
    }
}
