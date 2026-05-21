package com.marcio.marketplace.service;

import com.marcio.marketplace.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseValidatorService {

    private final OrderRepository orderRepository;

    public boolean hasUserPurchasedProduct(UUID userId, UUID productId) {
        return orderRepository.existsByBuyerIdAndProductId(userId, productId);
    }
}
