package com.ecommerce.order;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.ecommerce.exception.GlobalExceptionHandler;
import com.ecommerce.exception.OrderNotFoundException;
import com.ecommerce.model.CustomerOrder;
import com.ecommerce.model.OrderItem;

@WebMvcTest(controllers = OrderController.class)
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    void createOrderReturnsCreated() throws Exception {
        CustomerOrder order = CustomerOrder.create(
                "cust-1",
                "buyer@example.com",
                List.of(new OrderItem("SKU-001", "Headphones", 1, new BigDecimal("79.99"))),
                "CARD"
        );
        when(orderService.createOrder(any())).thenReturn(order);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "cust-1",
                                  "customerEmail": "buyer@example.com",
                                  "paymentMethod": "CARD",
                                  "items": [{"sku": "SKU-001", "quantity": 1}]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(order.getOrderId()))
                .andExpect(jsonPath("$.customerId").value("cust-1"));
    }

    @Test
    void createOrderValidatesPayload() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"\",\"customerEmail\":\"bad\",\"items\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOrderReturnsNotFound() throws Exception {
        when(orderService.getOrder("missing")).thenThrow(new OrderNotFoundException("missing"));
        mockMvc.perform(get("/api/orders/missing")).andExpect(status().isNotFound());
    }
}
