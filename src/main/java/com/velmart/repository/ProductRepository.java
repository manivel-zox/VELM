package com.velmart.repository;

import com.velmart.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    List<Product> findByCategoryId(String categoryId);

    List<Product> findBySellerId(String sellerId);

    default List<Product> search(String keyword, String categoryId, Double minPrice, Double maxPrice, String sortBy) {
        List<Product> products = findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .filter(p -> {
                    if (categoryId != null && !categoryId.isBlank() && !"all".equalsIgnoreCase(categoryId)) {
                        return categoryId.equalsIgnoreCase(p.getCategoryId());
                    }
                    return true;
                })
                .filter(p -> {
                    if (keyword != null && !keyword.isBlank()) {
                        String lowerKw = keyword.toLowerCase().trim();
                        boolean matchesName = p.getName() != null && p.getName().toLowerCase().contains(lowerKw);
                        boolean matchesDesc = p.getDescription() != null && p.getDescription().toLowerCase().contains(lowerKw);
                        boolean matchesBrand = p.getBrand() != null && p.getBrand().toLowerCase().contains(lowerKw);
                        return matchesName || matchesDesc || matchesBrand;
                    }
                    return true;
                })
                .filter(p -> {
                    if (minPrice != null && minPrice > 0) {
                        return p.getPrice() != null && p.getPrice() >= minPrice;
                    }
                    return true;
                })
                .filter(p -> {
                    if (maxPrice != null && maxPrice > 0) {
                        return p.getPrice() != null && p.getPrice() <= maxPrice;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        if ("price_asc".equalsIgnoreCase(sortBy)) {
            products.sort(Comparator.comparingDouble(p -> p.getPrice() != null ? p.getPrice() : 0.0));
        } else if ("price_desc".equalsIgnoreCase(sortBy)) {
            products.sort((p1, p2) -> Double.compare(
                    p2.getPrice() != null ? p2.getPrice() : 0.0,
                    p1.getPrice() != null ? p1.getPrice() : 0.0
            ));
        } else if ("name_asc".equalsIgnoreCase(sortBy)) {
            products.sort(Comparator.comparing(p -> p.getName() != null ? p.getName().toLowerCase() : ""));
        } else if ("newest".equalsIgnoreCase(sortBy)) {
            products.sort((p1, p2) -> {
                String c1 = p1.getCreatedAt() != null ? p1.getCreatedAt() : "";
                String c2 = p2.getCreatedAt() != null ? p2.getCreatedAt() : "";
                return c2.compareTo(c1);
            });
        }

        return products;
    }

    default void delete(String productId) {
        deleteById(productId);
    }
}
