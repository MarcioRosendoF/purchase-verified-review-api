CREATE TABLE reviews (
    id UUID PRIMARY KEY,
    author_id UUID NOT NULL,
    product_id UUID NOT NULL,
    rating INTEGER NOT NULL,
    comment TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_reviews_author FOREIGN KEY (author_id) REFERENCES users(id),
    CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT uk_reviews_author_product UNIQUE (author_id, product_id)
);

CREATE INDEX idx_reviews_product_id ON reviews(product_id);
