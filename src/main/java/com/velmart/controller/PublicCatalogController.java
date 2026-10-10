package com.velmart.controller;

import com.velmart.dto.response.ApiResponse;
import com.velmart.model.Category;
import com.velmart.model.Product;
import com.velmart.service.CategoryService;
import com.velmart.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public")
public class PublicCatalogController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public PublicCatalogController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<Category>>> getCategories() {
        List<Category> categories = categoryService.getActiveCategories();
        return ResponseEntity.ok(ApiResponse.ok("Categories retrieved successfully.", categories));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<Product>>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false, defaultValue = "newest") String sortBy) {

        List<Product> products = productService.searchProducts(keyword, categoryId, minPrice, maxPrice, sortBy);
        return ResponseEntity.ok(ApiResponse.ok("Products retrieved successfully.", products));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<Product>> getProductDetails(@PathVariable String productId) {
        Product product = productService.getProductById(productId);
        return ResponseEntity.ok(ApiResponse.ok("Product details retrieved successfully.", product));
    }

    @GetMapping("/categories/{categoryId}/products")
    public ResponseEntity<ApiResponse<List<Product>>> getProductsByCategory(@PathVariable String categoryId) {
        List<Product> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.ok("Category products retrieved successfully.", products));
    }
}
