package com.marcio.marketplace.service;

import com.marcio.marketplace.dto.request.CreateProductRequest;
import com.marcio.marketplace.dto.response.ProductResponse;
import com.marcio.marketplace.entity.Product;
import com.marcio.marketplace.entity.User;
import com.marcio.marketplace.exception.ResourceNotFoundException;
import com.marcio.marketplace.repository.ProductRepository;
import com.marcio.marketplace.repository.UserRepository;
import com.marcio.marketplace.repository.projection.ProductSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<ProductResponse> listActive(Pageable pageable) {
        return productRepository.findAllActiveWithStats(pageable)
            .map(summary -> new ProductResponse(
                summary.getProduct().getId(),
                summary.getProduct().getName(),
                summary.getProduct().getDescription(),
                summary.getProduct().getPrice(),
                summary.getAverageRating(),
                summary.getReviewCount(),
                summary.getProduct().getCreatedAt()
            ));
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(UUID id) {
        ProductSummary summary = productRepository.findActiveByIdWithStats(id)
            .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));

        return new ProductResponse(
            summary.getProduct().getId(),
            summary.getProduct().getName(),
            summary.getProduct().getDescription(),
            summary.getProduct().getPrice(),
            summary.getAverageRating(),
            summary.getReviewCount(),
            summary.getProduct().getCreatedAt()
        );
    }

    @Transactional
    public ProductResponse create(CreateProductRequest request, UUID createdByUserId) {
        User creator = userRepository.findById(createdByUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCreatedBy(creator);

        productRepository.save(product);

        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            0.0,
            0L,
            product.getCreatedAt()
        );
    }

    @Transactional
    public void softDelete(UUID id) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));

        product.setDeletedAt(Instant.now());
        productRepository.save(product);
    }
}
