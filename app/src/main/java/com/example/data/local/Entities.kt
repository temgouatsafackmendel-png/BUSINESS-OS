package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "organizations",
    indices = [Index(value = ["slug"], unique = true)]
)
data class OrganizationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val slug: String,
    val legalForm: String, // SARL, SAS, SA, GIE
    val taxId: String,     // NIU / RCCM / SIRET
    val ohadaZone: String, // CEMAC, UEMOA, OHADA-GENERAL, IFRS-EU
    val currency: String,  // XAF, XOF, EUR, USD
    val defaultVatPercent: Double = 19.25,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "workspaces",
    indices = [Index(value = ["organizationId"])]
)
data class WorkspaceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val organizationId: Long,
    val name: String,
    val code: String,
    val department: String,
    val isDefault: Boolean = false
)

@Entity(
    tableName = "memberships",
    indices = [Index(value = ["organizationId", "email"], unique = true)]
)
data class UserRoleMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val organizationId: Long,
    val fullName: String,
    val email: String,
    val roleName: String, // OWNER, ADMIN, CHIEF_ACCOUNTANT, SALES_MANAGER, HR_MANAGER, AUDITOR
    val permissionsCsv: String, // FINANCE_WRITE,ACCOUNTING_CLOSE,CRM_WRITE,HR_READ,AI_ADMIN,EXPORT_ALL
    val mfaEnabled: Boolean = true,
    val status: String = "ACTIVE"
)

@Entity(
    tableName = "accounts",
    indices = [Index(value = ["organizationId", "ohadaCode"])]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val organizationId: Long,
    val name: String,
    val accountNumber: String,
    val ohadaCode: String, // e.g., 521 (Banque locale), 571 (Caisse), 552 (Monnaie électronique)
    val type: String,      // BANK, CASH, MOBILE_MONEY, RECEIVABLE, PAYABLE
    val balanceCents: Long,
    val currency: String
)

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["organizationId", "timestamp"]), Index(value = ["reference"], unique = true)]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val organizationId: Long,
    val accountId: Long,
    val accountName: String,
    val reference: String,
    val type: String, // INCOME, EXPENSE, TRANSFER, DEBT, RECEIVABLE
    val category: String,
    val amountCents: Long,
    val taxAmountCents: Long,
    val currency: String,
    val paymentMethod: String, // MANUAL, MTN_MOMO, ORANGE_MONEY, CAMPAY, MONEY_FUSION, STRIPE, PAYPAL, BANK_TRANSFER
    val status: String,        // VALIDATED, PENDING, RECONCILED, REVERSED
    val description: String,
    val supportingDocRef: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "accounting_entries",
    indices = [Index(value = ["organizationId", "journalCode"]), Index(value = ["entryDate"])]
)
data class AccountingEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val organizationId: Long,
    val journalCode: String, // VE (Ventes), AC (Achats), BQ (Banque), CA (Caisse), OD (Opérations Diverses)
    val reference: String,
    val ohadaDebitCode: String,
    val ohadaDebitName: String,
    val ohadaCreditCode: String,
    val ohadaCreditName: String,
    val amountCents: Long,
    val taxRatePercent: Double,
    val description: String,
    val isReconciled: Boolean = false,
    val entryDate: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "crm_contacts",
    indices = [Index(value = ["organizationId", "kind"])]
)
data class CrmContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val organizationId: Long,
    val kind: String, // LEAD (Prospect), CUSTOMER (Client), SUPPLIER (Fournisseur)
    val companyName: String,
    val contactName: String,
    val email: String,
    val phone: String,
    val stage: String, // NEW, QUALIFIED, PROPOSAL, NEGOTIATION, WON_CLIENT, ACTIVE_PARTNER
    val estimatedValueCents: Long,
    val sourceCampaign: String,
    val historyLog: String,
    val convertedFromLeadAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "commercial_docs",
    indices = [Index(value = ["organizationId", "docType"]), Index(value = ["docNumber"], unique = true)]
)
data class CommercialDocEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val organizationId: Long,
    val contactId: Long,
    val contactName: String,
    val docType: String, // QUOTE (Devis), ORDER (Commande), INVOICE (Facture), CREDIT_NOTE (Avoir), CONTRACT, SUBSCRIPTION
    val docNumber: String,
    val itemsDescription: String,
    val subtotalCents: Long,
    val taxCents: Long,
    val totalCents: Long,
    val paidCents: Long,
    val currency: String,
    val status: String, // DRAFT, SENT, APPROVED, PARTIAL, PAID, OVERDUE, CANCELLED
    val dueDate: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "inventory_items",
    indices = [Index(value = ["organizationId", "sku"], unique = true)]
)
data class ProductStockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val organizationId: Long,
    val sku: String,
    val name: String,
    val kind: String, // PRODUCT, SERVICE
    val category: String,
    val unitPriceCents: Long,
    val costPriceCents: Long,
    val stockQuantity: Int,
    val minStockAlert: Int,
    val supplierName: String,
    val warehouseLocation: String
)

@Entity(
    tableName = "stock_movements",
    indices = [Index(value = ["organizationId", "productId"])]
)
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val organizationId: Long,
    val productId: Long,
    val productName: String,
    val movementType: String, // IN, OUT, ADJUSTMENT
    val quantity: Int,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "employees",
    indices = [Index(value = ["organizationId", "department"])]
)
data class EmployeeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val organizationId: Long,
    val fullName: String,
    val roleTitle: String,
    val department: String,
    val contractType: String, // CDI, CDD, CONSULTANT
    val baseSalaryCents: Long,
    val leaveBalanceDays: Int,
    val performanceScore: Int, // 0..100
    val status: String // ACTIVE, ON_LEAVE
)

@Entity(
    tableName = "projects_tasks",
    indices = [Index(value = ["organizationId", "status"])]
)
data class ProjectTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val organizationId: Long,
    val projectName: String,
    val taskTitle: String,
    val assigneeName: String,
    val priority: String, // LOW, MEDIUM, HIGH, CRITICAL
    val status: String,   // TODO, IN_PROGRESS, REVIEW, DONE
    val budgetCents: Long,
    val progressPercent: Int,
    val dueDate: Long,
    val commentsLog: String
)

@Entity(
    tableName = "documents",
    indices = [Index(value = ["organizationId", "category"])]
)
data class DocumentVaultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val organizationId: Long,
    val title: String,
    val category: String, // CONTRACT, INVOICE, ACCOUNTING_OHADA, HR_PAYROLL, LEGAL, TECHNICAL
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val storageDriver: String, // LOCAL_FS, S3_MINIO_COMPATIBLE
    val accessLevel: String,   // PUBLIC_ORG, FINANCE_ONLY, ADMIN_ONLY
    val version: Int,
    val checksumSha256: String,
    val contentPreview: String,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "workflows",
    indices = [Index(value = ["organizationId"])]
)
data class AutomationWorkflowEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val organizationId: Long,
    val name: String,
    val triggerEvent: String,  // INVOICE_OVERDUE, STOCK_LOW, PROSPECT_CONVERTED, HIGH_EXPENSE, NEW_PAYMENT
    val conditionExpr: String, // e.g., amount > 500,000 XAF or days_overdue >= 7
    val actionType: String,    // SEND_RELANCE, CREATE_PO_DRAFT, GENERATE_OHADA_ENTRY, NOTIFY_ADMIN
    val requiresHumanApproval: Boolean = true,
    val isActive: Boolean = true,
    val runCount: Int = 0,
    val lastRunStatus: String = "READY",
    val lastRunTimestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "audit_logs",
    indices = [Index(value = ["organizationId", "timestamp"])]
)
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val organizationId: Long,
    val actorName: String,
    val action: String,
    val module: String,
    val severity: String, // INFO, WARNING, SECURITY_CRITICAL
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
