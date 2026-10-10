/**
 * VELMART - Seller Dashboard Logic (Stats, Product CRUD, Fulfillment Management)
 */

const SellerDashboard = {
  profile: null,
  products: [],
  orders: [],

  async init() {
    await this.loadProfile();
    await this.loadStats();
    await this.loadProducts();
    await this.loadOrders();
  },

  switchTab(tabName) {
    document.querySelectorAll('.sidebar-link').forEach(link => {
      link.classList.toggle('active', link.dataset.tab === tabName);
    });
    document.querySelectorAll('.tab-pane').forEach(pane => {
      pane.style.display = pane.id === `tab-${tabName}` ? 'block' : 'none';
    });
  },

  async loadProfile() {
    try {
      const res = await API.get('/api/seller/profile');
      if (res && res.success && res.data) {
        this.profile = res.data;
        document.getElementById('seller-business-name').innerText = this.profile.businessName;
        document.getElementById('seller-status-badge').innerText = this.profile.applicationStatus;
        if (this.profile.applicationStatus !== 'APPROVED') {
          document.getElementById('pending-approval-banner').style.display = 'block';
        }
      }
    } catch (e) {
      window.location.href = '/seller-login.html';
    }
  },

  async loadStats() {
    try {
      const res = await API.get('/api/seller/stats');
      if (res && res.success && res.data) {
        const s = res.data;
        document.getElementById('stat-total-products').innerText = s.totalProducts;
        document.getElementById('stat-active-products').innerText = s.activeProducts;
        document.getElementById('stat-out-of-stock').innerText = s.outOfStockProducts;
        document.getElementById('stat-total-orders').innerText = s.totalOrders;
        document.getElementById('stat-pending-fulfillments').innerText = s.pendingFulfillments;
        document.getElementById('stat-revenue').innerText = API.formatINR(s.totalSalesRevenue);
      }
    } catch (e) {}
  },

  async loadProducts() {
    const container = document.getElementById('seller-products-table-body');
    if (!container) return;

    try {
      const res = await API.get('/api/seller/products');
      if (res && res.success && res.data) {
        this.products = res.data;
        this.renderProducts(this.products, container);
      }
    } catch (err) {
      container.innerHTML = `<tr><td colspan="6" style="text-align:center; color:var(--danger);">Error loading products: ${err.message}</td></tr>`;
    }
  },

  renderProducts(products, tbody) {
    if (products.length === 0) {
      tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; padding: 30px;">No products added yet. Click "+ Add New Product" to start selling.</td></tr>`;
      return;
    }

    let html = '';
    products.forEach(p => {
      html += `
        <tr>
          <td style="display: flex; gap: 12px; align-items: center;">
            <img src="${p.imageUrl}" alt="${p.name}" style="width: 44px; height: 44px; object-fit: cover; border-radius: 6px;" onerror="this.src='https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80'" />
            <div>
              <strong>${p.name}</strong>
              <div style="font-size: 12px; color: var(--text-muted);">${p.brand || 'VELMART'} | ${p.categoryId}</div>
            </div>
          </td>
          <td style="font-weight: 600;">${API.formatINR(p.price)}</td>
          <td>
            <input type="number" value="${p.stockQuantity}" min="0" style="width: 70px; padding: 4px 8px; border: 1px solid var(--border-color); border-radius: 4px;" onchange="SellerDashboard.quickUpdateStock('${p.productId}', this.value)" />
          </td>
          <td>
            <span class="badge ${p.active ? 'badge-success' : 'badge-neutral'}">${p.active ? 'Active' : 'Draft'}</span>
          </td>
          <td>
            <button class="btn btn-sm ${p.active ? 'btn-secondary' : 'btn-outline'}" onclick="SellerDashboard.toggleProductStatus('${p.productId}', ${!p.active})">
              ${p.active ? 'Deactivate' : 'Activate'}
            </button>
          </td>
          <td>
            <button class="btn btn-danger btn-sm" onclick="SellerDashboard.deleteProduct('${p.productId}')">Delete</button>
          </td>
        </tr>
      `;
    });
    tbody.innerHTML = html;
  },

  async quickUpdateStock(productId, newQty) {
    try {
      await API.patch(`/api/seller/products/${productId}/status`, {
        stockQuantity: parseInt(newQty, 10)
      });
      UI.showToast('Stock quantity updated', 'success');
      await this.loadStats();
    } catch (err) {
      UI.showToast(err.message || 'Failed to update stock', 'error');
    }
  },

  async toggleProductStatus(productId, newActive) {
    try {
      await API.patch(`/api/seller/products/${productId}/status`, {
        active: newActive
      });
      UI.showToast(`Product ${newActive ? 'activated' : 'deactivated'}`, 'info');
      await this.loadProducts();
      await this.loadStats();
    } catch (err) {
      UI.showToast(err.message || 'Failed to update product', 'error');
    }
  },

  async deleteProduct(productId) {
    if (!confirm('Are you sure you want to permanently delete this product?')) return;
    try {
      await API.delete(`/api/seller/products/${productId}`);
      UI.showToast('Product deleted', 'info');
      await this.loadProducts();
      await this.loadStats();
    } catch (err) {
      UI.showToast(err.message || 'Failed to delete product', 'error');
    }
  },

  openAddProductModal() {
    const modal = document.getElementById('add-product-modal');
    if (modal) modal.classList.add('active');
  },

  closeAddProductModal() {
    const modal = document.getElementById('add-product-modal');
    if (modal) modal.classList.remove('active');
  },

  async submitNewProduct(e) {
    e.preventDefault();
    const data = {
      name: document.getElementById('prod-name').value,
      brand: document.getElementById('prod-brand').value,
      categoryId: document.getElementById('prod-category').value,
      price: parseFloat(document.getElementById('prod-price').value),
      originalPrice: parseFloat(document.getElementById('prod-orig-price').value || document.getElementById('prod-price').value),
      stockQuantity: parseInt(document.getElementById('prod-stock').value, 10),
      imageUrl: document.getElementById('prod-image').value,
      description: document.getElementById('prod-desc').value,
      active: true
    };

    try {
      const res = await API.post('/api/seller/products', data);
      if (res && res.success) {
        UI.showToast('Product published to VELMART catalog!', 'success');
        this.closeAddProductModal();
        e.target.reset();
        await this.loadProducts();
        await this.loadStats();
      }
    } catch (err) {
      UI.showToast(err.message || 'Failed to add product', 'error');
    }
  },

  async loadOrders() {
    const container = document.getElementById('seller-orders-table-body');
    if (!container) return;

    try {
      const res = await API.get('/api/seller/orders');
      if (res && res.success && res.data) {
        this.orders = res.data;
        this.renderOrders(this.orders, container);
      }
    } catch (err) {
      container.innerHTML = `<tr><td colspan="6" style="text-align:center; color:var(--danger);">Error loading orders: ${err.message}</td></tr>`;
    }
  },

  renderOrders(items, tbody) {
    if (items.length === 0) {
      tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; padding: 30px;">No customer orders received yet.</td></tr>`;
      return;
    }

    let html = '';
    items.forEach(item => {
      let badgeClass = 'badge-primary';
      if (item.fulfillmentStatus === 'DELIVERED') badgeClass = 'badge-success';
      if (item.fulfillmentStatus === 'CANCELLED') badgeClass = 'badge-danger';
      if (item.fulfillmentStatus === 'SHIPPED') badgeClass = 'badge-warning';

      html += `
        <tr>
          <td>
            <strong>#${item.orderId}</strong>
            <div style="font-size: 11px; color: var(--text-muted);">${new Date(item.createdAt).toLocaleDateString()}</div>
          </td>
          <td>
            <div style="display: flex; gap: 10px; align-items: center;">
              <img src="${item.productImageSnapshot}" alt="${item.productNameSnapshot}" style="width: 36px; height: 36px; object-fit: cover; border-radius: 4px;" onerror="this.src='https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80'" />
              <div>${item.productNameSnapshot}</div>
            </div>
          </td>
          <td>${item.quantity}</td>
          <td style="font-weight: 700;">${API.formatINR(item.lineTotal)}</td>
          <td>
            <span class="badge ${badgeClass}">${item.fulfillmentStatus}</span>
          </td>
          <td>
            ${item.fulfillmentStatus !== 'DELIVERED' && item.fulfillmentStatus !== 'CANCELLED' ? `
              <select class="form-select" style="padding: 4px 8px; font-size: 12px; width: 130px;" onchange="SellerDashboard.updateFulfillment('${item.orderItemId}', this.value)">
                <option value="">Update Status...</option>
                <option value="PROCESSING">PROCESSING</option>
                <option value="SHIPPED">SHIPPED</option>
                <option value="DELIVERED">DELIVERED</option>
              </select>
            ` : '<span style="color: var(--text-muted); font-size: 12px;">Completed</span>'}
          </td>
        </tr>
      `;
    });
    tbody.innerHTML = html;
  },

  async updateFulfillment(orderItemId, status) {
    if (!status) return;
    try {
      await API.patch(`/api/seller/orders/items/${orderItemId}/fulfillment`, {
        fulfillmentStatus: status
      });
      UI.showToast(`Item fulfillment updated to ${status}`, 'success');
      await this.loadOrders();
      await this.loadStats();
    } catch (err) {
      UI.showToast(err.message || 'Failed to update fulfillment', 'error');
    }
  }
};
