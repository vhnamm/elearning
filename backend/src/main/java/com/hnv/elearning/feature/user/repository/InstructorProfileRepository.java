package com.hnv.elearning.feature.user.repository;

import com.hnv.elearning.feature.user.entity.InstructorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InstructorProfileRepository extends JpaRepository<InstructorProfile, Long> {
    @Query("SELECT " +
            "CASE WHEN COUNT(ip) > 0 THEN true " +
            "ELSE false END " +
            "FROM InstructorProfile ip JOIN ip.user u " +
            "WHERE u.email = :email")
    boolean existsByEmail(@Param("email") String email);
}
