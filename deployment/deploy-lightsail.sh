#!/usr/bin/env bash
# ==============================================================================
# VELMART - Automated Deployment Script for AWS Lightsail (Ubuntu 22.04 / 24.04)
# Run as root or with sudo: sudo bash deploy-lightsail.sh
# ==============================================================================

set -euo pipefail

echo "===================================================================="
echo " Starting VELMART Deployment on AWS Lightsail Ubuntu..."
echo "===================================================================="

# 1. Update OS Packages
echo "[1/7] Updating system packages..."
apt-get update -y && apt-get upgrade -y

# 2. Install Java 21, Git, Nginx, and Certbot
echo "[2/7] Installing OpenJDK 21, Nginx, Certbot..."
apt-get install -y openjdk-21-jdk git nginx certbot python3-certbot-nginx curl ufw

# 3. Create Application Directory
echo "[3/7] Setting up /opt/velmart directory..."
mkdir -p /opt/velmart
chown -R ubuntu:ubuntu /opt/velmart

# 4. Copy systemd service file
echo "[4/7] Installing systemd service..."
cp /tmp/velmart.service /etc/systemd/system/velmart.service 2>/dev/null || true
systemctl daemon-reload

# 5. Configure Nginx
echo "[5/7] Configuring Nginx reverse proxy..."
cp /tmp/nginx.conf /etc/nginx/sites-available/velmart 2>/dev/null || true
ln -sf /etc/nginx/sites-available/velmart /etc/nginx/sites-enabled/velmart
rm -f /etc/nginx/sites-enabled/default
nginx -t && systemctl restart nginx

# 6. Configure Firewall (UFW)
echo "[6/7] Securing Firewall (UFW)..."
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
ufw --force enable

# 7. Start application service
echo "[7/7] Starting VELMART service..."
systemctl enable velmart
systemctl restart velmart || true

echo "===================================================================="
echo " VELMART deployment script completed!"
echo " Check logs with: sudo journalctl -u velmart -f"
echo "===================================================================="
