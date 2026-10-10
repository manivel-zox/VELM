/**
 * VELMART - Core API & Network Client
 */

const API = {
  baseUrl: '',

  formatINR(amount) {
    if (amount === undefined || amount === null) return '₹0.00';
    return '₹' + Number(amount).toLocaleString('en-IN', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    });
  },

  async request(endpoint, options = {}) {
    const defaultHeaders = {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    };

    const config = {
      ...options,
      headers: {
        ...defaultHeaders,
        ...options.headers
      }
    };

    try {
      const response = await fetch(this.baseUrl + endpoint, config);
      const data = await response.json().catch(() => null);

      if (!response.ok) {
        let errorMsg = (data && data.message) ? data.message : `HTTP error ${response.status}`;
        if (response.status === 401 && !endpoint.includes('/api/auth/me')) {
          // Unauthenticated redirect if accessing protected resources
          if (window.location.pathname.includes('dashboard') ||
              window.location.pathname.includes('checkout') ||
              window.location.pathname.includes('cart')) {
            window.location.href = '/login.html?redirect=' + encodeURIComponent(window.location.pathname);
          }
        }
        const err = new Error(errorMsg);
        err.status = response.status;
        err.data = data;
        throw err;
      }

      return data;
    } catch (error) {
      console.error(`API Error on [${endpoint}]:`, error);
      throw error;
    }
  },

  get(endpoint) {
    return this.request(endpoint, { method: 'GET' });
  },

  post(endpoint, body) {
    return this.request(endpoint, {
      method: 'POST',
      body: JSON.stringify(body)
    });
  },

  put(endpoint, body) {
    return this.request(endpoint, {
      method: 'PUT',
      body: JSON.stringify(body)
    });
  },

  patch(endpoint, body) {
    return this.request(endpoint, {
      method: 'PATCH',
      body: JSON.stringify(body)
    });
  },

  delete(endpoint) {
    return this.request(endpoint, { method: 'DELETE' });
  }
};
