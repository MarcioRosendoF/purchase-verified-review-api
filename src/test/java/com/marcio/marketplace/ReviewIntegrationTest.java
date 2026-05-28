package com.marcio.marketplace;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcio.marketplace.dto.request.*;
import com.marcio.marketplace.dto.response.AuthResponse;
import com.marcio.marketplace.dto.response.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class ReviewIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String customerToken;
    private String productId;

    @BeforeEach
    void setup() throws Exception {
        RegisterRequest customerReq = new RegisterRequest();
        customerReq.setName("Cliente Teste");
        customerReq.setEmail("cliente" + System.currentTimeMillis() + "@email.com");
        customerReq.setPassword("senha123");

        MvcResult registerResult = mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(customerReq)))
            .andExpect(status().isCreated())
            .andReturn();

        AuthResponse customerAuth = objectMapper.readValue(
            registerResult.getResponse().getContentAsString(), AuthResponse.class
        );
        customerToken = customerAuth.getToken();

        LoginRequest adminLogin = new LoginRequest();
        adminLogin.setEmail("admin@marketplace.com");
        adminLogin.setPassword("admin123");

        MvcResult adminResult = mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(adminLogin)))
            .andExpect(status().isOk())
            .andReturn();

        AuthResponse adminAuth = objectMapper.readValue(
            adminResult.getResponse().getContentAsString(), AuthResponse.class
        );
        adminToken = adminAuth.getToken();

        CreateProductRequest productReq = new CreateProductRequest();
        productReq.setName("Produto Teste");
        productReq.setDescription("Descrição do produto teste");
        productReq.setPrice(new BigDecimal("99.90"));

        MvcResult productResult = mockMvc.perform(post("/products")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + adminToken)
            .content(objectMapper.writeValueAsString(productReq)))
            .andExpect(status().isCreated())
            .andReturn();

        ProductResponse product = objectMapper.readValue(
            productResult.getResponse().getContentAsString(), ProductResponse.class
        );
        productId = product.getId().toString();
    }

    @Test
    void postReview_semCompra_retorna403() throws Exception {
        CreateReviewRequest reviewReq = new CreateReviewRequest();
        reviewReq.setProductId(UUID.fromString(productId));
        reviewReq.setRating(5);
        reviewReq.setComment("Muito bom!");

        mockMvc.perform(post("/reviews")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + customerToken)
            .content(objectMapper.writeValueAsString(reviewReq)))
            .andExpect(status().isForbidden());
    }

    @Test
    void postReview_duplicada_retorna409() throws Exception {
        CreateOrderRequest orderReq = new CreateOrderRequest();
        orderReq.setProductId(UUID.fromString(productId));
        orderReq.setQuantity(1);

        mockMvc.perform(post("/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + customerToken)
            .content(objectMapper.writeValueAsString(orderReq)))
            .andExpect(status().isCreated());

        CreateReviewRequest reviewReq = new CreateReviewRequest();
        reviewReq.setProductId(UUID.fromString(productId));
        reviewReq.setRating(4);
        reviewReq.setComment("Gostei!");

        mockMvc.perform(post("/reviews")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + customerToken)
            .content(objectMapper.writeValueAsString(reviewReq)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/reviews")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + customerToken)
            .content(objectMapper.writeValueAsString(reviewReq)))
            .andExpect(status().isConflict());
    }

    @Test
    void getProducts_apossSoftDelete_produtoNaoAparece() throws Exception {
        mockMvc.perform(delete("/products/" + productId)
            .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/products")
            .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[?(@.id == '" + productId + "')]").doesNotExist());
    }

    @Test
    void register_emailDuplicado_retornaErro() throws Exception {
        String email = "duplicado" + System.currentTimeMillis() + "@email.com";
        RegisterRequest req1 = new RegisterRequest();
        req1.setName("User 1");
        req1.setEmail(email);
        req1.setPassword("senha123");

        mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req1)))
            .andExpect(status().isCreated());

        RegisterRequest req2 = new RegisterRequest();
        req2.setName("User 2");
        req2.setEmail(email);
        req2.setPassword("senha456");

        mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req2)))
            .andExpect(status().isForbidden());
    }

    @Test
    void login_credenciaisInvalidas_retorna403() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("usuario_inexistente@email.com");
        req.setPassword("senha_errada");

        mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isForbidden());
    }

    @Test
    void register_response_naoContemSenha() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setName("Sem Senha Resp");
        req.setEmail("semsenha" + System.currentTimeMillis() + "@email.com");
        req.setPassword("senhaSegura123");

        MvcResult result = mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andReturn();

        String body = result.getResponse().getContentAsString();
        org.junit.jupiter.api.Assertions.assertFalse(body.contains("password"));
        org.junit.jupiter.api.Assertions.assertFalse(body.contains("$2a$"));
    }
}
