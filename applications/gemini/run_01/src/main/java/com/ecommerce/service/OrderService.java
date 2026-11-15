package com.ecommerce.service;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.event.InventoryEvent;
import com.ecommerce.event.InventoryStatus;
import com.ecommerce.event.OrderEvent;
import com.ecommerce.event.PaymentEvent;
import com.ecommerce.event.PaymentStatus;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String orderEventsTopic;

    public OrderService(OrderRepository orderRepository,
                        KafkaTemplate<String, Object> kafkaTemplate,
                        @Value("${app.kafka.topics.order-events}") String orderEventsTopic) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.orderEventsTopic = orderEventsTopic;
    }

    /**
     * FR1a: Create and store a customer order.
     * FR2: Publish an order-created event.
     */
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Order order = new Order(
                request.customerId(),
                request.productId(),
                request.quantity(),
                request.totalAmount()
        );
        Order savedOrder = orderRepository.save(order);
        log.info("Order created and stored in DB with ID: {}", savedOrder.getId());

        OrderEvent event = OrderEvent.created(
                savedOrder.getId(),
                savedOrder.getCustomerId(),
                savedOrder.getProductId(),
                savedOrder.getQuantity(),
                savedOrder.getTotalAmount()
        );

        kafkaTemplate.send(orderEventsTopic, savedOrder.getId().toString(), event);
        log.info("Published OrderEvent (CREATED) to topic {} for orderId: {}", orderEventsTopic, savedOrder.getId());

        return savedOrder;
    }

    public Optional<Order> getOrder(UUID orderId) {
        return orderRepository.findById(orderId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Updates order status based on payment processing results.
     * FR6: Handle payment failure by marking order as PAYMENT_FAILED.
     */
    @Transactional
    @KafkaListener(topics = "${app.kafka.topics.payment-events}", groupId = "order-service-group")
    public void handlePaymentEvent(PaymentEvent paymentEvent) {
        log.info("OrderService received PaymentEvent for orderId: {}, status: {}",
                paymentEvent.orderId(), paymentEvent.status());

        orderRepository.findById(paymentEvent.orderId()).ifPresent(order -> {
            if (paymentEvent.status() == PaymentStatus.SUCCESS) {
                if (order.getStatus() == OrderStatus.CREATED) {
                    order.setStatus(OrderStatus.PAYMENT_COMPLETED);
                    orderRepository.save(order);
                    log.info("Order {} updated to PAYMENT_COMPLETED", order.getId());
                }
            } else if (paymentEvent.status() == PaymentStatus.FAILED) {
                order.setStatus(OrderStatus.PAYMENT_FAILED);
                order.setFailureReason(paymentEvent.reason());
                orderRepository.save(order);
                log.warn("Order {} marked as PAYMENT_FAILED. Reason: {}", order.getId(), paymentEvent.reason());
            }
        });
    }

    /**
     * Updates order status based on inventory reservation results.
     * FR6: Handle inventory shortage failure by marking order as FAILED_OUT_OF_STOCK.
     */
    @Transactional
    @KafkaListener(topics = "${app.kafka.topics.inventory-events}", groupId = "order-service-group")
    public void handleInventoryEvent(InventoryEvent inventoryEvent) {
        log.info("OrderService received InventoryEvent for orderId: {}, status: {}",
                inventoryEvent.orderId(), inventoryEvent.status());

        orderRepository.findById(inventoryEvent.orderId()).ifPresent(order -> {
            if (inventoryEvent.status() == InventoryStatus.RESERVED) {
                order.setStatus(OrderStatus.COMPLETED);
                orderRepository.save(order);
                log.info("Order {} processing completed successfully (COMPLETED)", order.getId());
            } else if (inventoryEvent.status() == InventoryStatus.OUT_OF_STOCK) {
                order.setStatus(OrderStatus.FAILED_OUT_OF_STOCK);
                order.setFailureReason(inventoryEvent.reason());
                orderRepository.save(order);
                log.warn("Order {} marked as FAILED_OUT_OF_STOCK. Reason: {}", order.getId(), inventoryEvent.reason());
            }
        });
    }
}
