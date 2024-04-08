package com.aloy.sellerbppservice.service.impl;

import com.aloy.sellerbppservice.model.Orders;
import com.aloy.sellerbppservice.repos.OrdersRepository;
import com.aloy.sellerbppservice.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrdersServiceImpl implements OrdersService {

    @Autowired
    private OrdersRepository ordersRepository;


    @Override
    public Orders save(Orders orders) {
        return ordersRepository.save(orders);
    }

    @Override
    public Orders getByOndcTransactionId(String ondcTransactionId) {
        return ordersRepository.findByOndcTransactionId(ondcTransactionId);
    }
}
