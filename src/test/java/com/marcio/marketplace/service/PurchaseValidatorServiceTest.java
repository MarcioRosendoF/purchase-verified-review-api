package com.marcio.marketplace.service;

import com.marcio.marketplace.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PurchaseValidatorServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PurchaseValidatorService purchaseValidatorService;

    @Test
    public void shouldReturnTrueWhenPurchaseExists() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        when(orderRepository.existsByBuyerIdAndProductId(userId, productId)).thenReturn(true);

        boolean result = purchaseValidatorService.hasUserPurchasedProduct(userId, productId);

        assertTrue(result);
        verify(orderRepository).existsByBuyerIdAndProductId(userId, productId);
    }

    @Test
    public void shouldReturnFalseWhenPurchaseDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        when(orderRepository.existsByBuyerIdAndProductId(userId, productId)).thenReturn(false);

        boolean result = purchaseValidatorService.hasUserPurchasedProduct(userId, productId);

        assertFalse(result);
        verify(orderRepository).existsByBuyerIdAndProductId(userId, productId);
    }
}
