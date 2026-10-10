package com.velmart.repository;

import com.velmart.model.AccountStatus;
import com.velmart.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByNormalizedEmail(String normalizedEmail);

    boolean existsByNormalizedEmail(String normalizedEmail);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.accountStatus = :status WHERE u.userId = :userId")
    void updateAccountStatusDirect(@Param("userId") String userId, @Param("status") AccountStatus status);

    default void updateAccountStatus(String userId, AccountStatus status) {
        findById(userId).ifPresent(user -> {
            user.setAccountStatus(status);
            save(user);
        });
    }

    default void delete(String userId) {
        deleteById(userId);
    }
}
