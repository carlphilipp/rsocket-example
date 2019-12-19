package com.slalom.rsocket.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slalom.rsocket.demo.domain.Tweet;
import org.reactivestreams.Publisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.CharSequenceEncoder;
import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.StringDecoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Configuration
public class RSocketConfig {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /*@Bean
    public RSocketStrategies rsocketStrategies() {
        return RSocketStrategies.builder()
            .decoder(new Jackson2JsonDecoder())
            //.decoder(StringDecoder.textPlainOnly())
            //.decoder(String)
            *//*.decoder(new Decoder<Tweet>() {
                @Override
                public boolean canDecode(ResolvableType elementType, MimeType mimeType) {
                    return true;
                }

                @Override
                public Flux<Tweet> decode(Publisher<DataBuffer> inputStream, ResolvableType elementType, MimeType mimeType, Map<String, Object> hints) {
                    return Flux.from(inputStream)
                        .map(dataBuffer -> {
                            try {
                                return objectMapper.readValue(dataBuffer.asByteBuffer().array(), Tweet.class);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                }

                @Override
                public Mono<Tweet> decodeToMono(Publisher<DataBuffer> inputStream, ResolvableType elementType, MimeType mimeType, Map<String, Object> hints) {
                    return Mono.from(inputStream)
                        .map(dataBuffer -> {
                            try {
                                final Tweet tweet = objectMapper.readValue(dataBuffer.asByteBuffer().array(), Tweet.class);
                                return tweet;
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                }

                @Override
                public List<MimeType> getDecodableMimeTypes() {
                    return null;
                }
            })
            .encoder(CharSequenceEncoder.allMimeTypes())*//*
            .dataBufferFactory(new DefaultDataBufferFactory(true))
            .build();
    }*/
}
