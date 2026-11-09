package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        OrganizationEntity::class,
        WorkspaceEntity::class,
        UserRoleMemberEntity::class,
        AccountEntity::class,
        TransactionEntity::class,
        AccountingEntryEntity::class,
        CrmContactEntity::class,
        CommercialDocEntity::class,
        ProductStockEntity::class,
        StockMovementEntity::class,
        EmployeeEntity::class,
        ProjectTaskEntity::class,
        DocumentVaultEntity::class,
        AutomationWorkflowEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BusinessOsDatabase : RoomDatabase() {
    abstract fun dao(): BusinessOsDao

    companion object {
        @Volatile
        private var INSTANCE: BusinessOsDatabase? = null

        fun getInstance(context: Context): BusinessOsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BusinessOsDatabase::class.java,
                    "business_os_enterprise.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                CoroutineScope(Dispatchers.IO).launch {
                    seedInitialEnterpriseDataIfNeeded(instance.dao())
                }
                instance
            }
        }

        suspend fun seedInitialEnterpriseDataIfNeeded(dao: BusinessOsDao) {
            if (dao.getOrganizationsList().isNotEmpty()) return

            val now = System.currentTimeMillis()
            val dayMs = 86_400_000L

            // 1. Seed 2 Multi-Tenant Organizations (OHADA / CEMAC + Europe SAS)
            val org1Id = dao.insertOrganization(
                OrganizationEntity(
                    name = "TechNova Africa SARL",
                    slug = "technova-africa",
                    legalForm = "SARL",
                    taxId = "M052612849312A • RCCM/DLA/2026/B/142",
                    ohadaZone = "OHADA - CEMAC",
                    currency = "XAF",
                    defaultVatPercent = 19.25,
                    createdAt = now - 90 * dayMs
                )
            )

            val org2Id = dao.insertOrganization(
                OrganizationEntity(
                    name = "HexaCloud Enterprise SAS",
                    slug = "hexacloud-eu",
                    legalForm = "SAS",
                    taxId = "FR-89410294812 • RCS PARIS",
                    ohadaZone = "PCG / IFRS-EU",
                    currency = "EUR",
                    defaultVatPercent = 20.0,
                    createdAt = now - 60 * dayMs
                )
            )

            // 2. Workspaces for Org 1 & Org 2
            dao.insertWorkspace(WorkspaceEntity(organizationId = org1Id, name = "Direction Générale & Finances", code = "HQ-FIN", department = "Direction", isDefault = true))
            dao.insertWorkspace(WorkspaceEntity(organizationId = org1Id, name = "Opérations, Cloud & ERP", code = "OPS-ERP", department = "Ingénierie", isDefault = false))
            dao.insertWorkspace(WorkspaceEntity(organizationId = org1Id, name = "Agence Commerciale & CRM", code = "SALES-DLA", department = "Ventes", isDefault = false))
            dao.insertWorkspace(WorkspaceEntity(organizationId = org2Id, name = "Executive & SaaS Operations", code = "EU-HQ", department = "Executive", isDefault = true))

            // 3. Memberships & RBAC Roles
            dao.insertMembership(UserRoleMemberEntity(organizationId = org1Id, fullName = "Mendel Temgoua", email = "mendel@technova.org", roleName = "OWNER • Super Admin", permissionsCsv = "ALL_MODULES,MULTI_TENANT_ADMIN,OHADA_CLOSE,SECURITY_AUDIT,AI_CONFIG", mfaEnabled = true))
            dao.insertMembership(UserRoleMemberEntity(organizationId = org1Id, fullName = "Clarisse Ndongo", email = "c.ndongo@technova.org", roleName = "Expert-Comptable OHADA", permissionsCsv = "FINANCE_WRITE,ACCOUNTING_CLOSE,TAX_EXPORT,DOCUMENTS_READ", mfaEnabled = true))
            dao.insertMembership(UserRoleMemberEntity(organizationId = org1Id, fullName = "Sébastien Kouam", email = "s.kouam@technova.org", roleName = "Directeur Commercial", permissionsCsv = "CRM_WRITE,BILLING_WRITE,MARKETING_WRITE", mfaEnabled = true))
            dao.insertMembership(UserRoleMemberEntity(organizationId = org1Id, fullName = "Amina Diallo", email = "a.diallo@technova.org", roleName = "DRH & Projets", permissionsCsv = "HR_WRITE,PROJECTS_WRITE,TASKS_WRITE", mfaEnabled = false))

            // 4. Financial Accounts (OHADA Classes 521, 552, 571, 411, 401)
            val bankAccId = dao.insertAccount(AccountEntity(organizationId = org1Id, name = "Banque Afriland / BGFI Pro", accountNumber = "CM21-10005-00492104810-44", ohadaCode = "5211", type = "BANK", balanceCents = 28_450_000_00L, currency = "XAF"))
            val momoAccId = dao.insertAccount(AccountEntity(organizationId = org1Id, name = "Portefeuille Mobile Money (MTN & Orange)", accountNumber = "MOMO-CORP-237-8841", ohadaCode = "5521", type = "MOBILE_MONEY", balanceCents = 6_820_000_00L, currency = "XAF"))
            val cashAccId = dao.insertAccount(AccountEntity(organizationId = org1Id, name = "Caisse Siège Douala", accountNumber = "CAISSE-HQ-01", ohadaCode = "5711", type = "CASH", balanceCents = 1_450_000_00L, currency = "XAF"))
            dao.insertAccount(AccountEntity(organizationId = org1Id, name = "Clients - Créances d'exploitation", accountNumber = "OHADA-4110", ohadaCode = "4111", type = "RECEIVABLE", balanceCents = 9_150_000_00L, currency = "XAF"))
            dao.insertAccount(AccountEntity(organizationId = org1Id, name = "Fournisseurs d'exploitation", accountNumber = "OHADA-4010", ohadaCode = "4011", type = "PAYABLE", balanceCents = 3_200_000_00L, currency = "XAF"))

            // Org 2 Account
            val euBankId = dao.insertAccount(AccountEntity(organizationId = org2Id, name = "Compte Courant Qonto / BNP", accountNumber = "FR76-30004-00891-000104829", ohadaCode = "5120", type = "BANK", balanceCents = 94_500_00L, currency = "EUR"))

            // 5. Validated Financial Transactions
            dao.insertTransaction(
                TransactionEntity(
                    organizationId = org1Id, accountId = bankAccId, accountName = "Banque Afriland / BGFI Pro",
                    reference = "TRX-2026-00101", type = "INCOME", category = "Déploiement ERP & Licences Support",
                    amountCents = 14_500_000_00L, taxAmountCents = 2_340_628_00L, currency = "XAF",
                    paymentMethod = "BANK_TRANSFER", status = "RECONCILED",
                    description = "Règlement Facture FAC-2026-001 • Groupe SABC Logistique",
                    supportingDocRef = "DOC-SHA-88F21A", timestamp = now - 14 * dayMs
                )
            )
            dao.insertTransaction(
                TransactionEntity(
                    organizationId = org1Id, accountId = momoAccId, accountName = "Portefeuille Mobile Money (MTN & Orange)",
                    reference = "TRX-2026-00102", type = "INCOME", category = "Abonnement Maintenance Annuel",
                    amountCents = 4_850_000_00L, taxAmountCents = 782_935_00L, currency = "XAF",
                    paymentMethod = "MTN_MOMO", status = "VALIDATED",
                    description = "Encaissement MTN MoMo API • Clinique de l'Espoir",
                    supportingDocRef = "MOMO-TX-994120", timestamp = now - 9 * dayMs
                )
            )
            dao.insertTransaction(
                TransactionEntity(
                    organizationId = org1Id, accountId = momoAccId, accountName = "Portefeuille Mobile Money (MTN & Orange)",
                    reference = "TRX-2026-00103", type = "INCOME", category = "Intégration Passerelle Paiement",
                    amountCents = 3_200_000_00L, taxAmountCents = 516_562_00L, currency = "XAF",
                    paymentMethod = "ORANGE_MONEY", status = "VALIDATED",
                    description = "Paiement Orange Money • AgroCam Distribution",
                    supportingDocRef = "OM-TX-77319", timestamp = now - 5 * dayMs
                )
            )
            dao.insertTransaction(
                TransactionEntity(
                    organizationId = org1Id, accountId = bankAccId, accountName = "Banque Afriland / BGFI Pro",
                    reference = "TRX-2026-00104", type = "EXPENSE", category = "Serveurs Bare-Metal & Réseau",
                    amountCents = 2_650_000_00L, taxAmountCents = 427_770_00L, currency = "XAF",
                    paymentMethod = "BANK_TRANSFER", status = "RECONCILED",
                    description = "Infrastructure Serveurs Auto-Hébergés & Fibre Dédiée",
                    supportingDocRef = "FAC-FOUR-419", timestamp = now - 7 * dayMs
                )
            )
            dao.insertTransaction(
                TransactionEntity(
                    organizationId = org1Id, accountId = bankAccId, accountName = "Banque Afriland / BGFI Pro",
                    reference = "TRX-2026-00105", type = "EXPENSE", category = "Salaires & Charges Sociales CNPS",
                    amountCents = 5_400_000_00L, taxAmountCents = 0L, currency = "XAF",
                    paymentMethod = "BANK_TRANSFER", status = "VALIDATED",
                    description = "Virement Paie Mensuelle Collaborateurs + Cotisations",
                    supportingDocRef = "PAIE-2026-10", timestamp = now - 3 * dayMs
                )
            )
            dao.insertTransaction(
                TransactionEntity(
                    organizationId = org1Id, accountId = cashAccId, accountName = "Caisse Siège Douala",
                    reference = "TRX-2026-00106", type = "EXPENSE", category = "Logistique & Missions Client",
                    amountCents = 480_000_00L, taxAmountCents = 77_480_00L, currency = "XAF",
                    paymentMethod = "MANUAL", status = "VALIDATED",
                    description = "Déplacement équipe audit client Yaoundé",
                    supportingDocRef = "BON-CAISSE-019", timestamp = now - 1 * dayMs
                )
            )

            dao.insertTransaction(
                TransactionEntity(
                    organizationId = org2Id, accountId = euBankId, accountName = "Compte Courant Qonto / BNP",
                    reference = "TRX-EU-2026-01", type = "INCOME", category = "Enterprise Self-Hosted Support",
                    amountCents = 42_000_00L, taxAmountCents = 7_000_00L, currency = "EUR",
                    paymentMethod = "STRIPE", status = "RECONCILED",
                    description = "Contrat Annuel Architecture DevOps • FinTech Paris",
                    supportingDocRef = "STRP-99102", timestamp = now - 4 * dayMs
                )
            )

            // 6. OHADA Accounting Entries (Double-Entry Journal VE, AC, BQ, OD)
            dao.insertAccountingEntry(
                AccountingEntryEntity(
                    organizationId = org1Id, journalCode = "VE", reference = "EC-OHADA-001",
                    ohadaDebitCode = "4111", ohadaDebitName = "Clients - Groupe SABC Logistique",
                    ohadaCreditCode = "7061", ohadaCreditName = "Services Vendus & Ingénierie Logicielle",
                    amountCents = 14_500_000_00L, taxRatePercent = 19.25,
                    description = "Facturation déploiement ERP Business OS • FAC-2026-001",
                    isReconciled = true, entryDate = now - 15 * dayMs
                )
            )
            dao.insertAccountingEntry(
                AccountingEntryEntity(
                    organizationId = org1Id, journalCode = "BQ", reference = "EC-OHADA-002",
                    ohadaDebitCode = "5211", ohadaDebitName = "Banque Locale Afriland / BGFI",
                    ohadaCreditCode = "4111", ohadaCreditName = "Clients - Groupe SABC Logistique",
                    amountCents = 14_500_000_00L, taxRatePercent = 0.0,
                    description = "Encaissement virement bancaire FAC-2026-001",
                    isReconciled = true, entryDate = now - 14 * dayMs
                )
            )
            dao.insertAccountingEntry(
                AccountingEntryEntity(
                    organizationId = org1Id, journalCode = "BQ", reference = "EC-OHADA-003",
                    ohadaDebitCode = "5521", ohadaDebitName = "Instruments Monnaie Électronique (MoMo/OM)",
                    ohadaCreditCode = "7061", ohadaCreditName = "Prestations de Services & Maintenance",
                    amountCents = 8_050_000_00L, taxRatePercent = 19.25,
                    description = "Règlements Mobile Money Clinique Espoir & AgroCam",
                    isReconciled = true, entryDate = now - 5 * dayMs
                )
            )
            dao.insertAccountingEntry(
                AccountingEntryEntity(
                    organizationId = org1Id, journalCode = "AC", reference = "EC-OHADA-004",
                    ohadaDebitCode = "6053", ohadaDebitName = "Fournitures Serveurs, Cloud & Télécoms",
                    ohadaCreditCode = "4011", ohadaCreditName = "Fournisseurs d'Exploitation",
                    amountCents = 2_650_000_00L, taxRatePercent = 19.25,
                    description = "Achat baies serveurs PostgreSQL & routeurs haute dispo",
                    isReconciled = true, entryDate = now - 7 * dayMs
                )
            )
            dao.insertAccountingEntry(
                AccountingEntryEntity(
                    organizationId = org1Id, journalCode = "OD", reference = "EC-OHADA-005",
                    ohadaDebitCode = "6611", ohadaDebitName = "Rémunérations Directes du Personnel",
                    ohadaCreditCode = "4221", ohadaCreditName = "Personnel - Rémunérations Dues",
                    amountCents = 5_400_000_00L, taxRatePercent = 0.0,
                    description = "Livre de paie mensuel & charges sociales OHADA",
                    isReconciled = false, entryDate = now - 3 * dayMs
                )
            )

            // 7. CRM Contacts: Customers, Prospects (Leads), Suppliers
            val client1Id = dao.insertContact(
                CrmContactEntity(
                    organizationId = org1Id, kind = "CUSTOMER",
                    companyName = "Groupe SABC Logistique", contactName = "Jean-Marc Ekani",
                    email = "jm.ekani@sabc-log.cm", phone = "+237 699 10 22 45",
                    stage = "WON_CLIENT", estimatedValueCents = 14_500_000_00L,
                    sourceCampaign = "Appel d'Offres Industriel",
                    historyLog = "[2026-10-15] Contrat signé • [2026-10-26] Facture FAC-2026-001 réglée par virement.",
                    createdAt = now - 45 * dayMs
                )
            )
            val client2Id = dao.insertContact(
                CrmContactEntity(
                    organizationId = org1Id, kind = "CUSTOMER",
                    companyName = "Clinique de l'Espoir", contactName = "Dr. Hélène Biya-Mba",
                    email = "direction@clinique-espoir.org", phone = "+237 677 84 19 00",
                    stage = "WON_CLIENT", estimatedValueCents = 9_150_000_00L,
                    sourceCampaign = "Recommandation Partenaire",
                    historyLog = "[2026-10-20] Déploiement module RH & Facturation hospitalière • [2026-11-01] Acompte MTN MoMo reçu.",
                    createdAt = now - 30 * dayMs
                )
            )
            dao.insertContact(
                CrmContactEntity(
                    organizationId = org1Id, kind = "LEAD",
                    companyName = "Cimenteries du Littoral SA", contactName = "Patrick Nguema",
                    email = "p.nguema@cimlittoral.com", phone = "+237 690 44 12 88",
                    stage = "NEGOTIATION", estimatedValueCents = 18_500_000_00L,
                    sourceCampaign = "Salon Tech & Industrie Douala",
                    historyLog = "[2026-11-02] Démo architecture auto-hébergée validée par le DSI • Devis DEV-2026-004 envoyé.",
                    createdAt = now - 8 * dayMs
                )
            )
            dao.insertContact(
                CrmContactEntity(
                    organizationId = org1Id, kind = "LEAD",
                    companyName = "TransAfrica Fret & Transit", contactName = "Chantal Fokou",
                    email = "c.fokou@transafrica-fret.cm", phone = "+237 655 31 09 72",
                    stage = "PROPOSAL", estimatedValueCents = 7_600_000_00L,
                    sourceCampaign = "Campagne Inbound LinkedIn",
                    historyLog = "[2026-11-05] Besoin gestion multi-entrepôts + comptabilité OHADA.",
                    createdAt = now - 4 * dayMs
                )
            )
            dao.insertContact(
                CrmContactEntity(
                    organizationId = org1Id, kind = "SUPPLIER",
                    companyName = "DataCenter KamerHost SARL", contactName = "Support Infrastructure",
                    email = "billing@kamerhost.cm", phone = "+237 233 42 88 10",
                    stage = "ACTIVE_PARTNER", estimatedValueCents = 3_200_000_00L,
                    sourceCampaign = "Fournisseur Agréé",
                    historyLog = "Fournisseur serveurs dédiés Linux, stockage S3 MinIO et passerelles SMS.",
                    createdAt = now - 60 * dayMs
                )
            )

            // 8. Commercial Documents (Invoices, Quotes, Orders, Contracts)
            dao.insertCommercialDoc(
                CommercialDocEntity(
                    organizationId = org1Id, contactId = client1Id, contactName = "Groupe SABC Logistique",
                    docType = "INVOICE", docNumber = "FAC-2026-001",
                    itemsDescription = "1x Licence Cluster Auto-Hébergé + Déploiement Comptabilité OHADA + Formation 25 utilisateurs",
                    subtotalCents = 12_159_372_00L, taxCents = 2_340_628_00L, totalCents = 14_500_000_00L,
                    paidCents = 14_500_000_00L, currency = "XAF", status = "PAID",
                    dueDate = now - 5 * dayMs, createdAt = now - 15 * dayMs
                )
            )
            dao.insertCommercialDoc(
                CommercialDocEntity(
                    organizationId = org1Id, contactId = client2Id, contactName = "Clinique de l'Espoir",
                    docType = "INVOICE", docNumber = "FAC-2026-002",
                    itemsDescription = "Déploiement ERP Médical + Module Stocks Pharmacie & Paie RH",
                    subtotalCents = 7_672_955_00L, taxCents = 1_477_045_00L, totalCents = 9_150_000_00L,
                    paidCents = 4_850_000_00L, currency = "XAF", status = "OVERDUE",
                    dueDate = now - 2 * dayMs, createdAt = now - 18 * dayMs
                )
            )
            dao.insertCommercialDoc(
                CommercialDocEntity(
                    organizationId = org1Id, contactId = client2Id, contactName = "AgroCam Distribution",
                    docType = "INVOICE", docNumber = "FAC-2026-003",
                    itemsDescription = "Connecteurs Mobile Money (MTN/Orange/Campay) & Module Facturation",
                    subtotalCents = 4_067_086_00L, taxCents = 782_914_00L, totalCents = 4_850_000_00L,
                    paidCents = 0L, currency = "XAF", status = "SENT",
                    dueDate = now + 10 * dayMs, createdAt = now - 3 * dayMs
                )
            )
            dao.insertCommercialDoc(
                CommercialDocEntity(
                    organizationId = org1Id, contactId = 3L, contactName = "Cimenteries du Littoral SA",
                    docType = "QUOTE", docNumber = "DEV-2026-004",
                    itemsDescription = "Architecture Multi-Filiales BUSINESS OS + IA Locale Ollama Sur Serveur Dédié",
                    subtotalCents = 15_513_626_00L, taxCents = 2_986_374_00L, totalCents = 18_500_000_00L,
                    paidCents = 0L, currency = "XAF", status = "SENT",
                    dueDate = now + 14 * dayMs, createdAt = now - 4 * dayMs
                )
            )

            // 9. Products, Services & Inventory
            val prod1Id = dao.insertProduct(
                ProductStockEntity(
                    organizationId = org1Id, sku = "SRV-NODE-EPYC",
                    name = "Appliance Serveur Business OS (AMD EPYC / 64GB ECC / NVMe RAID1)",
                    kind = "PRODUCT", category = "Matériel & Serveurs",
                    unitPriceCents = 2_450_000_00L, costPriceCents = 1_680_000_00L,
                    stockQuantity = 8, minStockAlert = 3,
                    supplierName = "DataCenter KamerHost SARL", warehouseLocation = "Entrepôt Douala Akwa - Rack A2"
                )
            )
            val prod2Id = dao.insertProduct(
                ProductStockEntity(
                    organizationId = org1Id, sku = "TERM-POS-NFC",
                    name = "Terminal Mobile Encaissement QR/NFC & Imprimante Thermique",
                    kind = "PRODUCT", category = "Terminaux Point de Vente",
                    unitPriceCents = 185_000_00L, costPriceCents = 110_000_00L,
                    stockQuantity = 3, minStockAlert = 5, // Low stock alert!
                    supplierName = "Shenzhen SmartPOS Import", warehouseLocation = "Entrepôt Douala Akwa - Zone B"
                )
            )
            dao.insertProduct(
                ProductStockEntity(
                    organizationId = org1Id, sku = "SVC-AUDIT-OHADA",
                    name = "Audit & Paramétrage Comptabilité OHADA Multi-Sociétés (Forfait)",
                    kind = "SERVICE", category = "Ingénierie & Conseil ERP",
                    unitPriceCents = 1_500_000_00L, costPriceCents = 350_000_00L,
                    stockQuantity = 999, minStockAlert = 0,
                    supplierName = "Équipe Interne Business OS", warehouseLocation = "Prestation Immatérielle"
                )
            )
            dao.insertProduct(
                ProductStockEntity(
                    organizationId = org1Id, sku = "SVC-DEVOPS-CADDY",
                    name = "Installation Docker Compose + PostgreSQL + Caddy HTTPS + Sauvegarde S3",
                    kind = "SERVICE", category = "DevOps & Sécurité",
                    unitPriceCents = 950_000_00L, costPriceCents = 200_000_00L,
                    stockQuantity = 999, minStockAlert = 0,
                    supplierName = "Équipe Interne Business OS", warehouseLocation = "Prestation Immatérielle"
                )
            )

            dao.insertStockMovement(StockMovementEntity(organizationId = org1Id, productId = prod1Id, productName = "Appliance Serveur Business OS", movementType = "IN", quantity = 10, reason = "Réception commande fournisseur PO-2026-11", timestamp = now - 20 * dayMs))
            dao.insertStockMovement(StockMovementEntity(organizationId = org1Id, productId = prod1Id, productName = "Appliance Serveur Business OS", movementType = "OUT", quantity = 2, reason = "Livraison Groupe SABC Logistique", timestamp = now - 14 * dayMs))
            dao.insertStockMovement(StockMovementEntity(organizationId = org1Id, productId = prod2Id, productName = "Terminal Mobile Encaissement QR/NFC", movementType = "OUT", quantity = 12, reason = "Équipement Clinique de l'Espoir & AgroCam", timestamp = now - 6 * dayMs))

            // 10. HR Employees & Projects/Tasks
            dao.insertEmployee(EmployeeEntity(organizationId = org1Id, fullName = "Mendel Temgoua", roleTitle = "Architecte Principal & CEO", department = "Direction & Architecture", contractType = "CDI", baseSalaryCents = 1_650_000_00L, leaveBalanceDays = 22, performanceScore = 98, status = "ACTIVE"))
            dao.insertEmployee(EmployeeEntity(organizationId = org1Id, fullName = "Clarisse Ndongo", roleTitle = "Responsable Financière & Comptable OHADA", department = "Finances & Comptabilité", contractType = "CDI", baseSalaryCents = 1_150_000_00L, leaveBalanceDays = 18, performanceScore = 95, status = "ACTIVE"))
            dao.insertEmployee(EmployeeEntity(organizationId = org1Id, fullName = "Sébastien Kouam", roleTitle = "Head of CRM & Croissance", department = "Ventes & Marketing", contractType = "CDI", baseSalaryCents = 980_000_00L, leaveBalanceDays = 14, performanceScore = 91, status = "ACTIVE"))
            dao.insertEmployee(EmployeeEntity(organizationId = org1Id, fullName = "Amina Diallo", roleTitle = "Responsable RH & Opérations", department = "Ressources Humaines", contractType = "CDI", baseSalaryCents = 890_000_00L, leaveBalanceDays = 20, performanceScore = 94, status = "ACTIVE"))
            dao.insertEmployee(EmployeeEntity(organizationId = org1Id, fullName = "Fabrice Njoya", roleTitle = "Ingénieur DevOps & Sécurité OWASP", department = "Ingénierie & Cloud", contractType = "CDI", baseSalaryCents = 920_000_00L, leaveBalanceDays = 15, performanceScore = 92, status = "ACTIVE"))

            dao.insertProjectTask(ProjectTaskEntity(organizationId = org1Id, projectName = "Déploiement ERP Cimenteries du Littoral", taskTitle = "Configurer cluster PostgreSQL 16 + Réplication & Sauvegardes chiffrées", assigneeName = "Fabrice Njoya", priority = "CRITICAL", status = "IN_PROGRESS", budgetCents = 4_500_000_00L, progressPercent = 75, dueDate = now + 5 * dayMs, commentsLog = "Tests de restauration validés en 42 secondes."))
            dao.insertProjectTask(ProjectTaskEntity(organizationId = org1Id, projectName = "Clôture Fiscale & Balance OHADA T4", taskTitle = "Rapprochement bancaire BGFI & Mobile Money + Déclaration TVA", assigneeName = "Clarisse Ndongo", priority = "HIGH", status = "IN_PROGRESS", budgetCents = 1_200_000_00L, progressPercent = 85, dueDate = now + 3 * dayMs, commentsLog = "Écritures journaux VE/AC/BQ équilibrées."))
            dao.insertProjectTask(ProjectTaskEntity(organizationId = org1Id, projectName = "Intégration Webhooks Mobile Money", taskTitle = "Vérification HMAC-SHA256 et idempotence MTN MoMo / Orange Money / Campay", assigneeName = "Mendel Temgoua", priority = "HIGH", status = "DONE", budgetCents = 2_100_000_00L, progressPercent = 100, dueDate = now - 2 * dayMs, commentsLog = "Tests d'idempotence 100% passés."))
            dao.insertProjectTask(ProjectTaskEntity(organizationId = org1Id, projectName = "Campagne Acquisition Entreprises CEMAC", taskTitle = "Relance opportunités B2B & conversion prospects qualifiés", assigneeName = "Sébastien Kouam", priority = "MEDIUM", status = "TODO", budgetCents = 1_800_000_00L, progressPercent = 30, dueDate = now + 9 * dayMs, commentsLog = "2 prospects majeurs en phase finale."))

            // 11. Document Vault
            dao.insertDocument(
                DocumentVaultEntity(
                    organizationId = org1Id, title = "Contrat Cadre Déploiement ERP - Groupe SABC",
                    category = "CONTRACT", fileName = "contrat_sabc_2026_signe.pdf", mimeType = "application/pdf",
                    sizeBytes = 1_482_900L, storageDriver = "LOCAL_FS", accessLevel = "ADMIN_ONLY",
                    version = 2, checksumSha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                    contentPreview = "Contrat de licence MIT & prestation d'intégration sur infrastructure locale SABC Logistique. SLA 99.95%."
                )
            )
            dao.insertDocument(
                DocumentVaultEntity(
                    organizationId = org1Id, title = "Grand Livre & Balance Générale OHADA - T4 2026",
                    category = "ACCOUNTING_OHADA", fileName = "balance_ohada_technova_2026.csv", mimeType = "text/csv",
                    sizeBytes = 642_100L, storageDriver = "S3_MINIO_COMPATIBLE", accessLevel = "FINANCE_ONLY",
                    version = 4, checksumSha256 = "8f434346648f6b96df89dda901c5176b10a6d83961dd3c1ac88b59b2dc327aa4",
                    contentPreview = "Classes 1 à 7 SYSCOHADA Révisé • Total Débit = Total Crédit • Certifié conforme."
                )
            )
            dao.insertDocument(
                DocumentVaultEntity(
                    organizationId = org1Id, title = "Architecture Docker Compose, Caddy HTTPS & Guide Reprise Après Sinistre",
                    category = "TECHNICAL", fileName = "runbook_devops_selfhosted.md", mimeType = "text/markdown",
                    sizeBytes = 128_400L, storageDriver = "LOCAL_FS", accessLevel = "PUBLIC_ORG",
                    version = 1, checksumSha256 = "2c26b46b68ffc68ff99b453c1d30413413422d706483bfa0f98a5e886266e7ae",
                    contentPreview = "Procédures pg_dump chiffré AES-256, restauration automatisée et isolation multi-tenant PostgreSQL RLS."
                )
            )

            // 12. Automation Workflows
            dao.insertWorkflow(
                AutomationWorkflowEntity(
                    organizationId = org1Id,
                    name = "Relance Facture Impayée + Alerte Trésorerie",
                    triggerEvent = "INVOICE_OVERDUE",
                    conditionExpr = "days_overdue >= 1 && unpaid_amount > 100000",
                    actionType = "SEND_RELANCE & NOTIFY_FINANCE",
                    requiresHumanApproval = true,
                    isActive = true,
                    runCount = 14,
                    lastRunStatus = "SUCCESS • 1 relance prête (FAC-2026-002)"
                )
            )
            dao.insertWorkflow(
                AutomationWorkflowEntity(
                    organizationId = org1Id,
                    name = "Réapprovisionnement Automatique Seuil Critique Stock",
                    triggerEvent = "STOCK_LOW",
                    conditionExpr = "stockQuantity <= minStockAlert",
                    actionType = "CREATE_PO_DRAFT",
                    requiresHumanApproval = true,
                    isActive = true,
                    runCount = 6,
                    lastRunStatus = "ALERT • Terminal POS NFC (Stock: 3 <= Seuil: 5)"
                )
            )
            dao.insertWorkflow(
                AutomationWorkflowEntity(
                    organizationId = org1Id,
                    name = "Génération Automatique Écriture OHADA sur Encaissement",
                    triggerEvent = "NEW_PAYMENT",
                    conditionExpr = "payment.status == VALIDATED",
                    actionType = "GENERATE_OHADA_ENTRY",
                    requiresHumanApproval = false,
                    isActive = true,
                    runCount = 42,
                    lastRunStatus = "SUCCESS • Journal BQ/VE synchronisé"
                )
            )

            // 13. Security & Audit Logs
            dao.insertAuditLog(AuditLogEntity(organizationId = org1Id, actorName = "Mendel Temgoua", action = "MULTI_TENANT_RLS_VERIFIED", module = "SECURITY", severity = "INFO", details = "Isolation stricte des données par organizationId vérifiée • Licence MIT active."))
            dao.insertAuditLog(AuditLogEntity(organizationId = org1Id, actorName = "Clarisse Ndongo", action = "OHADA_JOURNAL_POSTED", module = "ACCOUNTING", severity = "INFO", details = "Écriture EC-OHADA-003 validée dans le journal BQ (8 050 000 XAF)."))
            dao.insertAuditLog(AuditLogEntity(organizationId = org1Id, actorName = "Système Automations", action = "WORKFLOW_TRIGGERED", module = "AUTOMATIONS", severity = "WARNING", details = "Alerte stock bas détectée sur SKU TERM-POS-NFC (3 unités restantes)."))
        }
    }
}
