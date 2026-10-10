/**
 * VELMART - Customer Dashboard Logic (Orders, Tracking, Addresses, Profile)
 */

const CustomerDashboard = {
  activeTab: 'orders',

  async init() {
    await this.loadProfile();
    await this.loadOrders();
    await this.loadAddresses();

    const urlParams = new URLSearchParams(window.location.search);
    const tab = urlParams.get('tab');
    if (tab) {
      this.switchTab(tab);
    }
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

  async loadProfile() {
    try {
      const res = await API.get('/api/customer/profile');
      if (res && res.success && res.data) {
        const u = res.data;
        document.getElementById('profile-display-name').innerText = u.name;
        document.getElementById('profile-display-email').innerText = u.email;
        document.getElementById('profile-name-input').value = u.name;
        document.getElementById('profile-phone-input').value = u.phone;
        document.getElementById('profile-avatar').innerText = u.name.charAt(0).toUpperCase();
      }
    } catch (e) {
      window.location.href = '/login.html';
    }
  },

  async updateProfile(e) {
    e.preventDefault();
    const data = {
      name: document.getElementById('profile-name-input').value,
      phone: document.getElementById('profile-phone-input').value,
      currentPassword: document.getElementById('profile-curr-pass').value,
      newPassword: document.getElementById('profile-new-pass').value
    };

    try {
      const res = await API.put('/api/customer/profile', data);
      if (res && res.success) {
        UI.showToast('Profile updated successfully!', 'success');
        document.getElementById('profile-curr-pass').value = '';
        document.getElementById('profile-new-pass').value = '';
        await this.loadProfile();
      }
    } catch (err) {
      UI.showToast(err.message || 'Profile update failed', 'error');
    }
  },

  async loadOrders() {
    const container = document.getElementById('orders-list-container');
    if (!container) return;

    try {
      const res = await API.get('/api/customer/orders');
      if (res && res.success && res.data) {
        this.renderOrders(res.data, container);
      }
    } catch (err) {
      container.innerHTML = `<p style="color: var(--danger);">Failed to load order history.</p>`;
    }
  },

  renderOrders(orders, container) {
    if (orders.length === 0) {
      container.innerHTML = `
        <div class="empty-state">
          <div class="empty-state-icon">📦</div>
          <div class="empty-state-title">No Orders Placed Yet</div>
          <div class="empty-state-text">Explore our collection of watches and fashion products!</div>
          <a href="/products.html" class="btn btn-primary">Start Shopping</a>
        </div>
      `;
      return;
    }

    let html = '<div style="display: flex; flex-direction: column; gap: 20px;">';
    orders.forEach(order => {
      let statusBadge = `<span class="badge badge-primary">${order.orderStatus}</span>`;
      if (order.orderStatus === 'DELIVERED') statusBadge = `<span class="badge badge-success">DELIVERED</span>`;
      if (order.orderStatus === 'CANCELLED') statusBadge = `<span class="badge badge-danger">CANCELLED</span>`;
      if (order.orderStatus === 'SHIPPED') statusBadge = `<span class="badge badge-warning">SHIPPED</span>`;

      let itemsSummaryHtml = '';
      order.items.forEach(item => {
        itemsSummaryHtml += `
          <div style="display: flex; gap: 12px; align-items: center; margin-top: 10px;">
            <img src="${item.productImageSnapshot}" alt="${item.productNameSnapshot}" style="width: 50px; height: 50px; object-fit: cover; border-radius: 6px;" onerror="this.src='https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80'" />
            <div style="flex: 1;">
              <div style="font-weight: 600; font-size: 14px;">${item.productNameSnapshot}</div>
              <div style="font-size: 12px; color: var(--text-muted);">Qty: ${item.quantity} &times; ${API.formatINR(item.unitPriceSnapshot)}</div>
            </div>
            <div style="font-weight: 600; font-size: 14px;">${API.formatINR(item.lineTotal)}</div>
          </div>
        `;
      });

      const canCancel = order.orderStatus === 'PLACED' || order.orderStatus === 'CONFIRMED' || order.orderStatus === 'PROCESSING';

      html += `
        <div class="card" style="padding: 20px;">
          <div style="display: flex; justify-content: space-between; align-items: flex-start; border-bottom: 1px solid var(--border-color); padding-bottom: 14px; margin-bottom: 14px;">
            <div>
              <div style="font-size: 16px; font-weight: 700; color: var(--primary-navy);">Order #${order.orderId}</div>
              <div style="font-size: 12px; color: var(--text-muted); margin-top: 2px;">Placed on: ${new Date(order.createdAt).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })}</div>
            </div>
            <div style="text-align: right;">
              <div>${statusBadge}</div>
              <div style="font-size: 16px; font-weight: 800; color: var(--accent-blue); margin-top: 4px;">${API.formatINR(order.totalAmount)}</div>
            </div>
          </div>

          <!-- Items list -->
          <div>${itemsSummaryHtml}</div>

          <!-- Order Tracking Progress -->
          <div class="timeline" style="margin: 24px 0 16px;">
            <div class="timeline-step ${order.orderStatus !== 'CANCELLED' ? 'completed' : ''}">
              <div class="step-circle">1</div>
              <div class="step-label">Placed</div>
            </div>
            <div class="timeline-step ${['CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED'].includes(order.orderStatus) ? 'completed' : ''}">
              <div class="step-circle">2</div>
              <div class="step-label">Confirmed</div>
            </div>
            <div class="timeline-step ${['PROCESSING', 'SHIPPED', 'DELIVERED'].includes(order.orderStatus) ? 'completed' : ''}">
              <div class="step-circle">3</div>
              <div class="step-label">Processing</div>
            </div>
            <div class="timeline-step ${['SHIPPED', 'DELIVERED'].includes(order.orderStatus) ? 'completed' : ''}">
              <div class="step-circle">4</div>
              <div class="step-label">Shipped</div>
            </div>
            <div class="timeline-step ${order.orderStatus === 'DELIVERED' ? 'completed' : ''}">
              <div class="step-circle">5</div>
              <div class="step-label">Delivered</div>
            </div>
          </div>

          <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--border-color); padding-top: 14px; margin-top: 14px; font-size: 13px;">
            <div>
              <strong>Payment:</strong> Cash on Delivery (${order.paymentStatus})
            </div>
            <div style="display: flex; gap: 10px;">
              ${canCancel ? `<button class="btn btn-danger btn-sm" onclick="CustomerDashboard.cancelOrder('${order.orderId}')">Cancel Order</button>` : ''}
              <a href="/order-confirmation.html?orderId=${order.orderId}" class="btn btn-secondary btn-sm">View Invoice</a>
            </div>
          </div>
        </div>
      `;
    });
    html += '</div>';

    container.innerHTML = html;
  },

  async cancelOrder(orderId) {
    const reason = prompt("Please provide a reason for cancellation (optional):", "Changed my mind");
    if (reason === null) return;

    try {
      const res = await API.post(`/api/customer/orders/${orderId}/cancel`, { reason });
      if (res && res.success) {
        UI.showToast('Order cancelled successfully.', 'info');
        await this.loadOrders();
      }
    } catch (err) {
      UI.showToast(err.message || 'Could not cancel order', 'error');
    }
  },

  async loadAddresses() {
    const container = document.getElementById('dashboard-address-container');
    if (!container) return;

    try {
      const res = await API.get('/api/customer/addresses');
      if (res && res.success && res.data) {
        this.renderAddresses(res.data, container);
      }
    } catch (err) {
      container.innerHTML = `<p style="color: var(--danger);">Failed to load addresses.</p>`;
    }
  },

  renderAddresses(addresses, container) {
    if (addresses.length === 0) {
      container.innerHTML = `
        <div class="empty-state">
          <div class="empty-state-icon">📍</div>
          <div class="empty-state-title">No Saved Addresses</div>
          <p class="empty-state-text">Add your delivery addresses for seamless checkout.</p>
          <button class="btn btn-primary" onclick="CustomerDashboard.openAddAddressModal()">+ Add New Address</button>
        </div>
      `;
      return;
    }

    let html = `
      <div style="display: flex; justify-content: flex-end; margin-bottom: 16px;">
        <button class="btn btn-primary btn-sm" onclick="CustomerDashboard.openAddAddressModal()">+ Add New Address</button>
      </div>
      <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px;">
    `;

    addresses.forEach(addr => {
      html += `
        <div class="card" style="padding: 18px; position: relative;">
          ${addr.isDefault ? '<span class="badge badge-primary" style="position: absolute; top: 12px; right: 12px;">Default</span>' : ''}
          <div style="font-weight: 700; font-size: 15px; color: var(--primary-navy); margin-bottom: 4px;">${addr.recipientName}</div>
          <div style="font-size: 13px; color: var(--text-muted); margin-bottom: 10px;">${addr.phone}</div>
          <div style="font-size: 13px; color: var(--text-secondary); line-height: 1.5; margin-bottom: 16px;">
            ${addr.addressLine1}${addr.addressLine2 ? ', ' + addr.addressLine2 : ''}<br>
            ${addr.city}, ${addr.state} - <strong>${addr.postalCode}</strong>
          </div>
          <div style="display: flex; gap: 8px; border-top: 1px solid var(--border-color); padding-top: 12px;">
            ${!addr.isDefault ? `<button class="btn btn-outline btn-sm" onclick="CustomerDashboard.setDefaultAddress('${addr.addressId}')" style="flex: 1;">Set Default</button>` : ''}
            <button class="btn btn-danger btn-sm" onclick="CustomerDashboard.deleteAddress('${addr.addressId}')" style="padding: 4px 10px;">Delete</button>
          </div>
        </div>
      `;
    });
    html += '</div>';

    container.innerHTML = html;
  },

  openAddAddressModal() {
    const modal = document.getElementById('address-modal');
    if (modal) modal.classList.add('active');
  },

  closeAddAddressModal() {
    const modal = document.getElementById('address-modal');
    if (modal) modal.classList.remove('active');
  },

  async submitNewAddress(e) {
    e.preventDefault();
    const data = {
      recipientName: document.getElementById('addr-name').value,
      phone: document.getElementById('addr-phone').value,
      addressLine1: document.getElementById('addr-line1').value,
      addressLine2: document.getElementById('addr-line2').value,
      city: document.getElementById('addr-city').value,
      state: document.getElementById('addr-state').value,
      postalCode: document.getElementById('addr-pin').value,
      country: 'India',
      isDefault: document.getElementById('addr-default').checked
    };

    try {
      const res = await API.post('/api/customer/addresses', data);
      if (res && res.success) {
        UI.showToast('Address added successfully!', 'success');
        this.closeAddAddressModal();
        await this.loadAddresses();
      }
    } catch (err) {
      UI.showToast(err.message || 'Failed to save address', 'error');
    }
  },

  async setDefaultAddress(addressId) {
    try {
      await API.put(`/api/customer/addresses/${addressId}/default`, {});
      UI.showToast('Default address updated', 'success');
      await this.loadAddresses();
    } catch (err) {
      UI.showToast(err.message || 'Failed to update', 'error');
    }
  },

  async deleteAddress(addressId) {
    if (!confirm('Are you sure you want to delete this address?')) return;
    try {
      await API.delete(`/api/customer/addresses/${addressId}`);
      UI.showToast('Address removed', 'info');
      await this.loadAddresses();
    } catch (err) {
      UI.showToast(err.message || 'Failed to delete address', 'error');
    }
  }
};
