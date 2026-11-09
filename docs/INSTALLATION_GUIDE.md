# Guide d'Installation, de Déploiement & de Mise à Jour — BUSINESS OS 2026

**Auteur** : Mendel Temgoua (`mendeltemgouabusiness@gmail.com`)  
**Licence** : MIT Open Source

---

## 1. Installation sur Linux (Ubuntu / Debian / RHEL)
```bash
sudo apt update && sudo apt install -y git curl docker.io docker-compose-v2 openjdk-17-jdk
git clone https://github.com/mendeltemgoua/business-os.git
cd business-os
cp .env.example .env
docker compose up -d
```

## 2. Installation sur Windows (Windows 11 / WSL2 & Docker Desktop)
1. Activer WSL2 : `wsl --install` dans PowerShell Administrateur.
2. Installer **Docker Desktop** et **Android Studio** (ou JDK 17).
3. Cloner le dépôt :
   ```powershell
   git clone https://github.com/mendeltemgoua/business-os.git
   cd business-os
   copy .env.example .env
   docker compose up -d
   ```

## 3. Installation sur macOS (Apple Silicon M1/M2/M3/M4 & Intel)
```bash
brew install git openjdk@17
brew install --cask docker
git clone https://github.com/mendeltemgoua/business-os.git
cd business-os
cp .env.example .env
docker compose up -d
```

## 4. Déploiement Serveur Production & HTTPS Automatique (Caddy)
Le fichier `docker-compose.yml` inclut **Caddy 2.8** qui génère et renouvelle automatiquement les certificats TLS/HTTPS Let's Encrypt :
1. Pointer votre domaine DNS (`erp.votre-entreprise.com`) vers l'IP du serveur.
2. Démarrer la stack : `docker compose up -d`.
3. Optionnel — Activer l'IA locale auto-hébergée **Ollama** :
   ```bash
   docker compose --profile ai-local up -d
   docker exec -it ollama ollama pull llama3.2
   ```

## 5. Sauvegarde, Restauration & Mise à Jour Sans Coupure
- **Sauvegarde quotidienne** : `bash ./scripts/backup.sh`
- **Restauration vérifiée** : `bash ./scripts/restore.sh ./backups/business_os_pg_YYYYMMDD.sql.gz`
- **Mise à jour** :
  ```bash
  bash ./scripts/backup.sh
  git pull origin main
  docker compose up -d --build
  ```
