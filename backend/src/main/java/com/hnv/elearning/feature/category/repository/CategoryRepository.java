package com.hnv.elearning.feature.category.repository;

import com.hnv.elearning.feature.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category,Long> {

    @Query("SELECT c FROM Category c")
    List<Category> findAll();

}
