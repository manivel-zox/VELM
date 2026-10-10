/**
 * VELMART - Checkout & Order Placement Logic
 */

const Checkout = {
  addresses: [],
  selectedAddressId: null,
  cart: null,
  idempotencyKey: null,

  async init() {
    this.idempotencyKey = 'idemp_' + Date.now() + '_' + Math.random().toString(36).substring(2, 9);
    await this.loadCart();
    await this.loadAddresses();
  },

  async loadCart() {
    try {
      const res = await API.get('/api/cart');
      if (res && res.success && res.data) {
        this.cart = res.data;
        if (!this.cart.items || this.cart.items.length === 0) {
          window.location.href = '/cart.html';
          return;
        }
        if (!this.cart.allInStock) {
          UI.showToast('Some items in your cart are out of stock. Please update your cart before checking out.', 'warning');
          window.location.href = '/cart.html';
          return;
        }
        this.renderOrderSummary();
      }
    } catch (e) {
      window.location.href = '/login.html?redirect=/checkout.html';
    }
  },

  async loadAddresses() {
    const container = document.getElementById('address-list-container');
    if (!container) return;

    try {
      const res = await API.get('/api/customer/addresses');
      if (res && res.success && res.data) {
        this.addresses = res.data;
        this.renderAddresses(this.addresses, container);
      }
    } catch (err) {
      container.innerHTML = `<p style="color: var(--danger);">Failed to load addresses. ${err.message}</p>`;
    }
  },

  renderAddresses(addresses, container) {
    if (addresses.length === 0) {
      container.innerHTML = `
        <div style="background: #f8fafc; border: 1px dashed var(--border-color); padding: 20px; border-radius: 8px; text-align: center;">
          <p style="color: var(--text-secondary); margin-bottom: 12px;">You do not have any saved delivery addresses.</p>
          <button class="btn btn-primary btn-sm" onclick="Checkout.openAddAddressModal()">+ Add New Delivery Address</button>
        </div>
      `;
      this.selectedAddressId = null;
      return;
    }

    let html = '<div style="display: flex; flex-direction: column; gap: 12px;">';
    addresses.forEach((addr, idx) => {
      const isSelected = this.selectedAddressId ? this.selectedAddressId === addr.addressId : (addr.isDefault || idx === 0);
      if (isSelected) this.selectedAddressId = addr.addressId;

      html += `
        <label class="card" style="cursor: pointer; border-color: ${isSelected ? 'var(--accent-blue)' : 'var(--border-color)'}; background: ${isSelected ? 'var(--accent-blue-light)' : '#ffffff'}; padding: 16px; display: flex; gap: 14px; align-items: flex-start;">
          <input type="radio" name="delivery_address" value="${addr.addressId}" ${isSelected ? 'checked' : ''} onchange="Checkout.selectAddress('${addr.addressId}')" style="margin-top: 4px;" />
          <div style="flex: 1;">
            <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 4px;">
              <strong style="font-size: 15px; color: var(--primary-navy);">${addr.recipientName}</strong>
              <span style="font-size: 13px; color: var(--text-secondary);">${addr.phone}</span>
              ${addr.isDefault ? '<span class="badge badge-primary">Default</span>' : ''}
            </div>
            <div style="font-size: 14px; color: var(--text-secondary); line-height: 1.5;">
              ${addr.addressLine1}${addr.addressLine2 ? ', ' + addr.addressLine2 : ''}, ${addr.city}, ${addr.state} - <strong>${addr.postalCode}</strong>, ${addr.country}
            </div>
          </div>
        </label>
      `;
    });
    html += '</div>';
    html += `
      <div style="margin-top: 14px;">
        <button class="btn btn-secondary btn-sm" onclick="Checkout.openAddAddressModal()">+ Add Another Address</button>
      </div>
    `;

    container.innerHTML = html;
  },

  selectAddress(addressId) {
    this.selectedAddressId = addressId;
    this.renderAddresses(this.addresses, document.getElementById('address-list-container'));
  },

  renderOrderSummary() {
    const summaryContainer = document.getElementById('checkout-items-summary');
    const totalContainer = document.getElementById('checkout-totals');
    if (!this.cart) return;

    if (summaryContainer) {
      let html = '';
      this.cart.items.forEach(item => {
        html += `
          <div style="display: flex; justify-content: space-between; align-items: center; gap: 12px; margin-bottom: 12px; font-size: 14px;">
            <div style="display: flex; align-items: center; gap: 10px;">
              <img src="${item.productImageUrl}" alt="${item.productName}" style="width: 44px; height: 44px; object-fit: cover; border-radius: 6px;" onerror="this.src='https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80'" />
              <div>
                <div style="font-weight: 600; color: var(--primary-navy);">${item.productName}</div>
                <div style="color: var(--text-muted); font-size: 12px;">Qty: ${item.quantity} &times; ${API.formatINR(item.unitPrice)}</div>
              </div>
            </div>
            <div style="font-weight: 700;">${API.formatINR(item.lineTotal)}</div>
          </div>
        `;
      });
      summaryContainer.innerHTML = html;
    }

    if (totalContainer) {
      totalContainer.innerHTML = `
        <div style="display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 14px;">
          <span style="color: var(--text-secondary);">Items Subtotal:</span>
          <span style="font-weight: 600;">${API.formatINR(this.cart.itemSubtotal)}</span>
        </div>
        <div style="display: flex; justify-content: space-between; margin-bottom: 14px; font-size: 14px;">
          <span style="color: var(--text-secondary);">Delivery Fee:</span>
          <span style="font-weight: 600;">${this.cart.shippingFee === 0 ? '<span style="color: var(--success);">FREE</span>' : API.formatINR(this.cart.shippingFee)}</span>
        </div>
        <div style="border-top: 2px dashed var(--border-color); padding-top: 14px; display: flex; justify-content: space-between; font-size: 18px; font-weight: 800;">
          <span>Grand Total:</span>
          <span style="color: var(--accent-blue);">${API.formatINR(this.cart.totalAmount)}</span>
        </div>
      `;
    }
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
        this.selectedAddressId = res.data.addressId;
        await this.loadAddresses();
      }
    } catch (err) {
      UI.showToast(err.message || 'Failed to save address', 'error');
    }
  },

  async placeOrder() {
    if (!this.selectedAddressId) {
      UI.showToast('Please select or add a delivery address first.', 'warning');
      return;
    }

    const phoneInput = document.getElementById('checkout-contact-phone');
    const contactPhone = phoneInput ? phoneInput.value : '';

    if (!contactPhone || contactPhone.trim().length < 10) {
      UI.showToast('Please enter a valid 10-digit contact phone number for delivery confirmation.', 'warning');
      if (phoneInput) phoneInput.focus();
      return;
    }

    const placeBtn = document.getElementById('place-order-btn');
    if (placeBtn) {
      placeBtn.disabled = true;
      placeBtn.innerHTML = `<span class="spinner"></span> Processing Order...`;
    }

    try {
      const payload = {
        addressId: this.selectedAddressId,
        contactPhone: contactPhone.trim(),
        paymentMethod: 'CASH_ON_DELIVERY',
        idempotencyKey: this.idempotencyKey
      };

      const res = await API.post('/api/checkout', payload);
      if (res && res.success && res.data) {
        UI.showToast('Order confirmed successfully!', 'success');
        sessionStorage.setItem('last_order', JSON.stringify(res.data));
        setTimeout(() => {
          window.location.href = `/order-confirmation.html?orderId=${res.data.orderId}`;
        }, 1000);
      }
    } catch (err) {
      UI.showToast(err.message || 'Order placement failed. Please try again.', 'error');
      if (placeBtn) {
        placeBtn.disabled = false;
        placeBtn.innerHTML = `Confirm Cash on Delivery Order`;
      }
    }
  }
};
