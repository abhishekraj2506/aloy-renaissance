package com.aloy.coreapp.service.impl;

import com.aloy.coreapp.dto.OndcConfirmResponseDTO;
import com.aloy.coreapp.dto.SavedOrderDTO;
import com.aloy.coreapp.enums.OrderStatus;
import com.aloy.coreapp.model.Orders;
import com.aloy.coreapp.repos.OrderRepository;
import com.aloy.coreapp.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ExecutorService executorService;

    @Override
    public SavedOrderDTO saveOrder(OndcConfirmResponseDTO.Order ondcOrder, String userId, int points) {
        Optional<Orders> ordersOptional = orderRepository.findBySellerOrderId(ondcOrder.getId());
        if(ordersOptional.isPresent()) return SavedOrderDTO.builder().order(ordersOptional.get())
                .newOrder(false).build();

        Orders orders = new Orders();
        ondcOrder.getItems().forEach(i -> {
            i.setDescriptor(null);
        });
        orders.setUserId(userId);
        orders.setPoints(points);
        orders.setSellerOrderId(ondcOrder.getId());
        orders.setItems(ondcOrder.getItems());
        orders.setBilling(ondcOrder.getBilling());
        orders.setFulfillments(ondcOrder.getFulfillments());
        orders.setProvider(ondcOrder.getProvider());
        orders.setQuote(ondcOrder.getQuote());
        orders.setCancellationTerms(ondcOrder.getCancellation_terms());
        orders.setPayments(ondcOrder.getPayments());
        orders.setOrderStatus(OrderStatus.ACITVE);
        orders = orderRepository.save(orders);
        executorService.submit(new OrderStatusHandler(orders.getId(), orderRepository));
        return SavedOrderDTO.builder().newOrder(true).order(orders).build();
    }

    @Override
    public int getUserOrderCount(String userId) {
        return orderRepository.findAllByUserId(userId).size();
    }

    static class OrderStatusHandler implements Runnable {
        private final String orderId;
        private final OrderRepository repository;

        public OrderStatusHandler(String param1, OrderRepository param2) {
            this.orderId = param1;
            this.repository = param2;
        }

        @Override
        public void run() {
           log.info("Order status handler started for order id: {}", orderId);
            try {
                log.info("Sleeping for 30 seconds for order id: {}", orderId);
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                log.error("Error while sleeping", e);
            }
            log.info("Changing order status to COMPLETED");
            Optional<Orders> orders = repository.findById(orderId);
            orders.ifPresent(o -> {
                o.setOrderStatus(OrderStatus.COMPLETED);
                repository.save(o);
            });
        }
    }
}
