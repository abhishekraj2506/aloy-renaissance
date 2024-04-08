package com.aloy.sellerbppservice.service;

import com.aloy.sellerbppservice.model.Orders;

public interface OrdersService {

    Orders save(Orders orders);

    Orders getByOndcTransactionId(String ondcTransactionId);
}
