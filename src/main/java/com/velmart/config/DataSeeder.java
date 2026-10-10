package com.velmart.config;

import com.velmart.model.*;
import com.velmart.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${velmart.data-seeder.enabled:true}")
    private boolean seederEnabled;

    @Value("${velmart.admin.initial-email:admin@velmart.com}")
    private String adminEmail;

    @Value("${velmart.admin.initial-password:Admin@Velmart2026!}")
    private String adminPassword;

    @Value("${velmart.admin.initial-name:Velmart Platform Admin}")
    private String adminName;

    public DataSeeder(UserRepository userRepository,
                      SellerProfileRepository sellerProfileRepository,
                      CategoryRepository categoryRepository,
                      ProductRepository productRepository,
                      AddressRepository addressRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.sellerProfileRepository = sellerProfileRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.addressRepository = addressRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!seederEnabled) {
            log.info("DataSeeder is disabled via configuration.");
            return;
        }

        log.info("Initializing VELMART Data Seeder...");
        String now = Instant.now().toString();

        // 1. Seed Admin User
        if (userRepository.findByNormalizedEmail(adminEmail).isEmpty()) {
            User admin = new User(
                    UUID.randomUUID().toString(),
                    adminName,
                    adminEmail.toLowerCase(),
                    "+91 9876543210",
                    passwordEncoder.encode(adminPassword),
                    Role.ROLE_ADMIN,
                    AccountStatus.ACTIVE,
                    now,
                    now
            );
            userRepository.save(admin);
            log.info("Provisioned default Administrator account: {}", adminEmail);
        }

        // 2. Seed Demo Seller
        String sellerEmail = "seller@velmart.com";
        String sellerId = "seller-velmart-watches-001";
        if (userRepository.findByNormalizedEmail(sellerEmail).isEmpty()) {
            String sellerUserId = UUID.randomUUID().toString();
            User sellerUser = new User(
                    sellerUserId,
                    "Rajesh Sharma",
                    sellerEmail,
                    "+91 9876543211",
                    passwordEncoder.encode("Seller@Velmart2026!"),
                    Role.ROLE_SELLER,
                    AccountStatus.ACTIVE,
                    now,
                    now
            );
            userRepository.save(sellerUser);

            SellerProfile profile = new SellerProfile(
                    sellerId,
                    sellerUserId,
                    "Velmart Timepieces & Electronics",
                    "Plot 42, Electronics City Phase 1, Bengaluru, Karnataka 560100",
                    "+91 9876543211",
                    ApplicationStatus.APPROVED,
                    "Verified flagship vendor partner",
                    now,
                    now
            );
            sellerProfileRepository.save(profile);
            log.info("Provisioned default Seller account: {}", sellerEmail);
        }

        // 3. Seed Demo Customer
        String customerEmail = "customer@velmart.com";
        String customerUserId = "cust-velmart-001";
        if (userRepository.findByNormalizedEmail(customerEmail).isEmpty()) {
            User customer = new User(
                    customerUserId,
                    "Priya Patel",
                    customerEmail,
                    "+91 9876543212",
                    passwordEncoder.encode("Customer@Velmart2026!"),
                    Role.ROLE_CUSTOMER,
                    AccountStatus.ACTIVE,
                    now,
                    now
            );
            userRepository.save(customer);

            Address defaultAddress = new Address();
            defaultAddress.setAddressId(UUID.randomUUID().toString());
            defaultAddress.setUserId(customerUserId);
            defaultAddress.setRecipientName("Priya Patel");
            defaultAddress.setPhone("+91 9876543212");
            defaultAddress.setAddressLine1("Flat 402, Sunshine Heights, MG Road");
            defaultAddress.setAddressLine2("Near Metro Station");
            defaultAddress.setCity("Mumbai");
            defaultAddress.setState("Maharashtra");
            defaultAddress.setPostalCode("400001");
            defaultAddress.setCountry("India");
            defaultAddress.setIsDefault(true);
            defaultAddress.setCreatedAt(now);
            addressRepository.save(defaultAddress);

            log.info("Provisioned default Customer account: {}", customerEmail);
        }

        // 4. Seed Product Categories
        seedCategories();

        // 5. Seed Catalog Products (including comprehensive Watches)
        seedProducts(sellerId);

        log.info("VELMART Data Seeder finished successfully.");
    }

    private void seedCategories() {
        List<Category> categories = List.of(
                new Category("watches", "Watches", "Premium analog, chronograph, sports, and smartwatches for men and women.", "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80", true, 1, Instant.now().toString()),
                new Category("electronics", "Electronics", "High-performance headphones, audio gear, and modern accessories.", "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop&q=80", true, 2, Instant.now().toString()),
                new Category("mens-fashion", "Men's Fashion", "Trendy apparel, classic formalwear, and casual wardrobe essentials.", "https://images.unsplash.com/photo-1617137984095-74e4e5e3613f?w=600&auto=format&fit=crop&q=80", true, 3, Instant.now().toString()),
                new Category("womens-fashion", "Women's Fashion", "Chic dresses, elegant ethnic wear, tops, and festive fashion.", "https://images.unsplash.com/photo-1483985988355-763728e1935b?w=600&auto=format&fit=crop&q=80", true, 4, Instant.now().toString()),
                new Category("footwear", "Footwear", "Comfortable running sneakers, classic leather formals, and casual shoes.", "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&auto=format&fit=crop&q=80", true, 5, Instant.now().toString()),
                new Category("bags-accessories", "Bags and Accessories", "Genuine leather backpacks, sleek wallets, sunglasses, and belts.", "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600&auto=format&fit=crop&q=80", true, 6, Instant.now().toString()),
                new Category("home-living", "Home and Living", "Cozy textiles, minimalist decor, and modern living essentials.", "https://images.unsplash.com/photo-1513694203232-719a280e022f?w=600&auto=format&fit=crop&q=80", true, 7, Instant.now().toString())
        );

        for (Category cat : categories) {
            if (categoryRepository.findById(cat.getCategoryId()).isEmpty()) {
                categoryRepository.save(cat);
            }
        }
    }

    private void seedProducts(String sellerId) {
        if (!productRepository.findAll().isEmpty()) {
            return;
        }

        String now = Instant.now().toString();

        // --- WATCHES (8 realistic sample models) ---
        createProduct("vel-watch-01", sellerId, "Classic Analog Watch",
                "Timeless classic analog watch featuring a clean minimalist dial, scratch-resistant mineral glass, and water resistance up to 30 meters. Perfect for business and daily wear.",
                "watches", 1299.00, 1999.00, 35, 45,
                "https://images.unsplash.com/photo-1524805444758-089113d48a6d?w=700&auto=format&fit=crop&q=80",
                "VELMART Heritage",
                Map.of("Dial Color", "Midnight Black", "Strap Material", "Genuine Leather", "Dial Type", "Analog", "Water Resistance", "30M / 3 ATM", "Warranty", "1 Year"));

        createProduct("vel-watch-02", sellerId, "Stainless Steel Men's Watch",
                "Rugged yet sophisticated stainless steel timepiece with dual-tone brushed bracelet and luminescent hands for easy low-light readability.",
                "watches", 2499.00, 3499.00, 29, 30,
                "https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=700&auto=format&fit=crop&q=80",
                "Chronos Royal",
                Map.of("Dial Color", "Silver Sunray", "Strap Material", "316L Stainless Steel", "Dial Type", "Analog with Date", "Water Resistance", "50M / 5 ATM", "Warranty", "2 Years"));

        createProduct("vel-watch-03", sellerId, "Minimalist Leather Strap Watch",
                "Ultra-slim design with contemporary indices and premium Italian brown calfskin leather strap. Subtle elegance for modern professionals.",
                "watches", 1799.00, 2499.00, 28, 25,
                "https://images.unsplash.com/photo-1533139502658-0198f920d8e8?w=700&auto=format&fit=crop&q=80",
                "Nordic Minimal",
                Map.of("Dial Color", "Eggshell White", "Strap Material", "Italian Leather", "Dial Type", "Minimalist 2-Hand", "Water Resistance", "Splash Proof", "Warranty", "1 Year"));

        createProduct("vel-watch-04", sellerId, "Luxury-Style Chronograph Watch",
                "High-precision multi-dial chronograph watch with tachymeter bezel, stopwatch functionality, and bold sporty aesthetics.",
                "watches", 3999.00, 5999.00, 33, 18,
                "https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=700&auto=format&fit=crop&q=80",
                "Apex Precision",
                Map.of("Dial Color", "Deep Navy Blue", "Strap Material", "Solid Steel Link", "Dial Type", "3-Subdial Chronograph", "Water Resistance", "100M / 10 ATM", "Warranty", "2 Years"));

        createProduct("vel-watch-05", sellerId, "Digital Sports Watch",
                "Shock-resistant all-weather digital sports watch featuring backlight, dual time zones, countdown timer, and rugged resin band.",
                "watches", 899.00, 1299.00, 31, 50,
                "https://images.unsplash.com/photo-1508685096489-7aacd43bd3b1?w=700&auto=format&fit=crop&q=80",
                "ToughGear",
                Map.of("Dial Color", "Digital LCD", "Strap Material", "Durable Resin", "Dial Type", "Digital Multi-Function", "Water Resistance", "50M", "Warranty", "1 Year"));

        createProduct("vel-watch-06", sellerId, "Women's Rose Gold Watch",
                "Exquisite rose-gold plated mesh bracelet watch accented with crystal hour markers and a mother-of-pearl dial shimmer.",
                "watches", 2199.00, 2999.00, 27, 22,
                "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=700&auto=format&fit=crop&q=80",
                "Aura Luxe",
                Map.of("Dial Color", "Rose Pearl", "Strap Material", "Rose Gold Mesh", "Dial Type", "Analog Crystal", "Water Resistance", "30M", "Warranty", "1 Year"));

        createProduct("vel-watch-07", sellerId, "Smartwatch Active Pro",
                "Next-gen smartwatch featuring vivid 1.43\" AMOLED display, continuous heart-rate & SpO2 tracking, 100+ sports modes, and 10-day battery life.",
                "watches", 2999.00, 4499.00, 33, 40,
                "https://images.unsplash.com/photo-1579586337278-3befd40fd17a?w=700&auto=format&fit=crop&q=80",
                "TechPulse",
                Map.of("Display", "1.43 AMOLED HD", "Strap Material", "Liquid Silicone", "Connectivity", "Bluetooth 5.3", "Battery Life", "Up to 10 Days", "Water Resistance", "IP68 Waterproof"));

        createProduct("vel-watch-08", sellerId, "Casual Everyday Watch",
                "Lightweight everyday casual watch with military-inspired olive canvas strap, bold Arabic numerals, and reliable Japanese quartz movement.",
                "watches", 799.00, 999.00, 20, 60,
                "https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=700&auto=format&fit=crop&q=80",
                "Urban Scout",
                Map.of("Dial Color", "Matte Khaki", "Strap Material", "Woven Canvas", "Dial Type", "Analog 12/24 Hr", "Water Resistance", "30M", "Warranty", "1 Year"));

        // --- ELECTRONICS ---
        createProduct("vel-elec-01", sellerId, "Wireless Noise-Cancelling Headphones",
                "Immersive over-ear Bluetooth headphones with hybrid active noise cancellation, deep bass acoustic tuning, and 40-hour playback.",
                "electronics", 3499.00, 4999.00, 30, 25,
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=700&auto=format&fit=crop&q=80",
                "SonicFlow",
                Map.of("Battery Life", "40 Hours", "Connectivity", "Bluetooth 5.2 & 3.5mm Aux", "Noise Cancellation", "Active Hybrid ANC"));

        createProduct("vel-elec-02", sellerId, "Portable Bluetooth Speaker 20W",
                "Compact 360-degree surround sound speaker with punchy bass, IPX7 waterproof rating, and 12-hour continuous battery.",
                "electronics", 1899.00, 2499.00, 24, 35,
                "https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=700&auto=format&fit=crop&q=80",
                "BoomSound",
                Map.of("Output", "20W RMS", "Battery", "2600 mAh", "Waterproof", "IPX7"));

        // --- MEN'S FASHION ---
        createProduct("vel-men-01", sellerId, "Oxford Cotton Slim Fit Shirt",
                "Breathable 100% pure premium cotton formal-casual shirt with button-down collar and durable stitching.",
                "mens-fashion", 1199.00, 1699.00, 29, 40,
                "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=700&auto=format&fit=crop&q=80",
                "VELMART Threads",
                Map.of("Material", "100% Cotton", "Fit", "Slim Fit", "Pattern", "Solid Sky Blue"));

        // --- WOMEN'S FASHION ---
        createProduct("vel-women-01", sellerId, "Floral Print A-Line Midi Dress",
                "Flowy floral midi dress crafted with soft breathable viscose fabric and subtle waist tie detail for effortless summer style.",
                "womens-fashion", 1499.00, 2199.00, 32, 28,
                "https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?w=700&auto=format&fit=crop&q=80",
                "Flora Bloom",
                Map.of("Material", "Viscose Crepe", "Length", "Midi Length", "Care", "Machine Wash Cold"));

        // --- FOOTWEAR ---
        createProduct("vel-foot-01", sellerId, "Lightweight Pro Running Shoes",
                "Ergonomic breathable mesh running shoes equipped with responsive EVA cushioned sole for high impact absorption.",
                "footwear", 2299.00, 3299.00, 30, 30,
                "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=700&auto=format&fit=crop&q=80",
                "StrideMax",
                Map.of("Sole Material", "EVA & Non-slip Rubber", "Upper", "Breathable Mesh", "Closure", "Lace-Up"));

        // --- BAGS & ACCESSORIES ---
        createProduct("vel-bag-01", sellerId, "Vintage Full-Grain Leather Backpack",
                "Handcrafted vintage brown leather backpack with dedicated 15.6\" padded laptop compartment, antique brass zippers, and ergonomic shoulder straps.",
                "bags-accessories", 2799.00, 3999.00, 30, 20,
                "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=700&auto=format&fit=crop&q=80",
                "Urban Hide",
                Map.of("Material", "Full-Grain Genuine Leather", "Capacity", "22 Litres", "Laptop Sleeve", "Up to 15.6 Inch"));

        // --- HOME & LIVING ---
        createProduct("vel-home-01", sellerId, "Ceramic Aroma Diffuser & Table Lamp",
                "Minimalist matte ceramic ultrasonic essential oil diffuser with soothing warm LED ambient nightlight.",
                "home-living", 1399.00, 1899.00, 26, 35,
                "https://images.unsplash.com/photo-1513694203232-719a280e022f?w=700&auto=format&fit=crop&q=80",
                "Aura Living",
                Map.of("Material", "Ceramic & BPA-free PP", "Capacity", "300 ml", "Lighting", "7 Soft Color LEDs"));

        log.info("Seeded catalog products across all major categories.");
    }

    private void createProduct(String id, String sellerId, String name, String desc, String catId,
                               double price, double origPrice, int discount, int stock,
                               String imgUrl, String brand, Map<String, String> specs) {
        Product p = new Product();
        p.setProductId(id);
        p.setSellerId(sellerId);
        p.setName(name);
        p.setDescription(desc);
        p.setCategoryId(catId);
        p.setPrice(price);
        p.setOriginalPrice(origPrice);
        p.setDiscountPercent(discount);
        p.setStockQuantity(stock);
        p.setImageUrl(imgUrl);
        p.setGalleryImages(List.of(imgUrl));
        p.setBrand(brand);
        p.setSpecifications(specs);
        p.setActive(true);
        p.setCreatedAt(Instant.now().toString());
        p.setUpdatedAt(Instant.now().toString());
        productRepository.save(p);
    }
}
