package com.velmart.service;

import com.velmart.dto.request.CheckoutRequest;
import com.velmart.dto.request.FulfillmentStatusRequest;
import com.velmart.dto.request.OrderStatusUpdateRequest;
import com.velmart.dto.response.CartItemViewDto;
import com.velmart.dto.response.CartViewDto;
import com.velmart.dto.response.OrderDetailDto;
import com.velmart.exception.BadRequestException;
import com.velmart.exception.InsufficientStockException;
import com.velmart.exception.ResourceNotFoundException;
import com.velmart.exception.UnauthorizedActionException;
import com.velmart.model.Address;
import com.velmart.model.FulfillmentStatus;
import com.velmart.model.Order;
import com.velmart.model.OrderItem;
import com.velmart.model.OrderStatus;
import com.velmart.model.PaymentMethod;
import com.velmart.model.PaymentStatus;
import com.velmart.model.Product;
import com.velmart.model.User;
import com.velmart.repository.AddressRepository;
import com.velmart.repository.OrderItemRepository;
import com.velmart.repository.OrderRepository;
import com.velmart.repository.ProductRepository;
import com.velmart.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final AuditLogService auditLogService;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        ProductRepository productRepository,
                        AddressRepository addressRepository,
                        UserRepository userRepository,
                        CartService cartService,
                        AuditLogService auditLogService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.cartService = cartService;
        this.auditLogService = auditLogService;
    }

    public synchronized OrderDetailDto placeOrder(String userId, CheckoutRequest request, String clientIp) {
        // 1. Idempotency Check
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            Optional<Order> existingOrder = orderRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existingOrder.isPresent()) {
                log.info("Duplicate checkout prevented by idempotency key: {}", request.getIdempotencyKey());
                return getOrderDetails(userId, existingOrder.get().getOrderId(), false);
            }
        }

        // 2. Validate User & Delivery Address
        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer account not found."));

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new BadRequestException("Invalid delivery address selected."));

        if (!address.getUserId().equals(userId)) {
            throw new UnauthorizedActionException("You cannot use another user's delivery address.");
        }

        // 3. Retrieve and Validate Server-side Cart
        CartViewDto cart = cartService.getCart(userId);
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Your shopping cart is empty. Please add items before checking out.");
        }

        if (!cart.isAllInStock()) {
            throw new InsufficientStockException("One or more items in your cart are currently out of stock or have insufficient quantity. Please review your cart.");
        }

        // 4. Concurrency-Safe Stock Validation & Reservation
        List<Product> productsToUpdate = new ArrayList<>();
        for (CartItemViewDto item : cart.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product no longer exists: " + item.getProductName()));

            if (product.getStockQuantity() == null || product.getStockQuantity() < item.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product '" + product.getName() + "'. Available: " +
                        (product.getStockQuantity() != null ? product.getStockQuantity() : 0));
            }
            productsToUpdate.add(product);
        }

        // Decrement stock
        for (int i = 0; i < cart.getItems().size(); i++) {
            CartItemViewDto item = cart.getItems().get(i);
            Product product = productsToUpdate.get(i);
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            product.setUpdatedAt(Instant.now().toString());
            productRepository.save(product);
        }

        // 5. Build Address Snapshot
        Map<String, String> addressSnapshot = new HashMap<>();
        addressSnapshot.put("recipientName", address.getRecipientName());
        addressSnapshot.put("phone", request.getContactPhone() != null ? request.getContactPhone() : address.getPhone());
        addressSnapshot.put("addressLine1", address.getAddressLine1());
        addressSnapshot.put("addressLine2", address.getAddressLine2() != null ? address.getAddressLine2() : "");
        addressSnapshot.put("city", address.getCity());
        addressSnapshot.put("state", address.getState());
        addressSnapshot.put("postalCode", address.getPostalCode());
        addressSnapshot.put("country", address.getCountry());

        // 6. Generate Unique Order ID
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomSuffix = ThreadLocalRandom.current().nextInt(1000, 9999);
        String orderId = "VEL-" + dateStr + "-" + randomSuffix;

        String now = Instant.now().toString();

        Order order = new Order();
        order.setOrderId(orderId);
        order.setUserId(userId);
        order.setCustomerName(customer.getName());
        order.setCustomerEmail(customer.getNormalizedEmail());
        order.setContactPhone(request.getContactPhone() != null ? request.getContactPhone() : address.getPhone());
        order.setOrderStatus(OrderStatus.PLACED);
        order.setItemSubtotal(cart.getItemSubtotal());
        order.setShippingFee(cart.getShippingFee());
        order.setTotalAmount(cart.getTotalAmount());
        order.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.CASH_ON_DELIVERY);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setDeliveryAddressSnapshot(addressSnapshot);
        order.setIdempotencyKey(request.getIdempotencyKey());
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        orderRepository.save(order);

        // 7. Create Order Items
        List<OrderItem> createdItems = new ArrayList<>();
        for (CartItemViewDto item : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderItemId(UUID.randomUUID().toString());
            orderItem.setOrderId(orderId);
            orderItem.setSellerId(item.getSellerId());
            orderItem.setProductId(item.getProductId());
            orderItem.setProductNameSnapshot(item.getProductName());
            orderItem.setProductImageSnapshot(item.getProductImageUrl());
            orderItem.setUnitPriceSnapshot(item.getUnitPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setLineTotal(item.getLineTotal());
            orderItem.setFulfillmentStatus(FulfillmentStatus.PLACED);
            orderItem.setCreatedAt(now);

            orderItemRepository.save(orderItem);
            createdItems.add(orderItem);
        }

        // 8. Clear Customer Cart
        cartService.clearCart(userId);

        auditLogService.logAction(userId, customer.getNormalizedEmail(), "ROLE_CUSTOMER",
                "PLACE_ORDER", "ORDER", orderId, "Order placed successfully. Total: ₹" + order.getTotalAmount(), clientIp);

        return toDetailDto(order, createdItems);
    }

    public OrderDetailDto getOrderDetails(String userId, String orderId, boolean isAdminOrSeller) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (!isAdminOrSeller && !order.getUserId().equals(userId)) {
            throw new UnauthorizedActionException("Access denied. You are not authorized to view this order.");
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return toDetailDto(order, items);
    }

    public List<OrderDetailDto> getCustomerOrders(String userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(o -> toDetailDto(o, orderItemRepository.findByOrderId(o.getOrderId())))
                .collect(Collectors.toList());
    }

    public OrderDetailDto cancelOrderByCustomer(String userId, String orderId, String reason, String clientIp) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new UnauthorizedActionException("You are not authorized to cancel this order.");
        }

        if (order.getOrderStatus() == OrderStatus.SHIPPED || order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Orders that have already been shipped or delivered cannot be cancelled online. Please contact support.");
        }

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("This order is already cancelled.");
        }

        // Restock inventory
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : items) {
            item.setFulfillmentStatus(FulfillmentStatus.CANCELLED);
            orderItemRepository.save(item);

            productRepository.findById(item.getProductId()).ifPresent(prod -> {
                prod.setStockQuantity(prod.getStockQuantity() + item.getQuantity());
                prod.setUpdatedAt(Instant.now().toString());
                productRepository.save(prod);
            });
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setCancellationReason(reason != null ? reason : "Cancelled by customer");
        order.setUpdatedAt(Instant.now().toString());
        orderRepository.save(order);

        auditLogService.logAction(userId, order.getCustomerEmail(), "ROLE_CUSTOMER",
                "CANCEL_ORDER", "ORDER", orderId, "Order cancelled by customer. Reason: " + order.getCancellationReason(), clientIp);

        return toDetailDto(order, items);
    }

    // Seller order items
    public List<OrderItem> getSellerOrderItems(String sellerId) {
        return orderItemRepository.findBySellerId(sellerId);
    }

    public OrderItem updateItemFulfillmentStatus(String sellerId, String orderItemId, FulfillmentStatusRequest request, String sellerEmail, String clientIp) {
        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found with ID: " + orderItemId));

        if (!item.getSellerId().equals(sellerId)) {
            throw new UnauthorizedActionException("You do not have permission to update this order item.");
        }

        item.setFulfillmentStatus(request.getFulfillmentStatus());
        orderItemRepository.save(item);

        // Update parent order status if all items progressed
        checkAndUpdateParentOrderStatus(item.getOrderId());

        auditLogService.logAction(sellerId, sellerEmail, "ROLE_SELLER",
                "UPDATE_FULFILLMENT_STATUS", "ORDER_ITEM", orderItemId,
                "Item status changed to: " + request.getFulfillmentStatus(), clientIp);

        return item;
    }

    private void checkAndUpdateParentOrderStatus(String orderId) {
        List<OrderItem> allItems = orderItemRepository.findByOrderId(orderId);
        if (allItems.isEmpty()) return;

        boolean allDelivered = allItems.stream().allMatch(i -> i.getFulfillmentStatus() == FulfillmentStatus.DELIVERED);
        boolean anyShipped = allItems.stream().anyMatch(i -> i.getFulfillmentStatus() == FulfillmentStatus.SHIPPED || i.getFulfillmentStatus() == FulfillmentStatus.DELIVERED);
        boolean anyProcessing = allItems.stream().anyMatch(i -> i.getFulfillmentStatus() == FulfillmentStatus.PROCESSING);

        orderRepository.findById(orderId).ifPresent(order -> {
            if (order.getOrderStatus() == OrderStatus.CANCELLED) return;

            if (allDelivered) {
                order.setOrderStatus(OrderStatus.DELIVERED);
                order.setPaymentStatus(PaymentStatus.COMPLETED);
            } else if (anyShipped) {
                order.setOrderStatus(OrderStatus.SHIPPED);
            } else if (anyProcessing) {
                order.setOrderStatus(OrderStatus.PROCESSING);
            }
            order.setUpdatedAt(Instant.now().toString());
            orderRepository.save(order);
        });
    }

    // Admin order operations
    public List<OrderDetailDto> getAllOrdersForAdmin() {
        return orderRepository.findAll().stream()
                .map(o -> toDetailDto(o, orderItemRepository.findByOrderId(o.getOrderId())))
                .collect(Collectors.toList());
    }

    public OrderDetailDto updateOrderStatusByAdmin(String adminId, String adminEmail, String orderId, OrderStatusUpdateRequest request, String clientIp) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        order.setOrderStatus(request.getOrderStatus());
        if (request.getCancellationReason() != null) {
            order.setCancellationReason(request.getCancellationReason());
        }
        if (request.getOrderStatus() == OrderStatus.DELIVERED) {
            order.setPaymentStatus(PaymentStatus.COMPLETED);
        }
        order.setUpdatedAt(Instant.now().toString());
        orderRepository.save(order);

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        auditLogService.logAction(adminId, adminEmail, "ROLE_ADMIN",
                "ADMIN_UPDATE_ORDER_STATUS", "ORDER", orderId, "Status set to: " + request.getOrderStatus(), clientIp);

        return toDetailDto(order, items);
    }

    private OrderDetailDto toDetailDto(Order order, List<OrderItem> items) {
        OrderDetailDto dto = new OrderDetailDto();
        dto.setOrderId(order.getOrderId());
        dto.setUserId(order.getUserId());
        dto.setCustomerName(order.getCustomerName());
        dto.setCustomerEmail(order.getCustomerEmail());
        dto.setContactPhone(order.getContactPhone());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setItemSubtotal(order.getItemSubtotal());
        dto.setShippingFee(order.getShippingFee());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setDeliveryAddressSnapshot(order.getDeliveryAddressSnapshot());
        dto.setCancellationReason(order.getCancellationReason());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        dto.setItems(items != null ? items : List.of());
        return dto;
    }
}
