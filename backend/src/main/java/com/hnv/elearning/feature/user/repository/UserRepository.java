package com.hnv.elearning.feature.user.repository;

import com.hnv.elearning.feature.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u " +
            "JOIN FETCH u.userRoles ur " +
            "JOIN FETCH ur.role " +
            "WHERE u.email = :email")
    Optional<User> findWithAuthortiesByEmail (@Param("email") String email);

    boolean existsByEmail (String email);
}
