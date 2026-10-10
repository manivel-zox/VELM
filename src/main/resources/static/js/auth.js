/**
 * VELMART - Authentication & Session Handler
 */

const Auth = {
  async login(email, password, redirectTarget) {
    try {
      const res = await API.post('/api/auth/login', { email, password });
      if (res && res.success) {
        UI.showToast('Login successful! Welcome back, ' + res.data.name, 'success');
        setTimeout(() => {
          if (redirectTarget) {
            window.location.href = redirectTarget;
          } else if (res.data.role === 'ROLE_ADMIN') {
            window.location.href = '/admin-dashboard.html';
          } else if (res.data.role === 'ROLE_SELLER') {
            window.location.href = '/seller-dashboard.html';
          } else {
            window.location.href = '/customer-dashboard.html';
          }
        }, 800);
      }
    } catch (err) {
      UI.showToast(err.message || 'Login failed', 'error');
    }
  },

  async registerCustomer(data) {
    try {
      const res = await API.post('/api/auth/register', data);
      if (res && res.success) {
        UI.showToast('Account created successfully! Please sign in.', 'success');
        setTimeout(() => {
          window.location.href = '/login.html';
        }, 1200);
      }
    } catch (err) {
      UI.showToast(err.message || 'Registration failed', 'error');
    }
  },

  async registerSeller(data) {
    try {
      const res = await API.post('/api/auth/register-seller', data);
      if (res && res.success) {
        UI.showToast('Seller application submitted! Our team will review it shortly.', 'success');
        setTimeout(() => {
          window.location.href = '/seller-login.html';
        }, 1500);
      }
    } catch (err) {
      UI.showToast(err.message || 'Seller registration failed', 'error');
    }
  },

  async forgotPassword(email) {
    try {
      const res = await API.post('/api/auth/forgot-password', { email });
      if (res && res.success) {
        const resetCode = res.data && res.data.resetCode ? res.data.resetCode : '';
        UI.showToast(`Password reset code generated: ${resetCode}`, 'success');
        return resetCode;
      }
    } catch (err) {
      UI.showToast(err.message || 'Request failed', 'error');
      return null;
    }
  },

  async resetPassword(email, token, newPassword) {
    try {
      const res = await API.post('/api/auth/reset-password', { email, token, newPassword });
      if (res && res.success) {
        UI.showToast('Password reset successfully! Please sign in.', 'success');
        setTimeout(() => {
          window.location.href = '/login.html';
        }, 1200);
      }
    } catch (err) {
      UI.showToast(err.message || 'Password reset failed', 'error');
    }
  },

  async logout() {
    try {
      await fetch('/api/auth/logout', { method: 'POST' });
    } catch (e) {}
    UI.showToast('Logged out successfully.', 'info');
    setTimeout(() => {
      window.location.href = '/index.html';
    }, 600);
  }
};
