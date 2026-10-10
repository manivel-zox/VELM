/**
 * VELMART - UI Components, Header/Footer Injection, Toast & Modal System
 */

const UI = {
  currentUser: null,

  async initCommon() {
    this.renderHeader();
    this.renderFooter();
    await this.checkAuthStatus();
    await this.loadCategoryNav();
    await this.updateCartCount();
  },

  showToast(message, type = 'info') {
    let container = document.getElementById('toast-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'toast-container';
      document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
      <div class="toast-message">${message}</div>
      <button class="toast-close" onclick="this.parentElement.remove()">&times;</button>
    `;

    container.appendChild(toast);

    setTimeout(() => {
      if (toast.parentElement) {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => toast.remove(), 300);
      }
    }, 4000);
  },

  async checkAuthStatus() {
    try {
      const res = await API.get('/api/auth/me');
      if (res && res.success && res.data) {
        this.currentUser = res.data;
        this.updateNavForAuthUser(res.data);
      } else {
        this.currentUser = null;
        this.updateNavForGuest();
      }
    } catch (e) {
      this.currentUser = null;
      this.updateNavForGuest();
    }
  },

  async updateCartCount() {
    const badge = document.getElementById('cart-badge');
    if (!badge) return;

    if (!this.currentUser || this.currentUser.role !== 'ROLE_CUSTOMER') {
      badge.style.display = 'none';
      return;
    }

    try {
      const res = await API.get('/api/cart');
      if (res && res.success && res.data) {
        const count = res.data.totalItems || 0;
        badge.innerText = count;
        badge.style.display = count > 0 ? 'flex' : 'none';
      }
    } catch (e) {
      badge.style.display = 'none';
    }
  },

  async loadCategoryNav() {
    const navList = document.getElementById('category-nav-list');
    if (!navList) return;

    try {
      const res = await API.get('/api/public/categories');
      if (res && res.success && res.data) {
        const urlParams = new URLSearchParams(window.location.search);
        const currentCat = urlParams.get('category') || 'all';

        let html = `<li><a href="/products.html" class="category-nav-link ${currentCat === 'all' && window.location.pathname.includes('products') ? 'active' : ''}">All Products</a></li>`;
        res.data.forEach(cat => {
          const isActive = currentCat === cat.categoryId;
          html += `<li><a href="/products.html?category=${cat.categoryId}" class="category-nav-link ${isActive ? 'active' : ''}">${cat.name}</a></li>`;
        });
        navList.innerHTML = html;
      }
    } catch (e) {
      console.warn("Failed to load category navigation:", e);
    }
  },

  updateNavForAuthUser(user) {
    const authLinks = document.getElementById('nav-auth-links');
    if (!authLinks) return;

    let dashboardLink = '/customer-dashboard.html';
    let roleBadge = '<span class="badge badge-primary">Customer</span>';

    if (user.role === 'ROLE_SELLER') {
      dashboardLink = '/seller-dashboard.html';
      roleBadge = '<span class="badge badge-warning">Seller</span>';
    } else if (user.role === 'ROLE_ADMIN') {
      dashboardLink = '/admin-dashboard.html';
      roleBadge = '<span class="badge badge-danger">Admin</span>';
    }

    authLinks.innerHTML = `
      <a href="${dashboardLink}" class="nav-link-item">
        <svg width="16" height="16" fill="currentColor" viewBox="0 0 24 24"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>
        <span>${user.name.split(' ')[0]}</span>
        ${roleBadge}
      </a>
      <button onclick="Auth.logout()" class="btn btn-sm btn-outline" style="border-color: rgba(255,255,255,0.2); color: #e2e8f0;">Logout</button>
    `;
  },

  updateNavForGuest() {
    const authLinks = document.getElementById('nav-auth-links');
    if (!authLinks) return;

    authLinks.innerHTML = `
      <a href="/login.html" class="nav-link-item">Sign In</a>
      <a href="/register.html" class="btn btn-sm btn-primary">Register</a>
    `;
  },

  renderHeader() {
    const headerPlaceholder = document.getElementById('app-header');
    if (!headerPlaceholder) return;

    headerPlaceholder.innerHTML = `
      <header class="header">
        <div class="top-bar">
          <div class="container top-bar-content">
            <div>Free Delivery on orders above ₹999 across India | 100% Genuine Products</div>
            <div class="top-bar-links">
              <a href="/seller-login.html">Seller Hub</a>
              <a href="/admin-dashboard.html">Admin Portal</a>
              <a href="/contact.html">Help & Support</a>
            </div>
          </div>
        </div>

        <div class="navbar-main">
          <div class="container navbar-wrapper">
            <a href="/index.html" class="navbar-logo">
              <span class="logo-badge">V</span>
              <span>VELMART</span>
            </a>

            <div class="navbar-search">
              <form class="search-form" onsubmit="event.preventDefault(); window.location.href='/products.html?search='+encodeURIComponent(document.getElementById('nav-search-input').value);">
                <input type="text" id="nav-search-input" class="search-input" placeholder="Search for watches, electronics, fashion, bags..." required />
                <button type="submit" class="search-btn">Search</button>
              </form>
            </div>

            <div class="navbar-actions">
              <div id="nav-auth-links" style="display: flex; align-items: center; gap: 12px;"></div>
              <a href="/cart.html" class="nav-link-item cart-link">
                <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"/></svg>
                <span>Cart</span>
                <span id="cart-badge" class="cart-count" style="display: none;">0</span>
              </a>
            </div>
          </div>
        </div>

        <nav class="category-nav">
          <div class="container">
            <ul id="category-nav-list" class="category-nav-list">
              <!-- Dynamically populated -->
            </ul>
          </div>
        </nav>
      </header>
    `;
  },

  renderFooter() {
    const footerPlaceholder = document.getElementById('app-footer');
    if (!footerPlaceholder) return;

    footerPlaceholder.innerHTML = `
      <footer class="footer">
        <div class="container">
          <div class="footer-grid">
            <div>
              <a href="/index.html" class="footer-logo">
                <span class="logo-badge">V</span>
                <span>VELMART</span>
              </a>
              <p style="margin-top: 10px; font-size: 14px; line-height: 1.6; color: #94a3b8;">
                India's modern e-commerce shopping destination for watches, premium fashion, lifestyle gadgets, and home essentials. Fast shipping, easy cash on delivery, and genuine quality guaranteed.
              </p>
            </div>

            <div>
              <h4 class="footer-heading">Shop Categories</h4>
              <ul class="footer-links">
                <li><a href="/products.html?category=watches">Watches Collection</a></li>
                <li><a href="/products.html?category=electronics">Electronics & Audio</a></li>
                <li><a href="/products.html?category=mens-fashion">Men's Apparel</a></li>
                <li><a href="/products.html?category=womens-fashion">Women's Fashion</a></li>
                <li><a href="/products.html?category=footwear">Footwear</a></li>
              </ul>
            </div>

            <div>
              <h4 class="footer-heading">Customer Care</h4>
              <ul class="footer-links">
                <li><a href="/customer-dashboard.html">Track Order</a></li>
                <li><a href="/customer-dashboard.html">My Account</a></li>
                <li><a href="/about.html">About VELMART</a></li>
                <li><a href="/contact.html">Contact Us</a></li>
                <li><a href="/terms.html">Shipping & Returns</a></li>
              </ul>
            </div>

            <div>
              <h4 class="footer-heading">Partner & Legal</h4>
              <ul class="footer-links">
                <li><a href="/seller-register.html">Sell on VELMART</a></li>
                <li><a href="/seller-login.html">Seller Dashboard</a></li>
                <li><a href="/privacy.html">Privacy Policy</a></li>
                <li><a href="/terms.html">Terms of Service</a></li>
                <li><a href="/admin-dashboard.html">Administrator</a></li>
              </ul>
            </div>
          </div>

          <div class="footer-bottom">
            <div>&copy; ${new Date().getFullYear()} VELMART E-Commerce Ltd. All rights reserved. Powered by Java 21, Spring Boot & AWS DynamoDB.</div>
            <div>Secure Cash on Delivery & SSL Encrypted Checkout</div>
          </div>
        </div>
      </footer>
    `;
  }
};

document.addEventListener('DOMContentLoaded', () => {
  UI.initCommon();
});
