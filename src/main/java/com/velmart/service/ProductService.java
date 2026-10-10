package com.velmart.service;

import com.velmart.dto.request.ProductRequest;
import com.velmart.dto.request.ProductStatusRequest;
import com.velmart.exception.BadRequestException;
import com.velmart.exception.ResourceNotFoundException;
import com.velmart.exception.UnauthorizedActionException;
import com.velmart.model.Product;
import com.velmart.repository.CategoryRepository;
import com.velmart.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final AuditLogService auditLogService;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          AuditLogService auditLogService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.auditLogService = auditLogService;
    }

    // Public methods
    public List<Product> searchProducts(String keyword, String categoryId, Double minPrice, Double maxPrice, String sortBy) {
        return productRepository.search(keyword, categoryId, minPrice, maxPrice, sortBy);
    }

    public Product getProductById(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
    }

    public List<Product> getProductsByCategory(String categoryId) {
        return productRepository.findByCategoryId(categoryId).stream()
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .toList();
    }

    // Seller methods
    public List<Product> getSellerProducts(String sellerId) {
        return productRepository.findBySellerId(sellerId);
    }

    public Product createProduct(String sellerId, ProductRequest request, String sellerEmail, String clientIp) {
        if (!categoryRepository.findById(request.getCategoryId()).isPresent()) {
            throw new BadRequestException("Category ID does not exist: " + request.getCategoryId());
        }

        String productId = UUID.randomUUID().toString();
        String now = Instant.now().toString();

        Product product = new Product();
        product.setProductId(productId);
        product.setSellerId(sellerId);
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setCategoryId(request.getCategoryId());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice() != null ? request.getOriginalPrice() : request.getPrice());
        
        if (product.getOriginalPrice() > product.getPrice()) {
            int discount = (int) Math.round(((product.getOriginalPrice() - product.getPrice()) / product.getOriginalPrice()) * 100);
            product.setDiscountPercent(discount);
        } else {
            product.setDiscountPercent(0);
        }

        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());
        product.setGalleryImages(request.getGalleryImages() != null ? request.getGalleryImages() : List.of(request.getImageUrl()));
        product.setSpecifications(request.getSpecifications());
        product.setBrand(request.getBrand() != null ? request.getBrand() : "VELMART");
        product.setActive(request.getActive() != null ? request.getActive() : true);
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        productRepository.save(product);

        auditLogService.logAction(sellerId, sellerEmail, "ROLE_SELLER", "CREATE_PRODUCT",
                "PRODUCT", productId, "Added product: " + product.getName(), clientIp);

        return product;
    }

    public Product updateProduct(String sellerId, String productId, ProductRequest request, String sellerEmail, String clientIp) {
        Product product = getProductById(productId);

        if (!product.getSellerId().equals(sellerId)) {
            throw new UnauthorizedActionException("You do not have permission to modify this product as you are not its seller.");
        }

        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setCategoryId(request.getCategoryId());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice() != null ? request.getOriginalPrice() : request.getPrice());
        
        if (product.getOriginalPrice() > product.getPrice()) {
            int discount = (int) Math.round(((product.getOriginalPrice() - product.getPrice()) / product.getOriginalPrice()) * 100);
            product.setDiscountPercent(discount);
        } else {
            product.setDiscountPercent(0);
        }

        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());
        if (request.getGalleryImages() != null && !request.getGalleryImages().isEmpty()) {
            product.setGalleryImages(request.getGalleryImages());
        }
        product.setSpecifications(request.getSpecifications());
        product.setBrand(request.getBrand() != null ? request.getBrand() : product.getBrand());
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
        product.setUpdatedAt(Instant.now().toString());

        productRepository.save(product);

        auditLogService.logAction(sellerId, sellerEmail, "ROLE_SELLER", "UPDATE_PRODUCT",
                "PRODUCT", productId, "Updated product: " + product.getName(), clientIp);

        return product;
    }

    public Product updateProductStatus(String sellerId, String productId, ProductStatusRequest request, String sellerEmail, String clientIp) {
        Product product = getProductById(productId);

        if (!product.getSellerId().equals(sellerId)) {
            throw new UnauthorizedActionException("You do not have permission to modify this product.");
        }

        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
        if (request.getStockQuantity() != null) {
            product.setStockQuantity(request.getStockQuantity());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        product.setUpdatedAt(Instant.now().toString());

        productRepository.save(product);

        auditLogService.logAction(sellerId, sellerEmail, "ROLE_SELLER", "UPDATE_PRODUCT_STATUS",
                "PRODUCT", productId, "Updated status/stock/price for: " + product.getName(), clientIp);

        return product;
    }

    public void deleteProductBySeller(String sellerId, String productId, String sellerEmail, String clientIp) {
        Product product = getProductById(productId);
        if (!product.getSellerId().equals(sellerId)) {
            throw new UnauthorizedActionException("You do not have permission to delete this product.");
        }
        productRepository.delete(productId);

        auditLogService.logAction(sellerId, sellerEmail, "ROLE_SELLER", "DELETE_PRODUCT",
                "PRODUCT", productId, "Deleted product: " + product.getName(), clientIp);
    }

    // Admin methods
    public List<Product> getAllProductsForAdmin() {
        return productRepository.findAll();
    }

    public void adminToggleProductActive(String adminId, String adminEmail, String productId, boolean active, String clientIp) {
        Product product = getProductById(productId);
        product.setActive(active);
        product.setUpdatedAt(Instant.now().toString());
        productRepository.save(product);

        auditLogService.logAction(adminId, adminEmail, "ROLE_ADMIN", "ADMIN_TOGGLE_PRODUCT",
                "PRODUCT", productId, "Set active=" + active + " for: " + product.getName(), clientIp);
    }
}
