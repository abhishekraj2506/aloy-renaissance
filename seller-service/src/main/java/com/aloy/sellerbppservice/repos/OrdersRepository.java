package com.aloy.sellerbppservice.repos;

import com.aloy.sellerbppservice.model.Orders;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrdersRepository extends MongoRepository<Orders, String> {

    Orders findByOndcTransactionId(String ondcTxnId);
}
