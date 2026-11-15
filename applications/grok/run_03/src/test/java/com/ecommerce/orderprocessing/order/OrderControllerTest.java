package com.ecommerce.orderprocessing.order;

import com.ecommerce.orderprocessing.exception.GlobalExceptionHandler;
import com.ecommerce.orderprocessing.exception.OrderNotFoundException;
import com.ecommerce.orderprocessing.model.CustomerOrder;
import com.ecommerce.orderprocessing.model.OrderItem;
import com.ecommerce.orderprocessing.model.OrderStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Test
    void createOrderReturnsCreated() throws Exception {
        CustomerOrder order = sampleOrder();
        when(orderService.createOrder(any())).thenReturn(order);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "CUST-1",
                                  "customerEmail": "alice@example.com",
                                  "paymentMethod": "CREDIT_CARD",
                                  "items": [
                                    {
                                      "productId": "PROD-001",
                                      "productName": "Laptop",
                                      "quantity": 1,
                                      "unitPrice": 999.99
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value("order-1"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void createOrderValidatesPayload() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"customerId":"","customerEmail":"bad","paymentMethod":"","items":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOrderReturnsNotFound() throws Exception {
        when(orderService.getOrder("missing")).thenThrow(new OrderNotFoundException("missing"));
        mockMvc.perform(get("/api/orders/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Order not found: missing"));
    }

    private CustomerOrder sampleOrder() {
        return new CustomerOrder(
                "order-1",
                "CUST-1",
                "alice@example.com",
                List.of(new OrderItem("PROD-001", "Laptop", 1, new BigDecimal("999.99"))),
                new BigDecimal("999.99"),
                "CREDIT_CARD",
                OrderStatus.CREATED,
                null,
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-01-01T00:00:00Z")
        );
    }
}
