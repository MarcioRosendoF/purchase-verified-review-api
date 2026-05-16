package com.marcio.marketplace.repository;

import com.marcio.marketplace.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    Page<Order> findByBuyerId(UUID buyerId, Pageable pageable);
    boolean existsByBuyerIdAndProductId(UUID buyerId, UUID productId);
}
