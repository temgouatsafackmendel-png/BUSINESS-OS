#!/usr/bin/env bash
# ============================================================================
# BUSINESS OS 2026 — AUTOMATED RESTORE SCRIPT WITH SHA-256 VERIFICATION
# Author: Mendel Temgoua (mendeltemgouabusiness@gmail.com)
# License: MIT
# ============================================================================
set -euo pipefail

if [ $# -lt 1 ]; then
  echo "Usage: ./scripts/restore.sh <path_to_backup.sql.gz>"
  exit 1
fi

ARCHIVE_FILE="$1"
if [ ! -f "$ARCHIVE_FILE" ]; then
  echo "[ERREUR] Fichier introuvable : $ARCHIVE_FILE"
  exit 1
fi

if [ -f "${ARCHIVE_FILE}.sha256" ]; then
  echo "[BUSINESS OS] Vérification de l'empreinte SHA-256..."
  sha256sum -c "${ARCHIVE_FILE}.sha256"
fi

echo "[BUSINESS OS] Restauration de la base de données depuis $ARCHIVE_FILE..."
gunzip -c "$ARCHIVE_FILE" | docker exec -i postgres psql -U "${POSTGRES_USER:-businessos}" -d "${POSTGRES_DB:-business_os_db}"
echo "[OK] Restauration terminée avec succès."
