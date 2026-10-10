/**
 * VELMART - Administrator Control Center Logic
 */

const AdminDashboard = {
  activeTab: 'overview',
  users: [],
  sellers: [],
  categories: [],
  products: [],
  orders: [],
  auditLogs: [],

  async init() {
    await this.loadStats();
    await this.loadUsers();
    await this.loadSellers();
    await this.loadCategories();
    await this.loadProducts();
    await this.loadOrders();
    await this.loadAuditLogs();
  },

  switchTab(tabName) {
    this.activeTab = tabName;
    document.querySelectorAll('.sidebar-link').forEach(link => {
      link.classList.toggle('active', link.dataset.tab === tabName);
    });
    document.querySelectorAll('.tab-pane').forEach(pane => {
      pane.style.display = pane.id === `tab-${tabName}` ? 'block' : 'none';
    });
  },

  async loadStats() {
    try {
      const res = await API.get('/api/admin/stats');
      if (res && res.success && res.data) {
        const s = res.data;
        document.getElementById('stat-admin-customers').innerText = s.totalCustomers;
        document.getElementById('stat-admin-sellers').innerText = s.totalSellers;
        document.getElementById('stat-admin-pending-sellers').innerText = s.pendingSellerApplications;
        document.getElementById('stat-admin-products').innerText = s.totalProducts;
        document.getElementById('stat-admin-orders').innerText = s.totalOrders;
        document.getElementById('stat-admin-revenue').innerText = API.formatINR(s.totalSalesRevenue);
      }
    } catch (e) {
      window.location.href = '/login.html';
    }
  },

  // Users
  async loadUsers() {
    const tbody = document.getElementById('admin-users-table-body');
    if (!tbody) return;

    try {
      const res = await API.get('/api/admin/users');
      if (res && res.success && res.data) {
        this.users = res.data;
        this.renderUsers(this.users, tbody);
      }
    } catch (e) {}
  },

  renderUsers(users, tbody) {
    let html = '';
    users.forEach(u => {
      const isSuspended = u.accountStatus === 'SUSPENDED';
      html += `
        <tr>
          <td>
            <strong>${u.name}</strong>
            <div style="font-size: 12px; color: var(--text-muted);">${u.userId}</div>
          </td>
          <td>${u.email}</td>
          <td>${u.phone}</td>
          <td><span class="badge badge-neutral">${u.role.replace('ROLE_', '')}</span></td>
          <td>
            <span class="badge ${isSuspended ? 'badge-danger' : 'badge-success'}">${u.accountStatus}</span>
          </td>
          <td>
            <button class="btn btn-sm ${isSuspended ? 'btn-primary' : 'btn-danger'}" onclick="AdminDashboard.toggleUserStatus('${u.userId}', '${isSuspended ? 'ACTIVE' : 'SUSPENDED'}')">
              ${isSuspended ? 'Activate' : 'Suspend'}
            </button>
          </td>
        </tr>
      `;
    });
    tbody.innerHTML = html;
  },

  async toggleUserStatus(userId, newStatus) {
    try {
      await API.patch(`/api/admin/users/${userId}/status`, { status: newStatus });
      UI.showToast(`User status updated to ${newStatus}`, 'success');
      await this.loadUsers();
      await this.loadStats();
    } catch (err) {
      UI.showToast(err.message || 'Action failed', 'error');
    }
  },

  // Sellers
  async loadSellers() {
    const tbody = document.getElementById('admin-sellers-table-body');
    if (!tbody) return;

    try {
      const res = await API.get('/api/admin/sellers');
      if (res && res.success && res.data) {
        this.sellers = res.data;
        this.renderSellers(this.sellers, tbody);
      }
    } catch (e) {}
  },

  renderSellers(sellers, tbody) {
    let html = '';
    sellers.forEach(s => {
      let badge = 'badge-warning';
      if (s.applicationStatus === 'APPROVED') badge = 'badge-success';
      if (s.applicationStatus === 'REJECTED') badge = 'badge-danger';

      html += `
        <tr>
          <td>
            <strong>${s.businessName}</strong>
            <div style="font-size: 12px; color: var(--text-muted);">${s.businessAddress || 'No address provided'}</div>
          </td>
          <td>${s.phone}</td>
          <td><span class="badge ${badge}">${s.applicationStatus}</span></td>
          <td style="font-size: 12px; color: var(--text-secondary);">${s.approvalNotes || '-'}</td>
          <td>
            <div style="display: flex; gap: 6px;">
              ${s.applicationStatus !== 'APPROVED' ? `<button class="btn btn-sm btn-primary" onclick="AdminDashboard.reviewSeller('${s.sellerId}', 'APPROVED')">Approve</button>` : ''}
              ${s.applicationStatus !== 'REJECTED' ? `<button class="btn btn-sm btn-danger" onclick="AdminDashboard.reviewSeller('${s.sellerId}', 'REJECTED')">Reject</button>` : ''}
            </div>
          </td>
        </tr>
      `;
    });
    tbody.innerHTML = html;
  },

  async reviewSeller(sellerId, status) {
    const notes = prompt(`Enter review notes for ${status}:`, `Reviewed and ${status} by admin`);
    if (notes === null) return;

    try {
      await API.post(`/api/admin/sellers/${sellerId}/review`, { status, notes });
      UI.showToast(`Seller ${status}`, 'success');
      await this.loadSellers();
      await this.loadStats();
    } catch (err) {
      UI.showToast(err.message || 'Review failed', 'error');
    }
  },

  // Categories
  async loadCategories() {
    const tbody = document.getElementById('admin-categories-table-body');
    if (!tbody) return;

    try {
      const res = await API.get('/api/admin/categories');
      if (res && res.success && res.data) {
        this.categories = res.data;
        this.renderCategories(this.categories, tbody);
      }
    } catch (e) {}
  },

  renderCategories(categories, tbody) {
    let html = '';
    categories.forEach(c => {
      html += `
        <tr>
          <td><strong>${c.name}</strong></td>
          <td style="font-family: monospace; font-size: 12px;">${c.categoryId}</td>
          <td style="font-size: 13px; color: var(--text-secondary);">${c.description || '-'}</td>
          <td><span class="badge ${c.active ? 'badge-success' : 'badge-neutral'}">${c.active ? 'Active' : 'Inactive'}</span></td>
          <td>
            <button class="btn btn-danger btn-sm" onclick="AdminDashboard.deleteCategory('${c.categoryId}')">Delete</button>
          </td>
        </tr>
      `;
    });
    tbody.innerHTML = html;
  },

  openAddCategoryModal() {
    const modal = document.getElementById('add-category-modal');
    if (modal) modal.classList.add('active');
  },

  closeAddCategoryModal() {
    const modal = document.getElementById('add-category-modal');
    if (modal) modal.classList.remove('active');
  },

  async submitNewCategory(e) {
    e.preventDefault();
    const data = {
      categoryId: document.getElementById('cat-id').value,
      name: document.getElementById('cat-name').value,
      description: document.getElementById('cat-desc').value,
      imageUrl: document.getElementById('cat-image').value,
      displayOrder: parseInt(document.getElementById('cat-order').value || 0, 10),
      active: true
    };

    try {
      const res = await API.post('/api/admin/categories', data);
      if (res && res.success) {
        UI.showToast('Category created!', 'success');
        this.closeAddCategoryModal();
        e.target.reset();
        await this.loadCategories();
      }
    } catch (err) {
      UI.showToast(err.message || 'Failed to create category', 'error');
    }
  },

  async deleteCategory(categoryId) {
    if (!confirm('Are you sure you want to delete this category?')) return;
    try {
      await API.delete(`/api/admin/categories/${categoryId}`);
      UI.showToast('Category deleted', 'info');
      await this.loadCategories();
    } catch (err) {
      UI.showToast(err.message || 'Delete failed', 'error');
    }
  },

  // Products
  async loadProducts() {
    const tbody = document.getElementById('admin-products-table-body');
    if (!tbody) return;

    try {
      const res = await API.get('/api/admin/products');
      if (res && res.success && res.data) {
        this.products = res.data;
        this.renderProducts(this.products, tbody);
      }
    } catch (e) {}
  },

  renderProducts(products, tbody) {
    let html = '';
    products.forEach(p => {
      html += `
        <tr>
          <td style="display: flex; gap: 10px; align-items: center;">
            <img src="${p.imageUrl}" alt="${p.name}" style="width: 38px; height: 38px; object-fit: cover; border-radius: 4px;" onerror="this.src='https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80'" />
            <div>
              <strong>${p.name}</strong>
              <div style="font-size: 11px; color: var(--text-muted);">ID: ${p.productId}</div>
            </div>
          </td>
          <td>${p.categoryId}</td>
          <td style="font-weight: 600;">${API.formatINR(p.price)}</td>
          <td>${p.stockQuantity}</td>
          <td>
            <span class="badge ${p.active ? 'badge-success' : 'badge-neutral'}">${p.active ? 'Live' : 'Hidden'}</span>
          </td>
          <td>
            <button class="btn btn-sm ${p.active ? 'btn-secondary' : 'btn-primary'}" onclick="AdminDashboard.toggleProductActive('${p.productId}', ${!p.active})">
              ${p.active ? 'Hide' : 'Publish'}
            </button>
          </td>
        </tr>
      `;
    });
    tbody.innerHTML = html;
  },

  async toggleProductActive(productId, active) {
    try {
      await API.patch(`/api/admin/products/${productId}/toggle-active`, { active });
      UI.showToast(`Product visibility updated to ${active ? 'Live' : 'Hidden'}`, 'info');
      await this.loadProducts();
      await this.loadStats();
    } catch (err) {
      UI.showToast(err.message || 'Action failed', 'error');
    }
  },

  // Orders
  async loadOrders() {
    const tbody = document.getElementById('admin-orders-table-body');
    if (!tbody) return;

    try {
      const res = await API.get('/api/admin/orders');
      if (res && res.success && res.data) {
        this.orders = res.data;
        this.renderOrders(this.orders, tbody);
      }
    } catch (e) {}
  },

  renderOrders(orders, tbody) {
    let html = '';
    orders.forEach(o => {
      let badgeClass = 'badge-primary';
      if (o.orderStatus === 'DELIVERED') badgeClass = 'badge-success';
      if (o.orderStatus === 'CANCELLED') badgeClass = 'badge-danger';
      if (o.orderStatus === 'SHIPPED') badgeClass = 'badge-warning';

      const itemCount = (o.items && Array.isArray(o.items)) ? o.items.length : 0;

      html += `
        <tr>
          <td>
            <strong>#${o.orderId}</strong>
            <div style="font-size: 11px; color: var(--text-muted);">${new Date(o.createdAt).toLocaleDateString()}</div>
          </td>
          <td>
            <div>${o.customerName}</div>
            <div style="font-size: 12px; color: var(--text-muted);">${o.customerEmail}</div>
          </td>
          <td style="font-weight: 700;">${API.formatINR(o.totalAmount)}</td>
          <td>${o.paymentMethod}</td>
          <td><span class="badge ${badgeClass}">${o.orderStatus}</span></td>
          <td>
            <button class="btn btn-secondary btn-sm" onclick="AdminDashboard.viewOrderProducts('${o.orderId}')">
              📦 View Items (${itemCount})
            </button>
          </td>
          <td>
            <select class="form-select" style="padding: 4px 8px; font-size: 12px; width: 130px;" onchange="AdminDashboard.updateOrderStatus('${o.orderId}', this.value)">
              <option value="">Change Status...</option>
              <option value="CONFIRMED">CONFIRMED</option>
              <option value="PROCESSING">PROCESSING</option>
              <option value="SHIPPED">SHIPPED</option>
              <option value="DELIVERED">DELIVERED</option>
              <option value="CANCELLED">CANCELLED</option>
            </select>
          </td>
        </tr>
      `;
    });
    tbody.innerHTML = html;
  },

  async updateOrderStatus(orderId, orderStatus) {
    if (!orderStatus) return;
    try {
      await API.patch(`/api/admin/orders/${orderId}/status`, { orderStatus });
      UI.showToast(`Order status updated to ${orderStatus}`, 'success');
      await this.loadOrders();
      await this.loadStats();
    } catch (err) {
      UI.showToast(err.message || 'Status update failed', 'error');
    }
  },

  viewOrderProducts(orderId) {
    const order = this.orders ? this.orders.find(o => o.orderId === orderId) : null;
    if (!order) {
      UI.showToast('Order not found', 'error');
      return;
    }

    const modalTitle = document.getElementById('order-modal-title');
    const modalBody = document.getElementById('order-modal-body');
    const modal = document.getElementById('order-details-modal');

    if (modalTitle) modalTitle.innerText = `Order #${order.orderId} — Purchased Products`;

    let addrText = 'Not specified';
    if (order.deliveryAddressSnapshot) {
      const a = order.deliveryAddressSnapshot;
      const parts = [a.fullName, a.street, a.city, a.state, a.postalCode || a.pincode, a.country].filter(Boolean);
      addrText = parts.length > 0 ? parts.join(', ') : 'Not specified';
    }

    let itemsHtml = '';
    if (order.items && order.items.length > 0) {
      order.items.forEach(item => {
        let fBadge = 'badge-neutral';
        if (item.fulfillmentStatus === 'DELIVERED') fBadge = 'badge-success';
        if (item.fulfillmentStatus === 'SHIPPED') fBadge = 'badge-warning';
        if (item.fulfillmentStatus === 'CANCELLED') fBadge = 'badge-danger';

        itemsHtml += `
          <div style="display: flex; gap: 14px; align-items: center; padding: 12px 0; border-bottom: 1px solid var(--border-color);">
            <img src="${item.productImageSnapshot || ''}" alt="${item.productNameSnapshot || ''}" style="width: 56px; height: 56px; object-fit: cover; border-radius: 6px; border: 1px solid var(--border-color);" onerror="this.src='https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80'" />
            <div style="flex: 1;">
              <div style="font-weight: 700; font-size: 14px; color: var(--primary-navy);">${item.productNameSnapshot || 'Product'}</div>
              <div style="font-size: 12px; color: var(--text-muted); margin-top: 2px;">
                <span>Product ID: <code>${item.productId || '-'}</code></span> &bull; 
                <span>Seller ID: <code>${item.sellerId || '-'}</code></span>
              </div>
              <div style="font-size: 12px; color: var(--text-secondary); margin-top: 4px;">
                Unit Price: <strong>${API.formatINR(item.unitPriceSnapshot || 0)}</strong> &times; Quantity: <strong>${item.quantity}</strong>
              </div>
            </div>
            <div style="text-align: right;">
              <div style="font-weight: 700; font-size: 15px; color: var(--accent-blue);">${API.formatINR(item.lineTotal || (item.unitPriceSnapshot * item.quantity))}</div>
              <span class="badge ${fBadge}" style="margin-top: 4px; font-size: 11px;">
                ${item.fulfillmentStatus || 'PROCESSING'}
              </span>
            </div>
          </div>
        `;
      });
    } else {
      itemsHtml = '<p style="color: var(--text-muted); padding: 12px 0;">No product items found for this order.</p>';
    }

    if (modalBody) {
      modalBody.innerHTML = `
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 14px; margin-bottom: 18px; padding-bottom: 14px; border-bottom: 1px solid var(--border-color);">
          <div>
            <div style="font-size: 11px; text-transform: uppercase; color: var(--text-muted); font-weight: 700;">Customer Details</div>
            <div style="font-weight: 700; font-size: 14px; color: var(--primary-navy); margin-top: 2px;">${order.customerName || '-'}</div>
            <div style="font-size: 12px; color: var(--text-secondary);">${order.customerEmail || '-'}</div>
            <div style="font-size: 12px; color: var(--text-secondary);">${order.contactPhone || '-'}</div>
          </div>
          <div>
            <div style="font-size: 11px; text-transform: uppercase; color: var(--text-muted); font-weight: 700;">Shipping Address</div>
            <div style="font-size: 13px; line-height: 1.4; color: var(--text-secondary); margin-top: 2px;">${addrText}</div>
          </div>
          <div>
            <div style="font-size: 11px; text-transform: uppercase; color: var(--text-muted); font-weight: 700;">Order & Payment</div>
            <div style="font-size: 12px; margin-top: 2px;">Date: <strong>${new Date(order.createdAt).toLocaleString()}</strong></div>
            <div style="font-size: 12px;">Method: <strong>${order.paymentMethod || 'COD'}</strong> (${order.paymentStatus || 'PENDING'})</div>
            <div style="font-size: 12px; margin-top: 2px;">Status: <span class="badge badge-primary">${order.orderStatus}</span></div>
          </div>
        </div>

        <div style="margin-bottom: 14px;">
          <h4 style="font-size: 14px; font-weight: 700; color: var(--primary-navy); margin-bottom: 10px;">Purchased Products (${order.items ? order.items.length : 0})</h4>
          <div style="background: #f8fafc; border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 0 16px;">
            ${itemsHtml}
          </div>
        </div>

        <div style="display: flex; justify-content: flex-end; margin-top: 14px;">
          <div style="width: 280px; font-size: 13px; background: #ffffff; border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 12px 16px;">
            <div style="display: flex; justify-content: space-between; padding: 3px 0;">
              <span style="color: var(--text-secondary);">Items Subtotal:</span>
              <span style="font-weight: 600;">${API.formatINR(order.itemSubtotal || order.totalAmount)}</span>
            </div>
            <div style="display: flex; justify-content: space-between; padding: 3px 0;">
              <span style="color: var(--text-secondary);">Delivery Fee:</span>
              <span style="font-weight: 600;">${API.formatINR(order.shippingFee || 0)}</span>
            </div>
            <div style="display: flex; justify-content: space-between; padding: 6px 0; border-top: 1px solid var(--border-color); margin-top: 4px; font-weight: 700; font-size: 15px; color: var(--primary-navy);">
              <span>Grand Total:</span>
              <span style="color: var(--accent-blue);">${API.formatINR(order.totalAmount)}</span>
            </div>
          </div>
        </div>
      `;
    }

    if (modal) modal.classList.add('active');
  },

  closeOrderDetailsModal() {
    const modal = document.getElementById('order-details-modal');
    if (modal) modal.classList.remove('active');
  },

  // Audit Logs
  async loadAuditLogs() {
    const tbody = document.getElementById('admin-audit-table-body');
    if (!tbody) return;

    try {
      const res = await API.get('/api/admin/audit-logs');
      if (res && res.success && res.data) {
        this.auditLogs = res.data;
        this.renderAuditLogs(this.auditLogs, tbody);
      }
    } catch (e) {}
  },

  renderAuditLogs(logs, tbody) {
    let html = '';
    logs.forEach(l => {
      html += `
        <tr>
          <td style="font-size: 12px; color: var(--text-muted);">${new Date(l.timestamp).toLocaleString()}</td>
          <td>
            <strong>${l.actorEmail}</strong>
            <span class="badge badge-neutral" style="font-size: 10px; margin-left: 4px;">${l.role}</span>
          </td>
          <td><span class="badge badge-primary">${l.action}</span></td>
          <td>${l.targetType}:${l.targetId || '-'}</td>
          <td style="font-size: 13px;">${l.details || '-'}</td>
          <td style="font-family: monospace; font-size: 12px;">${l.ipAddress}</td>
        </tr>
      `;
    });
    tbody.innerHTML = html;
  }
};
