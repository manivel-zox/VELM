package com.velmart.repository;

import com.velmart.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, String> {

    List<OrderItem> findByOrderId(String orderId);

    List<OrderItem> findBySellerIdOrderByCreatedAtDesc(String sellerId);

    default List<OrderItem> findBySellerId(String sellerId) {
        return findBySellerIdOrderByCreatedAtDesc(sellerId);
    }

    default void delete(String orderItemId) {
        deleteById(orderItemId);
    }
}
