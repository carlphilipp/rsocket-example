package com.slalom.rsocket.demo.config;

import com.slalom.rsocket.demo.domain.Tweet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class MongoConfig {

    @Autowired
    private MongoOperations operations;

    @EventListener(ApplicationReadyEvent.class)
    public void createCappedCollection() {
        operations.createCollection(
            "tweets",
            CollectionOptions.empty().capped().size(5242880).maxDocuments(5000)
        );
        operations.insert(Tweet.builder().id(UUID.randomUUID().toString()).author("admin").content("Hello world!").build(), "tweets");
    }
}
