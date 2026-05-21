package com.marcio.marketplace.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

@Getter
public class CreateOrderRequest {

    @NotNull(message = "ID do produto é obrigatório")
    private UUID productId;

    @Min(value = 1, message = "Quantidade deve ser ao menos 1")
    private Integer quantity = 1;
}
