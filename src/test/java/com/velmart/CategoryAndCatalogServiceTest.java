package com.velmart;

import com.velmart.dto.request.CategoryRequest;
import com.velmart.model.Category;
import com.velmart.model.Product;
import com.velmart.service.CategoryService;
import com.velmart.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CategoryAndCatalogServiceTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Test
    @DisplayName("Should retrieve active categories including Watches")
    void testGetActiveCategories() {
        List<Category> categories = categoryService.getActiveCategories();
        assertNotNull(categories);
        assertFalse(categories.isEmpty());
        assertTrue(categories.stream().anyMatch(c -> "watches".equalsIgnoreCase(c.getCategoryId())));
    }

    @Test
    @DisplayName("Admin can create and delete a category")
    void testCreateAndDeleteCategory() {
        CategoryRequest req = new CategoryRequest();
        req.setCategoryId("smart-audio-test");
        req.setName("Smart Audio Test");
        req.setDescription("Wireless smart audio gear");
        req.setImageUrl("https://images.unsplash.com/photo-1505740420928");
        req.setActive(true);
        req.setDisplayOrder(10);

        Category created = categoryService.createCategory(req, "admin-01", "admin@velmart.com", "127.0.0.1");
        assertNotNull(created);
        assertEquals("Smart Audio Test", created.getName());

        // Verify retrieval
        Category fetched = categoryService.getCategoryById(created.getCategoryId());
        assertEquals("Smart Audio Test", fetched.getName());

        // Delete category
        categoryService.deleteCategory(created.getCategoryId(), "admin-01", "admin@velmart.com", "127.0.0.1");
        assertThrows(RuntimeException.class, () -> categoryService.getCategoryById(created.getCategoryId()));
    }

    @Test
    @DisplayName("Search products by keyword and price range")
    void testProductSearchAndFilter() {
        // Search watches by keyword "Analog"
        List<Product> analogWatches = productService.searchProducts("Analog", "watches", 500.0, 3000.0, "price_asc");
        assertNotNull(analogWatches);
        assertFalse(analogWatches.isEmpty());
        assertTrue(analogWatches.stream().allMatch(p -> "watches".equalsIgnoreCase(p.getCategoryId())));
        assertTrue(analogWatches.stream().allMatch(p -> p.getPrice() >= 500.0 && p.getPrice() <= 3000.0));
    }

    @Test
    @DisplayName("Sort products by price ascending")
    void testProductPriceAscendingSort() {
        List<Product> products = productService.searchProducts(null, "watches", null, null, "price_asc");
        assertNotNull(products);
        assertTrue(products.size() >= 2);

        for (int i = 0; i < products.size() - 1; i++) {
            assertTrue(products.get(i).getPrice() <= products.get(i + 1).getPrice(),
                    "Products should be ordered by price ascending");
        }
    }
}
