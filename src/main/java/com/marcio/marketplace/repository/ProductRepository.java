package com.marcio.marketplace.repository;

import com.marcio.marketplace.entity.Product;
import com.marcio.marketplace.repository.projection.ProductSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query(
        value = """
            SELECT p AS product,
                   COALESCE(AVG(r.rating), 0.0) AS averageRating,
                   COUNT(r) AS reviewCount
            FROM Product p
            LEFT JOIN Review r ON r.product = p
            WHERE p.deletedAt IS NULL
            GROUP BY p.id
            """,
        countQuery = """
            SELECT COUNT(p)
            FROM Product p
            WHERE p.deletedAt IS NULL
            """
    )
    Page<ProductSummary> findAllActiveWithStats(Pageable pageable);

    @Query(
        value = """
            SELECT p AS product,
                   COALESCE(AVG(r.rating), 0.0) AS averageRating,
                   COUNT(r) AS reviewCount
            FROM Product p
            LEFT JOIN Review r ON r.product = p
            WHERE p.id = :id AND p.deletedAt IS NULL
            GROUP BY p.id
            """
    )
    Optional<ProductSummary> findActiveByIdWithStats(@org.springframework.data.repository.query.Param("id") UUID id);

    Optional<Product> findByIdAndDeletedAtIsNull(UUID id);
}
