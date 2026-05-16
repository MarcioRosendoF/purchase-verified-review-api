package com.marcio.marketplace.repository.projection;

import com.marcio.marketplace.entity.Product;

public interface ProductSummary {
    Product getProduct();
    Double getAverageRating();
    Long getReviewCount();
}
