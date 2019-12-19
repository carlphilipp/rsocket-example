package com.slalom.rsocket.demo.config;

import com.slalom.rsocket.demo.domain.Tweet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Configuration
public class RedisConfig {

    private RedisServer redisServer;

    @Bean
    LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory("localhost", 6379);
    }

    @Bean
    ReactiveRedisOperations<String, Tweet> redisOperations(ReactiveRedisConnectionFactory factory) {
        Jackson2JsonRedisSerializer<Tweet> serializer = new Jackson2JsonRedisSerializer<>(Tweet.class);
        RedisSerializationContext.RedisSerializationContextBuilder<String, Tweet> builder = RedisSerializationContext.newSerializationContext(new StringRedisSerializer());

        RedisSerializationContext<String, Tweet> context = builder.value(serializer).build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

    @PostConstruct
    public void postConstruct() {
        redisServer = new RedisServer();
        redisServer.start();
    }

    @PreDestroy
    public void flushDb() {
        redisServer.stop();
    }
}
