/**
 * VELMART - Shopping Cart Logic
 */

const Cart = {
  cartData: null,

  async init() {
    await this.fetchCart();
  },

  async fetchCart() {
    const container = document.getElementById('cart-container');
    if (!container) return;

    try {
      const res = await API.get('/api/cart');
      if (res && res.success && res.data) {
        this.cartData = res.data;
        this.renderCart(this.cartData, container);
        UI.updateCartCount();
      }
    } catch (err) {
      if (err.status === 401) {
        container.innerHTML = `
          <div class="empty-state">
            <div class="empty-state-icon">🔒</div>
            <div class="empty-state-title">Please Sign In</div>
            <div class="empty-state-text">You need an active VELMART customer account to access your cart.</div>
            <a href="/login.html?redirect=/cart.html" class="btn btn-primary">Sign In to Continue</a>
          </div>
        `;
      } else {
        container.innerHTML = `
          <div class="empty-state">
            <div class="empty-state-icon">⚠️</div>
            <div class="empty-state-title">Error Loading Cart</div>
            <div class="empty-state-text">${err.message}</div>
          </div>
        `;
      }
    }
  },

  renderCart(cart, container) {
    if (!cart.items || cart.items.length === 0) {
      container.innerHTML = `
        <div class="empty-state">
          <div class="empty-state-icon">🛒</div>
          <div class="empty-state-title">Your Cart is Empty</div>
          <div class="empty-state-text">Explore our collection of watches, electronics, and fashion items.</div>
          <a href="/products.html" class="btn btn-primary">Start Shopping</a>
        </div>
      `;
      return;
    }

    let itemsHtml = '';
    cart.items.forEach(item => {
      const isOutOfStock = !item.inStock;
      itemsHtml += `
        <tr>
          <td style="display: flex; gap: 16px; align-items: center;">
            <img src="${item.productImageUrl}" alt="${item.productName}" style="width: 70px; height: 70px; object-fit: cover; border-radius: 8px; border: 1px solid var(--border-color);" onerror="this.src='https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80'" />
            <div>
              <a href="/product-detail.html?id=${item.productId}" style="font-weight: 600; color: var(--primary-navy); font-size: 15px;">
                ${item.productName}
              </a>
              <div style="margin-top: 4px;">
                ${isOutOfStock ? '<span class="badge badge-danger">Out of Stock / Insufficient quantity</span>' : '<span class="badge badge-success">In Stock</span>'}
              </div>
            </div>
          </td>
          <td style="font-weight: 600;">${API.formatINR(item.unitPrice)}</td>
          <td>
            <div style="display: inline-flex; align-items: center; border: 1px solid var(--border-color); border-radius: 6px; background: #ffffff;">
              <button class="btn btn-sm" onclick="Cart.updateQuantity('${item.productId}', ${item.quantity - 1})" style="border: none; padding: 4px 8px;">-</button>
              <span style="padding: 0 10px; font-weight: 600;">${item.quantity}</span>
              <button class="btn btn-sm" onclick="Cart.updateQuantity('${item.productId}', ${item.quantity + 1})" style="border: none; padding: 4px 8px;">+</button>
            </div>
          </td>
          <td style="font-weight: 700; color: var(--accent-blue);">${API.formatINR(item.lineTotal)}</td>
          <td>
            <button class="btn btn-sm btn-secondary" onclick="Cart.removeItem('${item.productId}')" title="Remove item" style="color: var(--danger); border-color: var(--danger-bg);">
              <svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
            </button>
          </td>
        </tr>
      `;
    });

    const freeShippingRemaining = 999 - cart.itemSubtotal;
    let shippingNote = '';
    if (freeShippingRemaining > 0) {
      shippingNote = `<div style="background: var(--warning-bg); color: #92400e; padding: 10px 14px; border-radius: 6px; font-size: 13px; margin-bottom: 16px;">
        Add <strong>${API.formatINR(freeShippingRemaining)}</strong> more to unlock <strong>FREE Delivery</strong>!
      </div>`;
    } else {
      shippingNote = `<div style="background: var(--success-bg); color: var(--success); padding: 10px 14px; border-radius: 6px; font-size: 13px; margin-bottom: 16px; font-weight: 600;">
        🎉 You qualify for FREE Delivery on this order!
      </div>`;
    }

    container.innerHTML = `
      <div style="display: grid; grid-template-columns: 1fr 340px; gap: 30px; margin-top: 20px;">
        <!-- Left: Items list -->
        <div class="card" style="padding: 0; overflow: hidden;">
          <div class="table-responsive">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Product</th>
                  <th>Price</th>
                  <th>Quantity</th>
                  <th>Subtotal</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                ${itemsHtml}
              </tbody>
            </table>
          </div>
          <div style="padding: 16px 20px; display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--border-color); background: #f8fafc;">
            <a href="/products.html" class="btn btn-secondary btn-sm">&larr; Continue Shopping</a>
            <button class="btn btn-outline btn-sm" onclick="Cart.clearCart()">Clear Cart</button>
          </div>
        </div>

        <!-- Right: Summary -->
        <div>
          <div class="card" style="position: sticky; top: 100px;">
            <h3 style="font-size: 18px; font-weight: 700; margin-bottom: 16px; color: var(--primary-navy);">Order Summary</h3>
            ${shippingNote}
            <div style="display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 14px;">
              <span style="color: var(--text-secondary);">Items Subtotal (${cart.totalItems} items):</span>
              <span style="font-weight: 600;">${API.formatINR(cart.itemSubtotal)}</span>
            </div>
            <div style="display: flex; justify-content: space-between; margin-bottom: 16px; font-size: 14px;">
              <span style="color: var(--text-secondary);">Delivery Charges:</span>
              <span style="font-weight: 600;">${cart.shippingFee === 0 ? '<span style="color: var(--success);">FREE</span>' : API.formatINR(cart.shippingFee)}</span>
            </div>
            <div style="border-top: 2px dashed var(--border-color); padding-top: 14px; margin-bottom: 24px; display: flex; justify-content: space-between; font-size: 18px; font-weight: 800;">
              <span>Total Amount:</span>
              <span style="color: var(--accent-blue);">${API.formatINR(cart.totalAmount)}</span>
            </div>

            <a href="/checkout.html" class="btn btn-primary btn-lg btn-block" ${!cart.allInStock ? 'disabled title="Please resolve out of stock items"' : ''}>
              Proceed to Checkout &rarr;
            </a>

            <div style="margin-top: 16px; text-align: center; font-size: 12px; color: var(--text-muted);">
              🔒 100% Safe and Secure Checkout with Cash on Delivery
            </div>
          </div>
        </div>
      </div>
    `;
  },

  async updateQuantity(productId, quantity) {
    try {
      const res = await API.put(`/api/cart/items/${productId}`, { quantity });
      if (res && res.success) {
        this.cartData = res.data;
        const container = document.getElementById('cart-container');
        this.renderCart(this.cartData, container);
        UI.updateCartCount();
      }
    } catch (err) {
      UI.showToast(err.message || 'Failed to update quantity', 'error');
    }
  },

  async removeItem(productId) {
    try {
      const res = await API.delete(`/api/cart/items/${productId}`);
      if (res && res.success) {
        UI.showToast('Item removed from cart', 'info');
        this.cartData = res.data;
        const container = document.getElementById('cart-container');
        this.renderCart(this.cartData, container);
        UI.updateCartCount();
      }
    } catch (err) {
      UI.showToast(err.message || 'Failed to remove item', 'error');
    }
  },

  async clearCart() {
    if (!confirm('Are you sure you want to clear your shopping cart?')) return;
    try {
      await API.delete('/api/cart');
      UI.showToast('Cart cleared', 'info');
      await this.fetchCart();
    } catch (err) {
      UI.showToast(err.message || 'Failed to clear cart', 'error');
    }
  }
};
