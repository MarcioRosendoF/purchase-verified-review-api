package com.marcio.marketplace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class OrderResponse {
    private UUID id;
    private UUID productId;
    private String productName;
    private Integer quantity;
    private Instant createdAt;
}
