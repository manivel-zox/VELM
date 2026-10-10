package com.velmart;

import com.velmart.dto.request.AddressRequest;
import com.velmart.dto.request.CartAddRequest;
import com.velmart.dto.request.CheckoutRequest;
import com.velmart.dto.response.CartViewDto;
import com.velmart.dto.response.OrderDetailDto;
import com.velmart.exception.InsufficientStockException;
import com.velmart.model.Address;
import com.velmart.model.OrderStatus;
import com.velmart.model.PaymentMethod;
import com.velmart.model.Product;
import com.velmart.model.User;
import com.velmart.repository.ProductRepository;
import com.velmart.repository.UserRepository;
import com.velmart.service.AddressService;
import com.velmart.service.CartService;
import com.velmart.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CartAndCheckoutServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private String testUserId;
    private String testProductId;
    private Address savedAddress;

    @BeforeEach
    void setUp() {
        testUserId = "cust-test-" + System.currentTimeMillis();
        testProductId = "vel-watch-01"; // Seeded Classic Analog Watch (price: ₹1299, initial stock: ~45)

        // Save a test user in userRepository
        User testUser = new User();
        testUser.setUserId(testUserId);
        testUser.setName("Test Customer");
        testUser.setNormalizedEmail(testUserId + "@velmart.com");
        testUser.setPhone("+91 9988776655");
        testUser.setPasswordHash("$2a$12$eW...");
        testUser.setRole(com.velmart.model.Role.ROLE_CUSTOMER);
        testUser.setAccountStatus(com.velmart.model.AccountStatus.ACTIVE);
        testUser.setCreatedAt(java.time.Instant.now().toString());
        userRepository.save(testUser);

        // Add an address for this test customer
        AddressRequest addrReq = new AddressRequest();
        addrReq.setRecipientName("Test Customer");
        addrReq.setPhone("+91 9988776655");
        addrReq.setAddressLine1("Test Street 123");
        addrReq.setCity("Bengaluru");
        addrReq.setState("Karnataka");
        addrReq.setPostalCode("560001");
        addrReq.setIsDefault(true);

        savedAddress = addressService.createAddress(testUserId, addrReq);
        cartService.clearCart(testUserId);
    }

    @Test
    @DisplayName("Should add product to cart and compute totals correctly")
    void testAddToCartAndTotals() {
        CartAddRequest req = new CartAddRequest(testProductId, 2);
        CartViewDto cart = cartService.addToCart(testUserId, req);

        assertNotNull(cart);
        assertEquals(2, cart.getTotalItems());
        assertEquals(1, cart.getItems().size());
        assertEquals(2598.00, cart.getItemSubtotal()); // 1299 * 2
        assertEquals(0.00, cart.getShippingFee()); // > 999 is free shipping
        assertEquals(2598.00, cart.getTotalAmount());
        assertTrue(cart.isAllInStock());
    }

    @Test
    @DisplayName("Should reject cart addition if quantity exceeds stock")
    void testExcessQuantityRejected() {
        Product p = productRepository.findById(testProductId).orElseThrow();
        int excessQty = p.getStockQuantity() + 10;

        CartAddRequest req = new CartAddRequest(testProductId, excessQty);
        assertThrows(RuntimeException.class, () -> cartService.addToCart(testUserId, req));
    }

    @Test
    @DisplayName("Should process checkout, decrement stock, and clear cart")
    void testCheckoutAndStockDecrement() {
        // Stock before checkout
        Product pBefore = productRepository.findById(testProductId).orElseThrow();
        int initialStock = pBefore.getStockQuantity();

        // Add 1 item to cart
        cartService.addToCart(testUserId, new CartAddRequest(testProductId, 1));

        // Execute checkout
        CheckoutRequest checkoutReq = new CheckoutRequest();
        checkoutReq.setAddressId(savedAddress.getAddressId());
        checkoutReq.setContactPhone("+91 9988776655");
        checkoutReq.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        checkoutReq.setIdempotencyKey("idemp_test_" + System.currentTimeMillis());

        OrderDetailDto order = orderService.placeOrder(testUserId, checkoutReq, "127.0.0.1");

        assertNotNull(order);
        assertTrue(order.getOrderId().startsWith("VEL-"));
        assertEquals(OrderStatus.PLACED, order.getOrderStatus());
        assertEquals(1, order.getItems().size());

        // Verify stock decremented by 1
        Product pAfter = productRepository.findById(testProductId).orElseThrow();
        assertEquals(initialStock - 1, pAfter.getStockQuantity());

        // Verify cart is cleared
        CartViewDto emptyCart = cartService.getCart(testUserId);
        assertEquals(0, emptyCart.getTotalItems());
    }

    @Test
    @DisplayName("Should prevent duplicate orders on identical idempotency key")
    void testIdempotentCheckout() {
        cartService.addToCart(testUserId, new CartAddRequest(testProductId, 1));

        String key = "idemp_duplicate_test_" + System.currentTimeMillis();

        CheckoutRequest checkoutReq = new CheckoutRequest();
        checkoutReq.setAddressId(savedAddress.getAddressId());
        checkoutReq.setContactPhone("+91 9988776655");
        checkoutReq.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        checkoutReq.setIdempotencyKey(key);

        OrderDetailDto firstOrder = orderService.placeOrder(testUserId, checkoutReq, "127.0.0.1");
        assertNotNull(firstOrder);

        // Submitting same idempotency key again
        OrderDetailDto duplicateOrder = orderService.placeOrder(testUserId, checkoutReq, "127.0.0.1");
        assertEquals(firstOrder.getOrderId(), duplicateOrder.getOrderId());
    }

    @Test
    @DisplayName("Customer order cancellation restores inventory stock")
    void testOrderCancellationRestoresInventory() {
        Product pBefore = productRepository.findById(testProductId).orElseThrow();
        int initialStock = pBefore.getStockQuantity();

        cartService.addToCart(testUserId, new CartAddRequest(testProductId, 2));

        CheckoutRequest checkoutReq = new CheckoutRequest();
        checkoutReq.setAddressId(savedAddress.getAddressId());
        checkoutReq.setContactPhone("+91 9988776655");
        checkoutReq.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        OrderDetailDto order = orderService.placeOrder(testUserId, checkoutReq, "127.0.0.1");
        assertEquals(initialStock - 2, productRepository.findById(testProductId).orElseThrow().getStockQuantity());

        // Cancel order
        OrderDetailDto cancelled = orderService.cancelOrderByCustomer(testUserId, order.getOrderId(), "Testing cancellation restock", "127.0.0.1");
        assertEquals(OrderStatus.CANCELLED, cancelled.getOrderStatus());

        // Assert inventory is fully restored back to initialStock
        Product pRestored = productRepository.findById(testProductId).orElseThrow();
        assertEquals(initialStock, pRestored.getStockQuantity());
    }
}
