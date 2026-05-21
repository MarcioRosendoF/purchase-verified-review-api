package com.marcio.marketplace.controller;

import com.marcio.marketplace.dto.request.CreateReviewRequest;
import com.marcio.marketplace.dto.response.ReviewResponse;
import com.marcio.marketplace.service.ReviewService;
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

import java.util.UUID;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ReviewResponse> create(
        @Valid @RequestBody CreateReviewRequest request,
        @AuthenticationPrincipal com.marcio.marketplace.entity.User user
    ) {
        return ResponseEntity.status(201).body(reviewService.create(request, user.getId()));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<ReviewResponse>> listByProduct(
        @PathVariable UUID productId,
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable
    ) {
        return ResponseEntity.ok(reviewService.listByProduct(productId, pageable));
    }
}
