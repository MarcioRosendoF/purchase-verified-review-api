package com.marcio.marketplace.service;

import com.marcio.marketplace.dto.request.CreateOrderRequest;
import com.marcio.marketplace.dto.response.OrderResponse;
import com.marcio.marketplace.entity.Order;
import com.marcio.marketplace.entity.Product;
import com.marcio.marketplace.entity.User;
import com.marcio.marketplace.exception.ResourceNotFoundException;
import com.marcio.marketplace.repository.OrderRepository;
import com.marcio.marketplace.repository.ProductRepository;
import com.marcio.marketplace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderResponse create(CreateOrderRequest request, UUID buyerId) {
        User buyer = userRepository.findById(buyerId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        Product product = productRepository.findByIdAndDeletedAtIsNull(request.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));

        Order order = new Order();
        order.setBuyer(buyer);
        order.setProduct(product);
        order.setQuantity(request.getQuantity());
        order.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));

        orderRepository.save(order);

        log.info("Pedido criado com sucesso. ID: {}, Comprador: {}, Produto: {}, Quantidade: {}, Preço Total: {}", 
            order.getId(), buyer.getId(), product.getId(), order.getQuantity(), order.getTotalPrice());

        return new OrderResponse(
            order.getId(),
            product.getId(),
            product.getName(),
            order.getQuantity(),
            order.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> listMyOrders(UUID buyerId, Pageable pageable) {
        return orderRepository.findByBuyerId(buyerId, pageable)
            .map(order -> new OrderResponse(
                order.getId(),
                order.getProduct().getId(),
                order.getProduct().getName(),
                order.getQuantity(),
                order.getCreatedAt()
            ));
    }
}
