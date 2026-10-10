package com.velmart.repository;

import com.velmart.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {

    List<Address> findByUserIdOrderByCreatedAtDesc(String userId);

    default List<Address> findByUserId(String userId) {
        return findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.userId = :userId")
    void clearDefaultAddresses(@Param("userId") String userId);

    default void delete(String addressId) {
        deleteById(addressId);
    }
}
