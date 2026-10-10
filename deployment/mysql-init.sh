#!/usr/bin/env bash
# ==============================================================================
# VELMART - MySQL Database Initialization Script
# Usage: bash deployment/mysql-init.sh
# ==============================================================================

set -euo pipefail

DB_NAME="${MYSQL_DATABASE:-velmart_db}"
DB_USER="${MYSQL_USER:-root}"
DB_PASS="${MYSQL_PASSWORD:-}"
DB_HOST="${MYSQL_HOST:-127.0.0.1}"
DB_PORT="${MYSQL_PORT:-3306}"

echo "===================================================================="
echo " Initializing MySQL Database for VELMART [${DB_NAME}]"
echo "===================================================================="

AUTH_FLAG=""
if [ -n "$DB_PASS" ]; then
  AUTH_FLAG="-p${DB_PASS}"
fi

mysql -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" ${AUTH_FLAG} < "$(dirname "$0")/mysql-schema.sql"

echo "✅ VELMART MySQL database and schema successfully initialized!"
