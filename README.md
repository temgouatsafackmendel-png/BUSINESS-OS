# BUSINESS OS 2026 — Enterprise Management Operating System

[![License: MIT](https://img.shields.io/badge/License-MIT-39FF88.svg)](./LICENSE)
[![Author: Mendel Temgoua](https://img.shields.io/badge/Author-Mendel%20Temgoua-16C784.svg)](mailto:mendeltemgouabusiness@gmail.com)
[![Architecture: Multi-Tenant](https://img.shields.io/badge/Architecture-Multi--Tenant%20%E2%80%A2%20OHADA-090D12.svg)](#architecture)
[![AI: Pluggable & Optional](https://img.shields.io/badge/AI-Optional%20%E2%80%A2%20Ollama%20%E2%80%A2%20Local-38BDF8.svg)](#business-ai)

> **Conçu et développé par Mendel Temgoua** (`mendeltemgouabusiness@gmail.com`)  
> Logiciel ERP Open Source sous licence **MIT**, 100 % gratuit en licence, auto-hébergeable, multi-entreprises (Multi-Tenant), conforme **SYSCOHADA Révisé / IFRS** et doté d'une architecture **AI-Ready** entièrement désactivable et interchangeable.

---

## 1. Vision & Objectifs Non Négociables

**BUSINESS OS** permet à toute entreprise (PME, ETI, cabinet comptable, groupe multi-filiales) de piloter l'intégralité de ses opérations sans dépendre d'un fournisseur cloud propriétaire ni d'un abonnement obligatoire :

- **Licence MIT** : Code source ouvert, modifiable, redistribuable et utilisable commercialement sans redevance.
- **Zéro Lock-In** : Base de données contrôlée à 100 % par l'utilisateur (SQLite locale hors-ligne sur Android + PostgreSQL 16 auto-hébergé) et exportation intégrale en `JSON` / `CSV`.
- **Continuité Sans IA** : L'application fonctionne à 100 % de ses capacités sans aucune connexion à un fournisseur d'IA externe.
- **Multi-Tenant Natif** : Cloisonnement strict des données par `organizationId`, gestion de plusieurs sociétés, espaces de travail (`workspaces`) et rôles RBAC.

---

## 2. Architecture & Arborescence du Dépôt

```text
business-os/
├── app/                                      # Application Android Native (Kotlin & Jetpack Compose M3)
│   └── src/
│       ├── main/java/com/example/
│       │   ├── MainActivity.kt               # Shell adaptatif Multi-Tenant & Navigation centrale
│       │   ├── data/
│       │   │   ├── ai/AiProviderEngine.kt    # Interface AIProvider (Disabled, Local, Ollama, OpenAI)
│       │   │   └── local/
│       │   │       ├── Entities.kt           # Schéma relationnel complet (15 tables isolées par orgId)
│       │   │       ├── BusinessOsDao.kt      # Requêtes réactives Flow, agrégations & transactions
│       │   │       └── BusinessOsDatabase.kt # Moteur SQLite Room + Initialisation Multi-Sociétés
│       │   └── ui/
│       │       ├── components/               # Système de design Business OS (#090D12 / #39FF88)
│       │       ├── screens/                  # Les 24 modules ERP regroupés en 7 pôles métiers
│       │       ├── theme/                    # Palette officielle, Mode Sombre/Clair & Typographie
│       │       └── viewmodel/                # Logique métier, Comptabilité OHADA & Export JSON
│       └── test/java/com/example/            # Tests Unitaires, Financiers, Garde-Fous IA & Robolectric
├── prisma/
│   └── schema.prisma                         # Modèle relationnel PostgreSQL 16 complet (36 tables ERP)
├── scripts/
│   ├── backup.sh                             # Sauvegarde automatisée & chiffrée (PostgreSQL & SQLite)
│   └── restore.sh                            # Procédure de restauration vérifiée avec contrôle SHA-256
├── docs/
│   ├── INSTALLATION_GUIDE.md                 # Guides Windows, Linux, macOS, Serveur, Caddy HTTPS & MAJ
│   └── ci-workflow.yml                       # Pipeline CI/CD GitHub Actions (Tests, Build APK, Audit)
├── docker-compose.yml                        # Stack Auto-Hébergée (PostgreSQL 16, Redis 7, Caddy, Ollama)
├── .env.example                              # Variables d'environnement documentées (100% optionnelles)
├── CONTRIBUTING.md                           # Guide de contribution Open Source
├── SECURITY.md                               # Politique de sécurité OWASP & divulgation responsable
├── THIRD_PARTY_LICENSES.md                   # Vérification des licences des dépendances (Apache 2.0 / MIT)
└── LICENSE                                   # Licence MIT — Copyright (c) 2026 Mendel Temgoua
```

---

## 3. Les 24 Modules Intégrés

| Pôle Métier | Modules Inclus | Fonctionnalités Clés |
| :--- | :--- | :--- |
| **1. Pilotage** | `Dashboard`, `Analytics`, `Notifications` | 14 KPIs temps réel, graphiques Revenus/Dépenses, filtres temporels (`7J`, `30J`, `Trimestre`, `Année`), widgets personnalisables. |
| **2. Finances** | `Finances`, `Trésorerie`, `Paiements` | Comptes Banque (`521`), Mobile Money (`552`), Caisse (`571`), connecteurs MTN MoMo, Orange Money, Campay, Money Fusion, Stripe, PayPal, Virement. **Interdiction de suppression silencieuse** (contre-passation obligatoire). |
| **3. Comptabilité** | `Comptabilité OHADA`, `Fiscalité` | Plan comptable SYSCOHADA Révisé (Classes 1 à 7), Journaux (`VE`, `AC`, `BQ`, `CA`, `OD`), Grand Livre, Balance équilibrée, Bilan, Compte de résultat, TVA collectée/déductible. |
| **4. CRM & Ventes** | `CRM`, `Prospects`, `Clients`, `Ventes`, `Facturation`, `Marketing` | Pipeline d'opportunités, **conversion Prospect $\rightarrow$ Client en 1 clic sans perte d'historique**, Devis (`DEV`), Factures (`FAC`), Commandes (`CMD`), Avoirs (`AVO`), Contrats (`CTR`). |
| **5. Opérations** | `Produits & Services`, `Stocks`, `Achats`, `Fournisseurs` | Catalogue SKU, calcul de marge unitaire, alertes de seuil critique, traçabilité des mouvements `IN / OUT`, fournisseurs OHADA (`401`). |
| **6. RH & Projets** | `Ressources Humaines`, `Projets`, `Tâches` | Contrats (`CDI`, `CDD`, `CONSULTANT`), paie mensuelle, congés, Kanban des tâches (`TODO`, `IN_PROGRESS`, `REVIEW`, `DONE`), budgets. |
| **7. GED, Auto & IA** | `Documents`, `Automatisations`, `Business AI` | Coffre documentaire Local/S3 avec empreinte `SHA-256`, moteur de workflows avec approbation humaine, **Business AI** (`Disabled`, `Local MIT`, `Ollama`, `OpenAI-Compatible`). |
| **8. Gouvernance** | `Administration`, `Paramètres`, `Sécurité` | Switcher Multi-Tenant, espaces de travail, RBAC + MFA, Journal d'audit immuable, exportation intégrale `JSON`. |

---

## 4. Démarrage Rapide (Git & Auto-Hébergement)

### Cloner le dépôt
```bash
git clone https://github.com/mendeltemgoua/business-os.git
cd business-os
```

### Lancer l'application Android (Local-First)
```bash
gradle :app:assembleDebug
gradle :app:testDebugUnitTest
```

### Déployer l'infrastructure Serveur Auto-Hébergée (Docker Compose)
```bash
cp .env.example .env
docker compose up -d
```

---

## 5. Auteur & Licence

- **Auteur Principal & Architecte Système** : **Mendel Temgoua**
- **Contact Professionnel** : [mendeltemgouabusiness@gmail.com](mailto:mendeltemgouabusiness@gmail.com)
- **Licence** : Distribué sous licence **MIT**. Voir le fichier [`LICENSE`](./LICENSE) pour plus d'informations.
