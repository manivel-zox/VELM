# VELMART — Full-Stack E-Commerce Platform

VELMART is a secure, enterprise-grade e-commerce web application engineered with **Java 21**, **Spring Boot 3.x**, **Spring Data JPA**, **MySQL 8.x**, **Spring Security (RBAC & BCrypt)**, and **Vanilla HTML5/CSS3/JavaScript**.

---

## 🌟 Key Features

1. **Multi-Role Access Control (RBAC)**:
   - **CUSTOMER**: Registration, persistent shopping cart, address book management, transactional stock validation & decrement, Cash on Delivery checkout with idempotency protection, real-time order tracking, and pre-dispatch cancellation.
   - **SELLER**: Business onboarding, application review status, inventory & product CRUD with strict record ownership verification, and item-level order fulfillment workflow (`PROCESSING` &rarr; `SHIPPED` &rarr; `DELIVERED`).
   - **ADMINISTRATOR**: Executive analytics, user governance (activation/suspension), seller application approval/rejection, category management, cross-seller product controls, global order oversight, and audit logs.
2. **Product Catalog & Watches Collection**:
   - Rich, realistic sample products across 7 departments: **Watches** (*Classic Analog, Chronographs, Smartwatches, Sports*), **Electronics**, **Men's Fashion**, **Women's Fashion**, **Footwear**, **Bags & Accessories**, and **Home & Living**.
   - Server-side filtering, keyword search, price range filters, and multi-criteria sorting (`price_asc`, `price_desc`, `name_asc`, `newest`).
3. **Database & Architecture**:
   - 9 relational MySQL tables managed via Spring Data JPA and Hibernate ORM.
   - Automatic schema initialization and update with foreign keys and performance indexes.
   - Server-side calculations for all subtotals, free delivery threshold (₹999), and order totals in Indian Rupees (₹).
   - Rate limiting on authentication endpoints to prevent brute-force attacks.

---

## 🛠️ Technology Stack

- **Backend**: Java 21, Spring Boot 3.3.4, Spring Data JPA, Spring Security, Spring Validation, Jackson.
- **Database**: MySQL 8.x (`com.mysql:mysql-connector-j`), Hibernate ORM.
- **Frontend**: Responsive HTML5, CSS3 (Navy & Royal Blue design tokens), Vanilla ES6 JavaScript (Fetch API).
- **Testing**: JUnit 5, MockMvc, Spring Security Test, H2 In-Memory DB (MySQL compatibility mode).
- **Hosting / Infra**: AWS Lightsail / Linux, Nginx Reverse Proxy, Let's Encrypt SSL, systemd service.

---

## 🚀 Quick Start (Local Development)

### 1. Prerequisites
- Java 21 JDK installed (`java -version`)
- Apache Maven 3.9+ (`mvn -version`)
- MySQL 8.x Server installed and running

### 2. Configure Database
Create the database or let Spring Boot automatically create it:
```sql
CREATE DATABASE IF NOT EXISTS velmart_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Configure your MySQL credentials in `.env` (or pass environment variables):
```env
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=velmart_db
MYSQL_USER=root
MYSQL_PASSWORD=your_mysql_password
```

### 3. Run the Application
```bash
mvn clean spring-boot:run
```
The application will start on `http://localhost:8080`.

### 4. Pre-Seeded Demonstration Accounts

| Role | Email | Password | Access / Portal |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin@velmart.com` | `Admin@Velmart2026!` | [http://localhost:8080/admin-dashboard.html](http://localhost:8080/admin-dashboard.html) |
| **Approved Seller** | `seller@velmart.com` | `Seller@Velmart2026!` | [http://localhost:8080/seller-dashboard.html](http://localhost:8080/seller-dashboard.html) |
| **Customer** | `customer@velmart.com` | `Customer@Velmart2026!` | [http://localhost:8080/login.html](http://localhost:8080/login.html) |

---

## 🗄️ Database Tables (MySQL)

1. `users` — User credentials, roles (`ROLE_CUSTOMER`, `ROLE_SELLER`, `ROLE_ADMIN`), account status.
2. `seller_profiles` — Business metadata, store address, vendor application approval status.
3. `categories` — Product categories and navigation order.
4. `products` — Catalog items, pricing, inventory stock, images, specifications.
5. `addresses` — Customer shipping addresses and default selection.
6. `cart_items` — Persistent per-user shopping carts.
7. `orders` — Customer orders, delivery snapshot, payment and order status.
8. `order_items` — Item-level order fulfillment and price snapshots.
9. `audit_logs` — Complete platform activity and governance audit trail.

---

## 🧪 Running Automated Tests

Run the complete test suite:
```bash
mvn clean test
```

### Verified Test Coverage:
- `AuthControllerTest`: User registration, duplicate email rejection, BCrypt hashing, login authentication.
- `SecurityRbacTest`: Role-based authorization rules across Customer, Seller, and Admin endpoints.
- `CartAndCheckoutServiceTest`: Shopping cart subtotals, stock decrement, duplicate order prevention via idempotency keys, and cancellation restock.
- `ProductOwnershipTest`: Strict seller record isolation and ownership verification.
- `CategoryAndCatalogServiceTest`: Catalog filtering, price ranges, search, and sorting.
- `SellerAndAdminServiceTest`: Seller registration workflow, admin approval, and platform metrics.
