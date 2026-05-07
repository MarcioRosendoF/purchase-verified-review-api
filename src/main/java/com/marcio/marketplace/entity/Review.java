package com.marcio.marketplace.entity;

import com.marcio.marketplace.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "reviews",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reviews_author_product",
                        columnNames = {"author_id", "product_id"}
                )
        },
        indexes = {
                @Index(name = "idx_reviews_product_id", columnList = "product_id")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer rating; // 1 a 5

    @Column(columnDefinition = "TEXT")
    private String comment;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}