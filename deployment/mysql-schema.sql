-- ==============================================================================
-- VELMART E-Commerce Database Schema for MySQL 8.x
-- ==============================================================================

CREATE DATABASE IF NOT EXISTS `velmart_db` 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

USE `velmart_db`;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS `users` (
    `user_id` VARCHAR(64) NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `normalized_email` VARCHAR(191) NOT NULL,
    `phone` VARCHAR(32) DEFAULT NULL,
    `password_hash` VARCHAR(255) NOT NULL,
    `role` VARCHAR(32) NOT NULL,
    `account_status` VARCHAR(32) NOT NULL,
    `created_at` VARCHAR(64) DEFAULT NULL,
    `updated_at` VARCHAR(64) DEFAULT NULL,
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_users_normalized_email` (`normalized_email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Seller Profiles Table
CREATE TABLE IF NOT EXISTS `seller_profiles` (
    `seller_id` VARCHAR(64) NOT NULL,
    `user_id` VARCHAR(64) NOT NULL,
    `business_name` VARCHAR(255) NOT NULL,
    `business_address` TEXT DEFAULT NULL,
    `phone` VARCHAR(32) DEFAULT NULL,
    `application_status` VARCHAR(32) DEFAULT NULL,
    `approval_notes` TEXT DEFAULT NULL,
    `created_at` VARCHAR(64) DEFAULT NULL,
    `updated_at` VARCHAR(64) DEFAULT NULL,
    PRIMARY KEY (`seller_id`),
    UNIQUE KEY `uk_seller_user_id` (`user_id`),
    CONSTRAINT `fk_seller_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Categories Table
CREATE TABLE IF NOT EXISTS `categories` (
    `category_id` VARCHAR(64) NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `description` TEXT DEFAULT NULL,
    `image_url` VARCHAR(1000) DEFAULT NULL,
    `active` BOOLEAN DEFAULT TRUE,
    `display_order` INT DEFAULT 0,
    `created_at` VARCHAR(64) DEFAULT NULL,
    PRIMARY KEY (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Products Table
CREATE TABLE IF NOT EXISTS `products` (
    `product_id` VARCHAR(64) NOT NULL,
    `seller_id` VARCHAR(64) DEFAULT NULL,
    `name` VARCHAR(255) NOT NULL,
    `description` TEXT DEFAULT NULL,
    `category_id` VARCHAR(64) DEFAULT NULL,
    `price` DOUBLE DEFAULT 0.0,
    `original_price` DOUBLE DEFAULT 0.0,
    `discount_percent` INT DEFAULT 0,
    `stock_quantity` INT DEFAULT 0,
    `image_url` VARCHAR(1000) DEFAULT NULL,
    `gallery_images` TEXT DEFAULT NULL,
    `specifications` TEXT DEFAULT NULL,
    `brand` VARCHAR(255) DEFAULT NULL,
    `active` BOOLEAN DEFAULT TRUE,
    `created_at` VARCHAR(64) DEFAULT NULL,
    `updated_at` VARCHAR(64) DEFAULT NULL,
    PRIMARY KEY (`product_id`),
    KEY `idx_product_seller` (`seller_id`),
    KEY `idx_product_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Addresses Table
CREATE TABLE IF NOT EXISTS `addresses` (
    `address_id` VARCHAR(64) NOT NULL,
    `user_id` VARCHAR(64) NOT NULL,
    `recipient_name` VARCHAR(255) DEFAULT NULL,
    `phone` VARCHAR(32) DEFAULT NULL,
    `address_line_1` VARCHAR(255) DEFAULT NULL,
    `address_line_2` VARCHAR(255) DEFAULT NULL,
    `city` VARCHAR(100) DEFAULT NULL,
    `state` VARCHAR(100) DEFAULT NULL,
    `postal_code` VARCHAR(32) DEFAULT NULL,
    `country` VARCHAR(100) DEFAULT 'India',
    `is_default` BOOLEAN DEFAULT FALSE,
    `created_at` VARCHAR(64) DEFAULT NULL,
    PRIMARY KEY (`address_id`),
    KEY `idx_address_user` (`user_id`),
    CONSTRAINT `fk_address_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Cart Items Table
CREATE TABLE IF NOT EXISTS `cart_items` (
    `cart_item_id` VARCHAR(128) NOT NULL,
    `user_id` VARCHAR(64) NOT NULL,
    `product_id` VARCHAR(64) NOT NULL,
    `quantity` INT NOT NULL DEFAULT 1,
    `updated_at` VARCHAR(64) DEFAULT NULL,
    PRIMARY KEY (`cart_item_id`),
    KEY `idx_cart_user` (`user_id`),
    KEY `idx_cart_user_product` (`user_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Orders Table
CREATE TABLE IF NOT EXISTS `orders` (
    `order_id` VARCHAR(64) NOT NULL,
    `user_id` VARCHAR(64) NOT NULL,
    `customer_name` VARCHAR(255) DEFAULT NULL,
    `customer_email` VARCHAR(191) DEFAULT NULL,
    `contact_phone` VARCHAR(32) DEFAULT NULL,
    `order_status` VARCHAR(32) DEFAULT NULL,
    `item_subtotal` DOUBLE DEFAULT 0.0,
    `shipping_fee` DOUBLE DEFAULT 0.0,
    `total_amount` DOUBLE DEFAULT 0.0,
    `payment_method` VARCHAR(32) DEFAULT NULL,
    `payment_status` VARCHAR(32) DEFAULT NULL,
    `delivery_address_snapshot` TEXT DEFAULT NULL,
    `cancellation_reason` TEXT DEFAULT NULL,
    `idempotency_key` VARCHAR(128) DEFAULT NULL,
    `created_at` VARCHAR(64) DEFAULT NULL,
    `updated_at` VARCHAR(64) DEFAULT NULL,
    PRIMARY KEY (`order_id`),
    KEY `idx_order_user` (`user_id`),
    KEY `idx_order_status` (`order_status`),
    KEY `idx_order_idempotency` (`idempotency_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Order Items Table
CREATE TABLE IF NOT EXISTS `order_items` (
    `order_item_id` VARCHAR(64) NOT NULL,
    `order_id` VARCHAR(64) NOT NULL,
    `seller_id` VARCHAR(64) NOT NULL,
    `productId` VARCHAR(64) NOT NULL,
    `product_name_snapshot` VARCHAR(255) DEFAULT NULL,
    `product_image_snapshot` VARCHAR(1000) DEFAULT NULL,
    `unit_price_snapshot` DOUBLE DEFAULT 0.0,
    `quantity` INT NOT NULL DEFAULT 1,
    `line_total` DOUBLE DEFAULT 0.0,
    `fulfillment_status` VARCHAR(32) DEFAULT NULL,
    `created_at` VARCHAR(64) DEFAULT NULL,
    PRIMARY KEY (`order_item_id`),
    KEY `idx_order_item_order` (`order_id`),
    KEY `idx_order_item_seller` (`seller_id`),
    CONSTRAINT `fk_order_item_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Audit Logs Table
CREATE TABLE IF NOT EXISTS `audit_logs` (
    `audit_id` VARCHAR(64) NOT NULL,
    `actor_id` VARCHAR(64) DEFAULT NULL,
    `actor_email` VARCHAR(191) DEFAULT NULL,
    `role` VARCHAR(32) DEFAULT NULL,
    `action` VARCHAR(64) DEFAULT NULL,
    `target_type` VARCHAR(64) DEFAULT NULL,
    `target_id` VARCHAR(64) DEFAULT NULL,
    `timestamp` VARCHAR(64) DEFAULT NULL,
    `details` TEXT DEFAULT NULL,
    `ip_address` VARCHAR(64) DEFAULT NULL,
    PRIMARY KEY (`audit_id`),
    KEY `idx_audit_actor` (`actor_id`),
    KEY `idx_audit_timestamp` (`timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
