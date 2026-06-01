package com.marcio.marketplace.service;

import com.marcio.marketplace.dto.request.CreateReviewRequest;
import com.marcio.marketplace.dto.response.ReviewResponse;
import com.marcio.marketplace.entity.Product;
import com.marcio.marketplace.entity.Review;
import com.marcio.marketplace.entity.User;
import com.marcio.marketplace.exception.BusinessException;
import com.marcio.marketplace.exception.ResourceNotFoundException;
import com.marcio.marketplace.repository.ProductRepository;
import com.marcio.marketplace.repository.ReviewRepository;
import com.marcio.marketplace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PurchaseValidatorService purchaseValidatorService;

    @Transactional
    public ReviewResponse create(CreateReviewRequest request, UUID authorId) {
        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        Product product = productRepository.findByIdAndDeletedAtIsNull(request.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));

        if (!purchaseValidatorService.hasUserPurchasedProduct(authorId, product.getId())) {
            log.warn("Tentativa de criar review negada. Usuário {} não realizou compra do produto {}.", authorId, product.getId());
            throw new BusinessException("Usuário não realizou compra deste produto");
        }

        Review review = new Review();
        review.setAuthor(author);
        review.setProduct(product);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        reviewRepository.save(review);

        log.info("Review criada com sucesso. ID: {}, Autor: {}, Produto: {}, Nota: {}", 
            review.getId(), author.getId(), product.getId(), review.getRating());

        return new ReviewResponse(
            review.getId(),
            author.getName(),
            review.getRating(),
            review.getComment(),
            review.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> listByProduct(UUID productId, Pageable pageable) {
        productRepository.findByIdAndDeletedAtIsNull(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));

        return reviewRepository.findByProductIdAndProductDeletedAtIsNull(productId, pageable)
            .map(r -> new ReviewResponse(
                r.getId(),
                r.getAuthor().getName(),
                r.getRating(),
                r.getComment(),
                r.getCreatedAt()
            ));
    }
}
