package com.velmart.repository;

import com.velmart.model.ApplicationStatus;
import com.velmart.model.SellerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerProfileRepository extends JpaRepository<SellerProfile, String> {

    Optional<SellerProfile> findByUserId(String userId);

    List<SellerProfile> findByApplicationStatus(ApplicationStatus applicationStatus);

    default void delete(String sellerId) {
        deleteById(sellerId);
    }
}
