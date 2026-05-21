package com.marcio.marketplace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReviewResponse {
    private UUID id;
    private String authorName;
    private Integer rating;
    private String comment;
    private Instant createdAt;
}
