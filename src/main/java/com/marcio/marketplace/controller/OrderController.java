package com.marcio.marketplace.controller;

import com.marcio.marketplace.dto.request.CreateOrderRequest;
import com.marcio.marketplace.dto.response.OrderResponse;
import com.marcio.marketplace.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponse> create(
        @Valid @RequestBody CreateOrderRequest request,
        @AuthenticationPrincipal com.marcio.marketplace.entity.User user
    ) {
        return ResponseEntity.status(201).body(orderService.create(request, user.getId()));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Page<OrderResponse>> myOrders(
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable,
        @AuthenticationPrincipal com.marcio.marketplace.entity.User user
    ) {
        return ResponseEntity.ok(orderService.listMyOrders(user.getId(), pageable));
    }
}
