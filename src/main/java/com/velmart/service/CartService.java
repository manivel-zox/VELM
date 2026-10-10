package com.velmart.service;

import com.velmart.dto.request.CartAddRequest;
import com.velmart.dto.request.CartUpdateRequest;
import com.velmart.dto.response.CartItemViewDto;
import com.velmart.dto.response.CartViewDto;
import com.velmart.exception.BadRequestException;
import com.velmart.exception.ResourceNotFoundException;
import com.velmart.model.CartItem;
import com.velmart.model.Product;
import com.velmart.repository.CartItemRepository;
import com.velmart.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private static final double FREE_SHIPPING_THRESHOLD = 999.0;
    private static final double STANDARD_SHIPPING_FEE = 49.0;

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    public CartViewDto getCart(String userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        CartViewDto cartView = new CartViewDto();
        List<CartItemViewDto> itemViews = new ArrayList<>();

        double subtotal = 0.0;
        int totalItemsCount = 0;
        boolean allInStock = true;

        for (CartItem cartItem : cartItems) {
            Optional<Product> prodOpt = productRepository.findById(cartItem.getProductId());
            if (prodOpt.isEmpty()) {
                // If product was deleted, remove it from the cart
                cartItemRepository.delete(cartItem.getCartItemId());
                continue;
            }

            Product product = prodOpt.get();
            CartItemViewDto viewDto = new CartItemViewDto();
            viewDto.setCartItemId(cartItem.getCartItemId());
            viewDto.setProductId(product.getProductId());
            viewDto.setProductName(product.getName());
            viewDto.setProductImageUrl(product.getImageUrl());
            viewDto.setUnitPrice(product.getPrice());
            viewDto.setOriginalPrice(product.getOriginalPrice() != null ? product.getOriginalPrice() : product.getPrice());
            viewDto.setDiscountPercent(product.getDiscountPercent() != null ? product.getDiscountPercent() : 0);
            viewDto.setQuantity(cartItem.getQuantity());
            viewDto.setSellerId(product.getSellerId());

            double lineTotal = product.getPrice() * cartItem.getQuantity();
            viewDto.setLineTotal(lineTotal);

            boolean inStock = Boolean.TRUE.equals(product.getActive()) &&
                    (product.getStockQuantity() != null && product.getStockQuantity() >= cartItem.getQuantity());
            viewDto.setInStock(inStock);
            viewDto.setAvailableStock(product.getStockQuantity() != null ? product.getStockQuantity() : 0);

            if (!inStock) {
                allInStock = false;
            }

            subtotal += lineTotal;
            totalItemsCount += cartItem.getQuantity();
            itemViews.add(viewDto);
        }

        cartView.setItems(itemViews);
        cartView.setTotalItems(totalItemsCount);
        cartView.setItemSubtotal(Math.round(subtotal * 100.0) / 100.0);
        
        // Shipping calculation: Free if subtotal >= 999 or cart is empty
        double shipping = (subtotal >= FREE_SHIPPING_THRESHOLD || totalItemsCount == 0) ? 0.0 : STANDARD_SHIPPING_FEE;
        cartView.setShippingFee(shipping);
        cartView.setTotalAmount(Math.round((subtotal + shipping) * 100.0) / 100.0);
        cartView.setAllInStock(allInStock);

        return cartView;
    }

    public CartViewDto addToCart(String userId, CartAddRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + request.getProductId()));

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new BadRequestException("This product is currently unavailable.");
        }

        if (product.getStockQuantity() == null || product.getStockQuantity() < 1) {
            throw new BadRequestException("This product is currently out of stock.");
        }

        Optional<CartItem> existingOpt = cartItemRepository.findByUserAndProduct(userId, request.getProductId());
        String now = Instant.now().toString();

        if (existingOpt.isPresent()) {
            CartItem existing = existingOpt.get();
            int newQuantity = existing.getQuantity() + request.getQuantity();
            if (newQuantity > product.getStockQuantity()) {
                throw new BadRequestException("Requested quantity exceeds available stock (" + product.getStockQuantity() + ").");
            }
            existing.setQuantity(newQuantity);
            existing.setUpdatedAt(now);
            cartItemRepository.save(existing);
        } else {
            if (request.getQuantity() > product.getStockQuantity()) {
                throw new BadRequestException("Requested quantity exceeds available stock (" + product.getStockQuantity() + ").");
            }
            CartItem newItem = new CartItem(userId, request.getProductId(), request.getQuantity(), now);
            cartItemRepository.save(newItem);
        }

        return getCart(userId);
    }

    public CartViewDto updateQuantity(String userId, String productId, CartUpdateRequest request) {
        if (request.getQuantity() <= 0) {
            return removeItem(userId, productId);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found."));

        if (request.getQuantity() > product.getStockQuantity()) {
            throw new BadRequestException("Requested quantity exceeds available stock (" + product.getStockQuantity() + ").");
        }

        String cartItemId = userId + "#" + productId;
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart."));

        cartItem.setQuantity(request.getQuantity());
        cartItem.setUpdatedAt(Instant.now().toString());
        cartItemRepository.save(cartItem);

        return getCart(userId);
    }

    public CartViewDto removeItem(String userId, String productId) {
        String cartItemId = userId + "#" + productId;
        cartItemRepository.delete(cartItemId);
        return getCart(userId);
    }

    public void clearCart(String userId) {
        cartItemRepository.deleteByUserId(userId);
    }
}
