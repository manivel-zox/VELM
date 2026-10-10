package com.velmart;

import com.velmart.dto.request.ProductRequest;
import com.velmart.exception.UnauthorizedActionException;
import com.velmart.model.Product;
import com.velmart.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductOwnershipTest {

    @Autowired
    private ProductService productService;

    @Test
    @DisplayName("Seller cannot update or delete product owned by another seller")
    void testSellerProductOwnershipEnforced() {
        String sellerA = "seller-a-" + System.currentTimeMillis();
        String sellerB = "seller-b-" + System.currentTimeMillis();

        // Seller A creates a product
        ProductRequest req = new ProductRequest();
        req.setName("Seller A Exclusive Watch");
        req.setDescription("Watch crafted by Seller A");
        req.setCategoryId("watches");
        req.setPrice(1999.00);
        req.setStockQuantity(10);
        req.setImageUrl("https://images.unsplash.com/photo-1524805444758-089113d48a6d?w=700");

        Product created = productService.createProduct(sellerA, req, "sellerA@velmart.com", "127.0.0.1");
        assertNotNull(created);
        assertEquals(sellerA, created.getSellerId());

        // Seller B attempts to modify Seller A's product
        ProductRequest updateReq = new ProductRequest();
        updateReq.setName("Hacked Name");
        updateReq.setDescription("Hacked Desc");
        updateReq.setCategoryId("watches");
        updateReq.setPrice(99.00);
        updateReq.setStockQuantity(5);
        updateReq.setImageUrl("https://images.unsplash.com/photo-1524805444758-089113d48a6d?w=700");

        assertThrows(UnauthorizedActionException.class, () ->
                productService.updateProduct(sellerB, created.getProductId(), updateReq, "sellerB@velmart.com", "127.0.0.1")
        );

        // Seller B attempts to delete Seller A's product
        assertThrows(UnauthorizedActionException.class, () ->
                productService.deleteProductBySeller(sellerB, created.getProductId(), "sellerB@velmart.com", "127.0.0.1")
        );

        // Seller A should be able to update their own product
        updateReq.setName("Updated By True Owner");
        Product updated = productService.updateProduct(sellerA, created.getProductId(), updateReq, "sellerA@velmart.com", "127.0.0.1");
        assertEquals("Updated By True Owner", updated.getName());
    }
}
