package com.velmart.repository;

import com.velmart.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {

    List<Category> findAllByOrderByDisplayOrderAsc();

    List<Category> findByActiveTrueOrderByDisplayOrderAsc();

    default List<Category> findAllActive() {
        return findByActiveTrueOrderByDisplayOrderAsc();
    }

    default void delete(String categoryId) {
        deleteById(categoryId);
    }
}
