# Politique de Sécurité & Conformité Licences — BUSINESS OS 2026

**Auteur & Mainteneur Principal** : Mendel Temgoua (`mendeltemgouabusiness@gmail.com`)

## 1. Architecture de Sécurité Intégrée (OWASP)
- **Isolation Multi-Tenant Stricte** : Toutes les requêtes SQLite (Room) et PostgreSQL (Prisma) filtrent obligatoirement par `organizationId`.
- **Intégrité Financière & OHADA** : Les transactions validées sont protégées contre toute suppression silencieuse. Toute correction passe par une écriture d'extourne (`OD`) et un enregistrement dans `audit_logs`.
- **Garde-Fou Business AI** : L'interface `AIProvider` bloque toute exécution automatique d'opération financière sensible et impose une validation humaine explicite.

## 2. Licences des Dépendances Vérifiées
Toutes les bibliothèques utilisées dans **BUSINESS OS** sont compatibles avec une distribution **MIT Open Source** sans restriction commerciale :
- **Kotlin / Coroutines / AndroidX / Jetpack Compose Material 3 / Room SQLite** : `Apache License 2.0`
- **Square OkHttp** : `Apache License 2.0`
- **Polices Space Grotesk, Inter, JetBrains Mono** : `SIL Open Font License 1.1`
- **PostgreSQL 16** : `PostgreSQL License (MIT-style)`
- **Caddy / Prisma** : `Apache License 2.0`

## 3. Signaler une Vulnérabilité
Pour toute divulgation responsable, contactez directement **Mendel Temgoua** à l'adresse : `mendeltemgouabusiness@gmail.com`.
