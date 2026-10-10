/**
 * VELMART - Product Catalog Browsing & Search Logic
 */

const Catalog = {
  products: [],
  categories: [],
  selectedCategory: 'all',
  searchQuery: '',
  minPrice: null,
  maxPrice: null,
  sortBy: 'newest',

  async init() {
    const params = new URLSearchParams(window.location.search);
    this.selectedCategory = params.get('category') || 'all';
    this.searchQuery = params.get('search') || '';

    const searchInput = document.getElementById('search-input');
    if (searchInput && this.searchQuery) {
      searchInput.value = this.searchQuery;
    }

    await this.loadCategories();
    await this.fetchAndRenderProducts();
  },

  async loadCategories() {
    try {
      const res = await API.get('/api/public/categories');
      if (res && res.success && res.data) {
        this.categories = res.data;
        this.renderCategoryFilter();
      }
    } catch (e) {
      console.error('Failed to load categories', e);
    }
  },

  renderCategoryFilter() {
    const container = document.getElementById('category-filter-list');
    if (!container) return;

    let html = `
      <label class="filter-radio-item">
        <input type="radio" name="category-filter" value="all" ${this.selectedCategory === 'all' ? 'checked' : ''} onchange="Catalog.setCategory('all')" />
        <span>All Categories</span>
      </label>
    `;

    this.categories.forEach(cat => {
      const isChecked = this.selectedCategory === cat.categoryId;
      html += `
        <label class="filter-radio-item">
          <input type="radio" name="category-filter" value="${cat.categoryId}" ${isChecked ? 'checked' : ''} onchange="Catalog.setCategory('${cat.categoryId}')" />
          <span>${cat.name}</span>
        </label>
      `;
    });

    container.innerHTML = html;
  },

  setCategory(catId) {
    this.selectedCategory = catId;
    this.fetchAndRenderProducts();
  },

  setSort(sortValue) {
    this.sortBy = sortValue;
    this.fetchAndRenderProducts();
  },

  applyPriceFilter(min, max) {
    this.minPrice = min ? parseFloat(min) : null;
    this.maxPrice = max ? parseFloat(max) : null;
    this.fetchAndRenderProducts();
  },

  async fetchAndRenderProducts() {
    const container = document.getElementById('products-container');
    const resultCountEl = document.getElementById('result-count');
    const pageTitleEl = document.getElementById('catalog-title');

    if (!container) return;

    container.innerHTML = `
      <div class="loading-container" style="grid-column: 1 / -1;">
        <div class="spinner spinner-dark"></div>
        <p>Loading products from VELMART...</p>
      </div>
    `;

    let queryParams = `?sortBy=${this.sortBy}`;
    if (this.selectedCategory && this.selectedCategory !== 'all') {
      queryParams += `&categoryId=${encodeURIComponent(this.selectedCategory)}`;
    }
    if (this.searchQuery) {
      queryParams += `&keyword=${encodeURIComponent(this.searchQuery)}`;
    }
    if (this.minPrice) {
      queryParams += `&minPrice=${this.minPrice}`;
    }
    if (this.maxPrice) {
      queryParams += `&maxPrice=${this.maxPrice}`;
    }

    try {
      const res = await API.get(`/api/public/products${queryParams}`);
      if (res && res.success && res.data) {
        this.products = res.data;

        if (resultCountEl) {
          resultCountEl.innerText = `${this.products.length} product${this.products.length === 1 ? '' : 's'} found`;
        }

        if (pageTitleEl) {
          if (this.selectedCategory !== 'all') {
            const current = this.categories.find(c => c.categoryId === this.selectedCategory);
            pageTitleEl.innerText = current ? current.name : 'Products';
          } else if (this.searchQuery) {
            pageTitleEl.innerText = `Search results for "${this.searchQuery}"`;
          } else {
            pageTitleEl.innerText = 'All Products';
          }
        }

        this.renderProductsList(this.products, container);
      }
    } catch (err) {
      container.innerHTML = `
        <div class="empty-state" style="grid-column: 1 / -1;">
          <div class="empty-state-icon">⚠️</div>
          <div class="empty-state-title">Unable to load products</div>
          <div class="empty-state-text">${err.message}</div>
          <button class="btn btn-primary" onclick="Catalog.fetchAndRenderProducts()">Try Again</button>
        </div>
      `;
    }
  },

  renderProductsList(products, container) {
    if (products.length === 0) {
      container.innerHTML = `
        <div class="empty-state" style="grid-column: 1 / -1;">
          <div class="empty-state-icon">🔍</div>
          <div class="empty-state-title">No matching products found</div>
          <div class="empty-state-text">Try adjusting your filters, category, or search keywords.</div>
          <a href="/products.html" class="btn btn-secondary">Clear Filters</a>
        </div>
      `;
      return;
    }

    let html = '';
    products.forEach(p => {
      const discountTag = p.discountPercent && p.discountPercent > 0
        ? `<div class="product-discount-tag">${p.discountPercent}% OFF</div>` : '';

      const originalPriceHtml = p.originalPrice && p.originalPrice > p.price
        ? `<span class="original-price">${API.formatINR(p.originalPrice)}</span>` : '';

      const isOutOfStock = p.stockQuantity === null || p.stockQuantity <= 0;
      const stockBadge = isOutOfStock
        ? `<span class="badge badge-danger">Out of Stock</span>`
        : `<span class="badge badge-success">In Stock (${p.stockQuantity})</span>`;

      html += `
        <div class="product-card">
          <a href="/product-detail.html?id=${p.productId}" class="product-card-image">
            ${discountTag}
            <img src="${p.imageUrl}" alt="${p.name}" loading="lazy" onerror="this.src='https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80'" />
          </a>
          <div class="product-card-body">
            <div class="product-card-brand">${p.brand || 'VELMART'}</div>
            <a href="/product-detail.html?id=${p.productId}">
              <h3 class="product-card-title">${p.name}</h3>
            </a>
            <div class="stock-status">${stockBadge}</div>
            <div class="product-card-price">
              <span class="current-price">${API.formatINR(p.price)}</span>
              ${originalPriceHtml}
            </div>
            <div style="margin-top: 10px; display: flex; gap: 8px;">
              <a href="/product-detail.html?id=${p.productId}" class="btn btn-secondary btn-sm" style="flex: 1;">View</a>
              <button class="btn btn-primary btn-sm" onclick="Catalog.quickAddToCart('${p.productId}')" ${isOutOfStock ? 'disabled' : ''} style="flex: 1;">
                Add to Cart
              </button>
            </div>
          </div>
        </div>
      `;
    });

    container.innerHTML = html;
  },

  async quickAddToCart(productId) {
    if (!UI.currentUser) {
      window.location.href = '/login.html?redirect=' + encodeURIComponent(window.location.pathname + window.location.search);
      return;
    }

    try {
      const res = await API.post('/api/cart/items', { productId, quantity: 1 });
      if (res && res.success) {
        UI.showToast('Product added to your cart!', 'success');
        UI.updateCartCount();
      }
    } catch (err) {
      UI.showToast(err.message || 'Could not add to cart', 'error');
    }
  }
};
