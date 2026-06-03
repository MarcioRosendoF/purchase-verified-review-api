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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PurchaseValidatorService purchaseValidatorService;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    public void shouldThrowExceptionWhenAuthorNotFound() {
        UUID authorId = UUID.randomUUID();
        CreateReviewRequest request = new CreateReviewRequest();
        request.setProductId(UUID.randomUUID());
        request.setRating(5);
        request.setComment("Excelente");

        when(userRepository.findById(authorId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.create(request, authorId);
        });

        assertEquals("Usuário não encontrado", exception.getMessage());
        verifyNoInteractions(productRepository, purchaseValidatorService, reviewRepository);
    }

    @Test
    public void shouldThrowExceptionWhenProductNotFound() {
        UUID authorId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        CreateReviewRequest request = new CreateReviewRequest();
        request.setProductId(productId);
        request.setRating(5);
        request.setComment("Excelente");
        User author = new User();
        author.setId(authorId);

        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(productRepository.findByIdAndDeletedAtIsNull(productId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.create(request, authorId);
        });

        assertEquals("Produto não encontrado", exception.getMessage());
        verifyNoInteractions(purchaseValidatorService, reviewRepository);
    }

    @Test
    public void shouldThrowExceptionWhenUserHasNotPurchasedProduct() {
        UUID authorId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        CreateReviewRequest request = new CreateReviewRequest();
        request.setProductId(productId);
        request.setRating(5);
        request.setComment("Excelente");

        User author = new User();
        author.setId(authorId);

        Product product = new Product();
        product.setId(productId);

        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(productRepository.findByIdAndDeletedAtIsNull(productId)).thenReturn(Optional.of(product));
        when(purchaseValidatorService.hasUserPurchasedProduct(authorId, productId)).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reviewService.create(request, authorId);
        });

        assertEquals("Usuário não realizou compra deste produto", exception.getMessage());
        verifyNoInteractions(reviewRepository);
    }

    @Test
    public void shouldCreateReviewSuccessfully() {
        UUID authorId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        CreateReviewRequest request = new CreateReviewRequest();
        request.setProductId(productId);
        request.setRating(5);
        request.setComment("Excelente");

        User author = new User();
        author.setId(authorId);
        author.setName("Marcio");

        Product product = new Product();
        product.setId(productId);

        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(productRepository.findByIdAndDeletedAtIsNull(productId)).thenReturn(Optional.of(product));
        when(purchaseValidatorService.hasUserPurchasedProduct(authorId, productId)).thenReturn(true);

        ReviewResponse response = reviewService.create(request, authorId);

        assertNotNull(response);
        assertEquals("Marcio", response.getAuthorName());
        assertEquals(5, response.getRating());
        assertEquals("Excelente", response.getComment());

        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        Review savedReview = reviewCaptor.getValue();
        assertEquals(author, savedReview.getAuthor());
        assertEquals(product, savedReview.getProduct());
        assertEquals(5, savedReview.getRating());
        assertEquals("Excelente", savedReview.getComment());
    }

    @Test
    public void shouldThrowExceptionOnListWhenProductNotFound() {
        UUID productId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        when(productRepository.findByIdAndDeletedAtIsNull(productId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.listByProduct(productId, pageable);
        });

        assertEquals("Produto não encontrado", exception.getMessage());
        verifyNoInteractions(reviewRepository);
    }

    @Test
    public void shouldListReviewsSuccessfully() {
        UUID productId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        Product product = new Product();
        product.setId(productId);

        User author = new User();
        author.setName("Marcio");

        Review review = new Review();
        review.setId(UUID.randomUUID());
        review.setProduct(product);
        review.setAuthor(author);
        review.setRating(5);
        review.setComment("Otimo");
        review.setCreatedAt(Instant.now());

        Page<Review> reviewPage = new PageImpl<>(Collections.singletonList(review), pageable, 1);

        when(productRepository.findByIdAndDeletedAtIsNull(productId)).thenReturn(Optional.of(product));
        when(reviewRepository.findByProductIdAndProductDeletedAtIsNull(productId, pageable)).thenReturn(reviewPage);

        Page<ReviewResponse> result = reviewService.listByProduct(productId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        ReviewResponse response = result.getContent().get(0);
        assertEquals("Marcio", response.getAuthorName());
        assertEquals(5, response.getRating());
        assertEquals("Otimo", response.getComment());

        verify(productRepository).findByIdAndDeletedAtIsNull(productId);
        verify(reviewRepository).findByProductIdAndProductDeletedAtIsNull(productId, pageable);
    }
}
