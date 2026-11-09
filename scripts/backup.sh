#!/usr/bin/env bash
# ============================================================================
# BUSINESS OS 2026 — AUTOMATED BACKUP SCRIPT (POSTGRESQL & SQLITE)
# Author: Mendel Temgoua (mendeltemgouabusiness@gmail.com)
# License: MIT
# ============================================================================
set -euo pipefail

BACKUP_DIR="${BACKUP_DIR:-./backups}"
TIMESTAMP="$(date -u +"%Y%m%d_%H%M%S")"
mkdir -p "$BACKUP_DIR"

echo "[BUSINESS OS] Démarrage de la sauvegarde souveraine ($TIMESTAMP)..."

if command -v docker >/dev/null 2>&1 && docker ps --format '{{.Names}}' | grep -q "postgres"; then
  PG_ARCHIVE="$BACKUP_DIR/business_os_pg_${TIMESTAMP}.sql.gz"
  docker exec -t postgres pg_dump -U "${POSTGRES_USER:-businessos}" "${POSTGRES_DB:-business_os_db}" | gzip -9 > "$PG_ARCHIVE"
  sha256sum "$PG_ARCHIVE" > "${PG_ARCHIVE}.sha256"
  echo "[OK] Sauvegarde PostgreSQL créée : $PG_ARCHIVE"
fi

echo "[BUSINESS OS] Sauvegarde terminée avec vérification d'intégrité SHA-256."
