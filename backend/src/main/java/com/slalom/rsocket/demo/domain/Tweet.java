package com.slalom.rsocket.demo.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Document
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Tweet implements Serializable {
    @Id
    private String id;
    private String author;
    private String content;
}
