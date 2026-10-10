package com.velmart.repository;

import com.velmart.model.Order;
import com.velmart.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);

    default List<Order> findByUserId(String userId) {
        return findByUserIdOrderByCreatedAtDesc(userId);
    }

    List<Order> findAllByOrderByCreatedAtDesc();

    List<Order> findByOrderStatus(OrderStatus orderStatus);

    default List<Order> findByStatus(OrderStatus status) {
        return findByOrderStatus(status);
    }

    default void delete(String orderId) {
        deleteById(orderId);
    }
}
