package com.velmart.repository;

import com.velmart.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {

    Optional<CartItem> findByUserIdAndProductId(String userId, String productId);

    default Optional<CartItem> findByUserAndProduct(String userId, String productId) {
        return findByUserIdAndProductId(userId, productId);
    }

    List<CartItem> findByUserId(String userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);

    default void delete(String cartItemId) {
        deleteById(cartItemId);
    }
}
