/**
 * VELMART - Product Detail View Logic
 */

const ProductDetail = {
  product: null,
  quantity: 1,

  async init() {
    const params = new URLSearchParams(window.location.search);
    const productId = params.get('id');

    if (!productId) {
      window.location.href = '/products.html';
      return;
    }

    await this.fetchProduct(productId);
  },

  async fetchProduct(productId) {
    const container = document.getElementById('product-detail-container');
    if (!container) return;

    try {
      const res = await API.get(`/api/public/products/${productId}`);
      if (res && res.success && res.data) {
        this.product = res.data;
        document.title = `${this.product.name} - VELMART`;
        this.renderProduct(this.product, container);
      }
    } catch (err) {
      container.innerHTML = `
        <div class="empty-state">
          <div class="empty-state-icon">⚠️</div>
          <div class="empty-state-title">Product not found</div>
          <div class="empty-state-text">${err.message}</div>
          <a href="/products.html" class="btn btn-primary">Browse All Products</a>
        </div>
      `;
    }
  },

  renderProduct(p, container) {
    const isOutOfStock = p.stockQuantity === null || p.stockQuantity <= 0;
    const discountBadge = p.discountPercent && p.discountPercent > 0
      ? `<span class="badge badge-danger">${p.discountPercent}% Discount</span>` : '';

    const originalPriceHtml = p.originalPrice && p.originalPrice > p.price
      ? `<span class="original-price" style="font-size: 18px;">${API.formatINR(p.originalPrice)}</span>` : '';

    // Gallery images
    const images = p.galleryImages && p.galleryImages.length > 0 ? p.galleryImages : [p.imageUrl];
    let thumbnailsHtml = '';
    images.forEach((img, idx) => {
      thumbnailsHtml += `
        <div class="gallery-thumb ${idx === 0 ? 'active' : ''}" onclick="ProductDetail.switchImage('${img}', this)">
          <img src="${img}" alt="Thumbnail ${idx + 1}" onerror="this.src='https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80'" />
        </div>
      `;
    });

    // Specifications map
    let specsHtml = '';
    if (p.specifications && Object.keys(p.specifications).length > 0) {
      specsHtml = `
        <div style="margin-top: 30px;">
          <h3 style="font-size: 18px; font-weight: 700; margin-bottom: 14px; color: var(--primary-navy);">Technical Specifications</h3>
          <table class="data-table" style="background: #ffffff; border-radius: 8px; overflow: hidden;">
            <tbody>
              ${Object.entries(p.specifications).map(([key, val]) => `
                <tr>
                  <td style="font-weight: 600; width: 35%; background: #f8fafc;">${key}</td>
                  <td>${val}</td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
      `;
    }

    container.innerHTML = `
      <div style="display: grid; grid-template-columns: 1fr 1.2fr; gap: 40px; margin-top: 20px;">
        <!-- Left Column: Gallery -->
        <div>
          <div class="card" style="padding: 10px; background: #ffffff; border-radius: var(--radius-md); overflow: hidden; margin-bottom: 16px;">
            <img id="main-product-img" src="${images[0]}" alt="${p.name}" style="width: 100%; height: 420px; object-fit: contain;" onerror="this.src='https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80'" />
          </div>
          <div style="display: flex; gap: 10px; overflow-x: auto;">
            ${thumbnailsHtml}
          </div>
        </div>

        <!-- Right Column: Info & Actions -->
        <div>
          <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px;">
            <span class="badge badge-primary">${p.brand || 'VELMART'}</span>
            <span class="badge badge-neutral">${p.categoryId.toUpperCase()}</span>
            ${discountBadge}
          </div>

          <h1 style="font-size: 28px; font-weight: 800; color: var(--primary-navy); margin-bottom: 16px; line-height: 1.3;">
            ${p.name}
          </h1>

          <div style="display: flex; align-items: baseline; gap: 14px; margin-bottom: 20px; padding: 14px 0; border-top: 1px solid var(--border-color); border-bottom: 1px solid var(--border-color);">
            <span class="current-price" style="font-size: 32px; color: var(--accent-blue); font-weight: 800;">
              ${API.formatINR(p.price)}
            </span>
            ${originalPriceHtml}
            <span style="font-size: 13px; color: var(--text-secondary);">Inclusive of all taxes</span>
          </div>

          <div style="margin-bottom: 24px;">
            <div style="font-size: 14px; font-weight: 600; margin-bottom: 8px; color: var(--text-primary);">Stock Availability:</div>
            ${isOutOfStock
              ? '<span class="badge badge-danger" style="font-size: 14px; padding: 6px 14px;">Out of Stock</span>'
              : `<span class="badge badge-success" style="font-size: 14px; padding: 6px 14px;">In Stock (${p.stockQuantity} units available)</span>`
            }
          </div>

          <!-- Quantity Selector & CTA -->
          <div class="card" style="padding: 20px; background: #f8fafc; border-radius: var(--radius-md); margin-bottom: 24px;">
            <div style="display: flex; align-items: center; gap: 16px; margin-bottom: 18px;">
              <label style="font-size: 14px; font-weight: 600;">Quantity:</label>
              <div style="display: flex; align-items: center; border: 1px solid var(--border-color); border-radius: 6px; background: #ffffff;">
                <button type="button" class="btn btn-sm" onclick="ProductDetail.changeQuantity(-1)" style="border: none; padding: 8px 12px;">-</button>
                <input type="number" id="qty-input" value="1" min="1" max="${p.stockQuantity || 1}" readonly style="width: 50px; text-align: center; border: none; font-weight: 600;" />
                <button type="button" class="btn btn-sm" onclick="ProductDetail.changeQuantity(1)" style="border: none; padding: 8px 12px;">+</button>
              </div>
            </div>

            <div style="display: flex; gap: 12px;">
              <button class="btn btn-primary btn-lg" onclick="ProductDetail.addToCart()" ${isOutOfStock ? 'disabled' : ''} style="flex: 1;">
                <svg width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"/></svg>
                Add to Cart
              </button>
              <button class="btn btn-navy btn-lg" onclick="ProductDetail.buyNow()" ${isOutOfStock ? 'disabled' : ''} style="flex: 1;">
                Buy Now
              </button>
            </div>
          </div>

          <!-- Description -->
          <div>
            <h3 style="font-size: 18px; font-weight: 700; margin-bottom: 10px; color: var(--primary-navy);">Product Overview</h3>
            <p style="color: var(--text-secondary); line-height: 1.7; font-size: 15px;">
              ${p.description}
            </p>
          </div>

          <!-- Specs Table -->
          ${specsHtml}
        </div>
      </div>
    `;
  },

  switchImage(src, thumbEl) {
    const mainImg = document.getElementById('main-product-img');
    if (mainImg) mainImg.src = src;

    document.querySelectorAll('.gallery-thumb').forEach(t => t.classList.remove('active'));
    if (thumbEl) thumbEl.classList.add('active');
  },

  changeQuantity(delta) {
    const qtyInput = document.getElementById('qty-input');
    if (!qtyInput || !this.product) return;

    let current = parseInt(qtyInput.value, 10) || 1;
    let next = current + delta;
    if (next < 1) next = 1;
    if (next > (this.product.stockQuantity || 1)) next = this.product.stockQuantity || 1;

    qtyInput.value = next;
    this.quantity = next;
  },

  async addToCart() {
    if (!UI.currentUser) {
      window.location.href = '/login.html?redirect=' + encodeURIComponent(window.location.pathname + window.location.search);
      return;
    }

    try {
      const res = await API.post('/api/cart/items', {
        productId: this.product.productId,
        quantity: this.quantity
      });
      if (res && res.success) {
        UI.showToast(`Added ${this.quantity} item(s) to your cart!`, 'success');
        UI.updateCartCount();
      }
    } catch (err) {
      UI.showToast(err.message || 'Failed to add product to cart', 'error');
    }
  },

  async buyNow() {
    await this.addToCart();
    window.location.href = '/cart.html';
  }
};
