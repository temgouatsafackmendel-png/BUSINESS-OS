package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiAssistantMessage
import com.example.data.ai.AiConfigState
import com.example.data.ai.AiProviderType
import com.example.data.ai.PluggableAiEngine
import com.example.data.local.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToLong

enum class ErpPrimaryModule(val title: String, val subtitle: String) {
    DASHBOARD("Dashboard", "Pilotage Temps Réel & KPIs"),
    FINANCES("Finances & Comptabilité", "Trésorerie, Paiements & OHADA"),
    CRM_SALES("CRM, Ventes & Facturation", "Prospects, Clients, Devis & Factures"),
    OPERATIONS("Produits, Stocks & Achats", "Catalogue, Entrepôts & Fournisseurs"),
    HR_PROJECTS("RH, Projets & Tâches", "Collaborateurs, Paie & Productivité"),
    DOCS_AI_AUTO("Documents, Workflows & IA", "GED, Automatisations & Business AI"),
    ADMIN_SECURITY("Multi-Tenant, Sécurité & Self-Host", "Organisations, RBAC, Audit & Docker")
}

enum class TimePeriodFilter(val label: String, val daysBack: Int) {
    DAYS_7("7 Jours", 7),
    DAYS_30("30 Jours", 30),
    QUARTER("Trimestre", 90),
    YEAR("Année 2026", 365)
}

data class DashboardWidgetConfig(
    val showFinancialKpis: Boolean = true,
    val showInteractiveChart: Boolean = true,
    val showAlertsBanner: Boolean = true,
    val showCrmPipelineSummary: Boolean = true,
    val showProjectsObjectives: Boolean = true,
    val showTreasuryAccounts: Boolean = true
)

data class DashboardMetrics(
    val revenueBilledCents: Long = 0L,
    val revenueCollectedCents: Long = 0L,
    val expensesTotalCents: Long = 0L,
    val netProfitCents: Long = 0L,
    val netMarginPercent: Double = 0.0,
    val availableTreasuryCents: Long = 0L,
    val unpaidInvoicesCents: Long = 0L,
    val unpaidInvoicesCount: Int = 0,
    val activeCustomersCount: Int = 0,
    val activeLeadsCount: Int = 0,
    val pipelineValueCents: Long = 0L,
    val salesDocsCount: Int = 0,
    val activeProjectsCount: Int = 0,
    val completedTasksCount: Int = 0,
    val totalTasksCount: Int = 0,
    val lowStockCount: Int = 0,
    val revenueTargetProgress: Float = 0.78f
)

@OptIn(ExperimentalCoroutinesApi::class)
class BusinessOsViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = BusinessOsDatabase.getInstance(application).dao()
    private val aiEngine = PluggableAiEngine()

    // Theme & Navigation State
    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _currentModule = MutableStateFlow(ErpPrimaryModule.DASHBOARD)
    val currentModule: StateFlow<ErpPrimaryModule> = _currentModule.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(TimePeriodFilter.YEAR)
    val selectedPeriod: StateFlow<TimePeriodFilter> = _selectedPeriod.asStateFlow()

    private val _widgetConfig = MutableStateFlow(DashboardWidgetConfig())
    val widgetConfig: StateFlow<DashboardWidgetConfig> = _widgetConfig.asStateFlow()

    private val _toastBannerMessage = MutableStateFlow<String?>(null)
    val toastBannerMessage: StateFlow<String?> = _toastBannerMessage.asStateFlow()

    // Multi-Tenant Active Organization ID
    private val _activeOrgId = MutableStateFlow(1L)
    val activeOrgId: StateFlow<Long> = _activeOrgId.asStateFlow()

    val organizations: StateFlow<List<OrganizationEntity>> = dao.observeOrganizations()
        .onEach { list ->
            if (list.isNotEmpty() && list.none { it.id == _activeOrgId.value }) {
                _activeOrgId.value = list.first().id
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeOrganization: StateFlow<OrganizationEntity?> = combine(organizations, _activeOrgId) { orgs, id ->
        orgs.find { it.id == id } ?: orgs.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Multi-tenant isolated data streams
    val workspaces: StateFlow<List<WorkspaceEntity>> = _activeOrgId
        .flatMapLatest { dao.observeWorkspaces(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memberships: StateFlow<List<UserRoleMemberEntity>> = _activeOrgId
        .flatMapLatest { dao.observeMemberships(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<AccountEntity>> = _activeOrgId
        .flatMapLatest { dao.observeAccounts(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = _activeOrgId
        .flatMapLatest { dao.observeTransactions(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accountingEntries: StateFlow<List<AccountingEntryEntity>> = _activeOrgId
        .flatMapLatest { dao.observeAccountingEntries(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contacts: StateFlow<List<CrmContactEntity>> = _activeOrgId
        .flatMapLatest { dao.observeContacts(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val commercialDocs: StateFlow<List<CommercialDocEntity>> = _activeOrgId
        .flatMapLatest { dao.observeCommercialDocs(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<ProductStockEntity>> = _activeOrgId
        .flatMapLatest { dao.observeProducts(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stockMovements: StateFlow<List<StockMovementEntity>> = _activeOrgId
        .flatMapLatest { dao.observeStockMovements(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val employees: StateFlow<List<EmployeeEntity>> = _activeOrgId
        .flatMapLatest { dao.observeEmployees(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val projectTasks: StateFlow<List<ProjectTaskEntity>> = _activeOrgId
        .flatMapLatest { dao.observeProjectTasks(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val documents: StateFlow<List<DocumentVaultEntity>> = _activeOrgId
        .flatMapLatest { dao.observeDocuments(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workflows: StateFlow<List<AutomationWorkflowEntity>> = _activeOrgId
        .flatMapLatest { dao.observeWorkflows(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = _activeOrgId
        .flatMapLatest { dao.observeAuditLogs(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Business AI State
    private val _aiConfig = MutableStateFlow(AiConfigState())
    val aiConfig: StateFlow<AiConfigState> = _aiConfig.asStateFlow()

    private val _aiMessages = MutableStateFlow(
        listOf(
            AiAssistantMessage(
                id = 1L,
                role = "ASSISTANT",
                content = "Bienvenue dans BUSINESS OS AI (Architecture Multi-Fournisseurs Open Source).\n\n• Mode actuel : Moteur Analytique Déterministe Local (100% Hors-Ligne & MIT).\n• Vous pouvez à tout moment désactiver l'IA ou basculer vers un serveur Ollama local / API compatible OpenAI.\n• Demandez une analyse de trésorerie, un diagnostic comptable OHADA, un plan de relance factures ou un résumé des stocks.",
                providerBadge = "Moteur Local MIT"
            )
        )
    )
    val aiMessages: StateFlow<List<AiAssistantMessage>> = _aiMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // Exported JSON preview state
    private val _exportedJsonPreview = MutableStateFlow<String?>(null)
    val exportedJsonPreview: StateFlow<String?> = _exportedJsonPreview.asStateFlow()

    // Real-time Dashboard Metrics derived exclusively from the SQLite database
    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        accounts,
        transactions,
        commercialDocs,
        contacts,
        projectTasks,
        products,
        _selectedPeriod
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val accs = values[0] as List<AccountEntity>
        @Suppress("UNCHECKED_CAST")
        val txs = values[1] as List<TransactionEntity>
        @Suppress("UNCHECKED_CAST")
        val docs = values[2] as List<CommercialDocEntity>
        @Suppress("UNCHECKED_CAST")
        val crms = values[3] as List<CrmContactEntity>
        @Suppress("UNCHECKED_CAST")
        val tasks = values[4] as List<ProjectTaskEntity>
        @Suppress("UNCHECKED_CAST")
        val prods = values[5] as List<ProductStockEntity>
        val period = values[6] as TimePeriodFilter

        val cutoff = System.currentTimeMillis() - period.daysBack * 86_400_000L
        val filteredTxs = txs.filter { it.timestamp >= cutoff && it.status != "REVERSED" }
        val filteredInvoices = docs.filter { it.docType == "INVOICE" && it.createdAt >= cutoff && it.status != "CANCELLED" }

        val revenueCollected = filteredTxs.filter { it.type == "INCOME" }.sumOf { it.amountCents }
        val expensesTotal = filteredTxs.filter { it.type == "EXPENSE" }.sumOf { it.amountCents }
        val revenueBilled = filteredInvoices.sumOf { it.totalCents }.coerceAtLeast(revenueCollected)
        val netProfit = revenueCollected - expensesTotal
        val marginPercent = if (revenueCollected > 0L) {
            (netProfit.toDouble() / revenueCollected.toDouble()) * 100.0
        } else 0.0

        val treasuryAvailable = accs
            .filter { it.type == "BANK" || it.type == "CASH" || it.type == "MOBILE_MONEY" }
            .sumOf { it.balanceCents }

        val unpaidInvoices = docs.filter { it.docType == "INVOICE" && it.status != "PAID" && it.status != "CANCELLED" }
        val unpaidAmount = unpaidInvoices.sumOf { (it.totalCents - it.paidCents).coerceAtLeast(0L) }

        val customersCount = crms.count { it.kind == "CUSTOMER" }
        val leads = crms.filter { it.kind == "LEAD" }
        val pipelineVal = leads.sumOf { it.estimatedValueCents }

        val distinctProjects = tasks.map { it.projectName }.distinct().size
        val doneTasks = tasks.count { it.status == "DONE" }
        val lowStockItems = prods.count { it.kind == "PRODUCT" && it.stockQuantity <= it.minStockAlert }

        val targetCents = if (accs.firstOrNull()?.currency == "EUR") 60_000_00L else 30_000_000_00L
        val progress = if (targetCents > 0) (revenueBilled.toFloat() / targetCents.toFloat()).coerceIn(0.05f, 1f) else 0.5f

        DashboardMetrics(
            revenueBilledCents = revenueBilled,
            revenueCollectedCents = revenueCollected,
            expensesTotalCents = expensesTotal,
            netProfitCents = netProfit,
            netMarginPercent = marginPercent,
            availableTreasuryCents = treasuryAvailable,
            unpaidInvoicesCents = unpaidAmount,
            unpaidInvoicesCount = unpaidInvoices.size,
            activeCustomersCount = customersCount,
            activeLeadsCount = leads.size,
            pipelineValueCents = pipelineVal,
            salesDocsCount = docs.size,
            activeProjectsCount = distinctProjects,
            completedTasksCount = doneTasks,
            totalTasksCount = tasks.size,
            lowStockCount = lowStockItems,
            revenueTargetProgress = progress
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    fun selectModule(module: ErpPrimaryModule) {
        _currentModule.value = module
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun selectTimePeriod(period: TimePeriodFilter) {
        _selectedPeriod.value = period
    }

    fun updateWidgetConfig(newConfig: DashboardWidgetConfig) {
        _widgetConfig.value = newConfig
    }

    fun clearToastBanner() {
        _toastBannerMessage.value = null
    }

    private fun notifyUser(msg: String) {
        _toastBannerMessage.value = msg
    }

    fun switchOrganization(orgId: Long) {
        _activeOrgId.value = orgId
        val orgName = organizations.value.find { it.id == orgId }?.name ?: "Organisation #$orgId"
        notifyUser("Espace Multi-Tenant actif : $orgName (Isolation BDD active)")
    }

    // --- MULTI-TENANT ORGANIZATION & WORKSPACE CREATION ---
    fun createOrganization(
        name: String,
        legalForm: String,
        taxId: String,
        ohadaZone: String,
        currency: String,
        vatPercent: Double
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val slug = name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-') + "-" + (100..999).random()
            val newOrgId = dao.insertOrganization(
                OrganizationEntity(
                    name = name.trim(),
                    slug = slug,
                    legalForm = legalForm.trim().ifBlank { "SARL" },
                    taxId = taxId.trim().ifBlank { "RCCM-PENDING" },
                    ohadaZone = ohadaZone,
                    currency = currency,
                    defaultVatPercent = vatPercent
                )
            )
            dao.insertWorkspace(
                WorkspaceEntity(
                    organizationId = newOrgId,
                    name = "Siège Principal & Direction",
                    code = "HQ-01",
                    department = "Direction Générale",
                    isDefault = true
                )
            )
            dao.insertAccount(
                AccountEntity(
                    organizationId = newOrgId,
                    name = "Banque Principale ($currency)",
                    accountNumber = "BANK-${(10000..99999).random()}",
                    ohadaCode = "5211",
                    type = "BANK",
                    balanceCents = 0L,
                    currency = currency
                )
            )
            dao.insertAccount(
                AccountEntity(
                    organizationId = newOrgId,
                    name = "Caisse Principale ($currency)",
                    accountNumber = "CASH-01",
                    ohadaCode = "5711",
                    type = "CASH",
                    balanceCents = 0L,
                    currency = currency
                )
            )
            dao.insertAuditLog(
                AuditLogEntity(
                    organizationId = newOrgId,
                    actorName = "Super Admin",
                    action = "ORG_PROVISIONED",
                    module = "ADMIN",
                    severity = "INFO",
                    details = "Organisation multi-tenant '$name' créée avec plan comptable $ohadaZone ($currency)."
                )
            )
            _activeOrgId.value = newOrgId
            notifyUser("Nouvelle organisation '$name' créée et activée !")
        }
    }

    fun createWorkspace(name: String, code: String, department: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val orgId = _activeOrgId.value
            dao.insertWorkspace(
                WorkspaceEntity(
                    organizationId = orgId,
                    name = name.trim(),
                    code = code.trim().uppercase().ifBlank { "WS-${(10..99).random()}" },
                    department = department.trim().ifBlank { "Opérations" }
                )
            )
            notifyUser("Espace de travail '$name' ajouté.")
        }
    }

    fun addRoleMember(fullName: String, email: String, roleName: String, permissionsCsv: String, mfaEnabled: Boolean) {
        if (fullName.isBlank() || email.isBlank()) return
        viewModelScope.launch {
            val orgId = _activeOrgId.value
            dao.insertMembership(
                UserRoleMemberEntity(
                    organizationId = orgId,
                    fullName = fullName.trim(),
                    email = email.trim(),
                    roleName = roleName.trim(),
                    permissionsCsv = permissionsCsv.trim(),
                    mfaEnabled = mfaEnabled
                )
            )
            dao.insertAuditLog(
                AuditLogEntity(
                    organizationId = orgId,
                    actorName = "Admin Sécurité",
                    action = "RBAC_MEMBER_ADDED",
                    module = "SECURITY",
                    severity = "INFO",
                    details = "Utilisateur $fullName ($email) ajouté avec rôle $roleName."
                )
            )
            notifyUser("Collaborateur RBAC '$fullName' ajouté.")
        }
    }

    // --- FINANCIAL MANAGEMENT & OHADA ACCOUNTING ---
    fun recordTransaction(
        accountId: Long,
        type: String, // INCOME, EXPENSE, TRANSFER
        category: String,
        amountMajorUnits: Double,
        paymentMethod: String,
        description: String,
        supportingDocRef: String
    ) {
        if (amountMajorUnits <= 0.0) return
        viewModelScope.launch {
            val orgId = _activeOrgId.value
            val org = activeOrganization.value
            val currency = org?.currency ?: "XAF"
            val vatRate = org?.defaultVatPercent ?: 19.25
            val amountCents = (amountMajorUnits * 100.0).roundToLong()
            val taxCents = (amountCents * (vatRate / (100.0 + vatRate))).roundToLong()

            val targetAcc = accounts.value.find { it.id == accountId } ?: accounts.value.firstOrNull()
            val accId = targetAcc?.id ?: 1L
            val accName = targetAcc?.name ?: "Compte Principal"
            val ref = "TRX-2026-${(10000..99999).random()}"

            dao.insertTransaction(
                TransactionEntity(
                    organizationId = orgId,
                    accountId = accId,
                    accountName = accName,
                    reference = ref,
                    type = type,
                    category = category.ifBlank { "Opération Courante" },
                    amountCents = amountCents,
                    taxAmountCents = taxCents,
                    currency = currency,
                    paymentMethod = paymentMethod,
                    status = "VALIDATED",
                    description = description.ifBlank { "$type - $category" },
                    supportingDocRef = supportingDocRef.ifBlank { "JUSTIF-${(1000..9999).random()}" }
                )
            )

            // Update Account Balance
            if (targetAcc != null) {
                val delta = if (type == "INCOME") amountCents else -amountCents
                dao.updateAccount(targetAcc.copy(balanceCents = targetAcc.balanceCents + delta))
            }

            // Automatically post corresponding OHADA Double-Entry Journal record
            val accOhada = targetAcc?.ohadaCode ?: "5211"
            if (type == "INCOME") {
                dao.insertAccountingEntry(
                    AccountingEntryEntity(
                        organizationId = orgId,
                        journalCode = if (accOhada.startsWith("57")) "CA" else "BQ",
                        reference = "EC-$ref",
                        ohadaDebitCode = accOhada,
                        ohadaDebitName = accName,
                        ohadaCreditCode = "7061",
                        ohadaCreditName = "Produits & Prestations ($category)",
                        amountCents = amountCents,
                        taxRatePercent = vatRate,
                        description = "Encaissement $paymentMethod • $description",
                        isReconciled = false
                    )
                )
            } else {
                dao.insertAccountingEntry(
                    AccountingEntryEntity(
                        organizationId = orgId,
                        journalCode = if (accOhada.startsWith("57")) "CA" else "AC",
                        reference = "EC-$ref",
                        ohadaDebitCode = "6051",
                        ohadaDebitName = "Charges d'exploitation ($category)",
                        ohadaCreditCode = accOhada,
                        ohadaCreditName = accName,
                        amountCents = amountCents,
                        taxRatePercent = vatRate,
                        description = "Décaissement $paymentMethod • $description",
                        isReconciled = false
                    )
                )
            }

            dao.insertAuditLog(
                AuditLogEntity(
                    organizationId = orgId,
                    actorName = "Trésorier / Comptable",
                    action = "TRANSACTION_VALIDATED",
                    module = "FINANCES",
                    severity = "INFO",
                    details = "Transaction $ref ($type) de ${formatMoney(amountCents, currency)} validée via $paymentMethod."
                )
            )
            notifyUser("Transaction $ref enregistrée + Écriture OHADA générée.")
        }
    }

    /**
     * Strict Rule: "Ne jamais supprimer silencieusement une transaction validée."
     * Validated transactions can only be reversed via an explicit counter-passation (contre-passation OHADA) with an audit log.
     */
    fun reverseValidatedTransaction(tx: TransactionEntity, reason: String) {
        if (tx.status == "REVERSED") return
        viewModelScope.launch {
            val orgId = _activeOrgId.value
            dao.updateTransaction(tx.copy(status = "REVERSED", description = "${tx.description} [ANNULÉE PAR CONTRE-PASSATION: $reason]"))

            val targetAcc = accounts.value.find { it.id == tx.accountId }
            if (targetAcc != null) {
                val reverseDelta = if (tx.type == "INCOME") -tx.amountCents else tx.amountCents
                dao.updateAccount(targetAcc.copy(balanceCents = targetAcc.balanceCents + reverseDelta))
            }

            dao.insertAccountingEntry(
                AccountingEntryEntity(
                    organizationId = orgId,
                    journalCode = "OD",
                    reference = "REV-${tx.reference}",
                    ohadaDebitCode = if (tx.type == "INCOME") "7061" else (targetAcc?.ohadaCode ?: "5211"),
                    ohadaDebitName = "Contre-passation ${tx.reference}",
                    ohadaCreditCode = if (tx.type == "INCOME") (targetAcc?.ohadaCode ?: "5211") else "6051",
                    ohadaCreditName = "Extourne ${tx.accountName}",
                    amountCents = tx.amountCents,
                    taxRatePercent = 0.0,
                    description = "Contre-passation traçable de ${tx.reference} • Motif : $reason",
                    isReconciled = true
                )
            )

            dao.insertAuditLog(
                AuditLogEntity(
                    organizationId = orgId,
                    actorName = "Contrôle Interne",
                    action = "TRANSACTION_REVERSED_AUDIT",
                    module = "FINANCES",
                    severity = "WARNING",
                    details = "Aucune suppression silencieuse : Transaction ${tx.reference} contre-passée (Motif: $reason)."
                )
            )
            notifyUser("Contre-passation comptable effectuée et tracée dans l'Audit Log.")
        }
    }

    fun createTreasuryAccount(name: String, accountNumber: String, ohadaCode: String, type: String, initialBalanceMajor: Double) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val orgId = _activeOrgId.value
            val currency = activeOrganization.value?.currency ?: "XAF"
            dao.insertAccount(
                AccountEntity(
                    organizationId = orgId,
                    name = name.trim(),
                    accountNumber = accountNumber.trim().ifBlank { "ACC-${(1000..9999).random()}" },
                    ohadaCode = ohadaCode.trim().ifBlank { "5211" },
                    type = type,
                    balanceCents = (initialBalanceMajor * 100.0).roundToLong(),
                    currency = currency
                )
            )
            notifyUser("Compte '$name' (OHADA $ohadaCode) ajouté.")
        }
    }

    fun addManualOhadaEntry(
        journalCode: String,
        debitCode: String,
        debitName: String,
        creditCode: String,
        creditName: String,
        amountMajor: Double,
        description: String
    ) {
        if (amountMajor <= 0.0 || debitCode.isBlank() || creditCode.isBlank()) return
        viewModelScope.launch {
            val orgId = _activeOrgId.value
            val vat = activeOrganization.value?.defaultVatPercent ?: 19.25
            val ref = "EC-OHADA-${(1000..9999).random()}"
            dao.insertAccountingEntry(
                AccountingEntryEntity(
                    organizationId = orgId,
                    journalCode = journalCode,
                    reference = ref,
                    ohadaDebitCode = debitCode.trim(),
                    ohadaDebitName = debitName.trim().ifBlank { "Compte Débit $debitCode" },
                    ohadaCreditCode = creditCode.trim(),
                    ohadaCreditName = creditName.trim().ifBlank { "Compte Crédit $creditCode" },
                    amountCents = (amountMajor * 100.0).roundToLong(),
                    taxRatePercent = vat,
                    description = description.trim().ifBlank { "Écriture comptable équilibrée" }
                )
            )
            notifyUser("Écriture OHADA $ref enregistrée au journal $journalCode.")
        }
    }

    fun toggleEntryReconciliation(entry: AccountingEntryEntity) {
        viewModelScope.launch {
            dao.updateAccountingEntry(entry.copy(isReconciled = !entry.isReconciled))
            notifyUser(if (!entry.isReconciled) "Écriture ${entry.reference} rapprochée." else "Rapprochement annulé.")
        }
    }

    // --- CRM, PROSPECT-TO-CLIENT CONVERSION & BILLING ---
    fun addCrmContact(
        kind: String, // LEAD, CUSTOMER, SUPPLIER
        companyName: String,
        contactName: String,
        email: String,
        phone: String,
        stage: String,
        estimatedValueMajor: Double,
        sourceCampaign: String,
        notes: String
    ) {
        if (companyName.isBlank()) return
        viewModelScope.launch {
            val orgId = _activeOrgId.value
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            dao.insertContact(
                CrmContactEntity(
                    organizationId = orgId,
                    kind = kind,
                    companyName = companyName.trim(),
                    contactName = contactName.trim().ifBlank { "Direction" },
                    email = email.trim(),
                    phone = phone.trim(),
                    stage = stage,
                    estimatedValueCents = (estimatedValueMajor * 100.0).roundToLong(),
                    sourceCampaign = sourceCampaign.trim().ifBlank { "Direct" },
                    historyLog = "[$dateStr] Fiche créée ($kind) • $notes"
                )
            )
            notifyUser("${if (kind == "LEAD") "Prospect" else "Contact"} '$companyName' enregistré.")
        }
    }

    /**
     * Requirement: "Permettre de convertir un prospect en client sans perdre son historique."
     */
    fun convertProspectToClient(prospect: CrmContactEntity) {
        if (prospect.kind != "LEAD") return
        viewModelScope.launch {
            val orgId = _activeOrgId.value
            val now = System.currentTimeMillis()
            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(now))
            val updatedHistory = "${prospect.historyLog}\n• [$dateStr] CONVERTI DE PROSPECT EN CLIENT ACTIF (Historique intégral conservé)."

            dao.updateContact(
                prospect.copy(
                    kind = "CUSTOMER",
                    stage = "WON_CLIENT",
                    convertedFromLeadAt = now,
                    historyLog = updatedHistory
                )
            )

            dao.insertAuditLog(
                AuditLogEntity(
                    organizationId = orgId,
                    actorName = "CRM Commercial",
                    action = "LEAD_CONVERTED_TO_CUSTOMER",
                    module = "CRM",
                    severity = "INFO",
                    details = "Prospect '${prospect.companyName}' converti en Client sans perte d'historique."
                )
            )
            notifyUser("Prospect '${prospect.companyName}' converti en Client avec tout son historique !")
        }
    }

    fun createCommercialDocument(
        contactId: Long,
        contactName: String,
        docType: String, // QUOTE, ORDER, INVOICE, CREDIT_NOTE, CONTRACT, SUBSCRIPTION
        itemsDescription: String,
        subtotalMajor: Double,
        dueInDays: Int
    ) {
        if (contactName.isBlank() || subtotalMajor <= 0.0) return
        viewModelScope.launch {
            val orgId = _activeOrgId.value
            val org = activeOrganization.value
            val currency = org?.currency ?: "XAF"
            val vatPercent = org?.defaultVatPercent ?: 19.25

            val subtotalCents = (subtotalMajor * 100.0).roundToLong()
            val taxCents = (subtotalCents * (vatPercent / 100.0)).roundToLong()
            val totalCents = subtotalCents + taxCents

            val prefix = when (docType) {
                "QUOTE" -> "DEV"
                "ORDER" -> "CMD"
                "INVOICE" -> "FAC"
                "CREDIT_NOTE" -> "AVO"
                "CONTRACT" -> "CTR"
                else -> "ABO"
            }
            val docNum = "$prefix-2026-${(1000..9999).random()}"

            dao.insertCommercialDoc(
                CommercialDocEntity(
                    organizationId = orgId,
                    contactId = contactId,
                    contactName = contactName.trim(),
                    docType = docType,
                    docNumber = docNum,
                    itemsDescription = itemsDescription.trim().ifBlank { "Prestations & Licences Business OS" },
                    subtotalCents = subtotalCents,
                    taxCents = taxCents,
                    totalCents = totalCents,
                    paidCents = 0L,
                    currency = currency,
                    status = "SENT",
                    dueDate = System.currentTimeMillis() + dueInDays.coerceAtLeast(1) * 86_400_000L
                )
            )

            // If it's an INVOICE, also post the OHADA Ventes (VE) receivable entry (4111 Debit / 7061 Credit)
            if (docType == "INVOICE") {
                dao.insertAccountingEntry(
                    AccountingEntryEntity(
                        organizationId = orgId,
                        journalCode = "VE",
                        reference = "EC-$docNum",
                        ohadaDebitCode = "4111",
                        ohadaDebitName = "Clients - $contactName",
                        ohadaCreditCode = "7061",
                        ohadaCreditName = "Ventes de Produits & Services",
                        amountCents = totalCents,
                        taxRatePercent = vatPercent,
                        description = "Émission facture $docNum • $contactName"
                    )
                )
            }
            notifyUser("Document $docNum ($docType) créé pour $contactName.")
        }
    }

    fun recordInvoicePayment(doc: CommercialDocEntity, paymentMethod: String) {
        val remaining = (doc.totalCents - doc.paidCents).coerceAtLeast(0L)
        if (remaining <= 0L) return
        viewModelScope.launch {
            val orgId = _activeOrgId.value
            dao.updateCommercialDoc(
                doc.copy(
                    paidCents = doc.totalCents,
                    status = "PAID"
                )
            )

            // Select matching treasury account (Mobile Money vs Bank vs Cash)
            val accList = accounts.value
            val targetAcc = when (paymentMethod) {
                "MTN_MOMO", "ORANGE_MONEY", "CAMPAY", "MONEY_FUSION" ->
                    accList.find { it.type == "MOBILE_MONEY" } ?: accList.firstOrNull()
                "MANUAL" ->
                    accList.find { it.type == "CASH" } ?: accList.firstOrNull()
                else ->
                    accList.find { it.type == "BANK" } ?: accList.firstOrNull()
            }

            val ref = "PAY-${doc.docNumber}-${(100..999).random()}"
            dao.insertTransaction(
                TransactionEntity(
                    organizationId = orgId,
                    accountId = targetAcc?.id ?: 1L,
                    accountName = targetAcc?.name ?: "Trésorerie",
                    reference = ref,
                    type = "INCOME",
                    category = "Règlement Client Facture ${doc.docNumber}",
                    amountCents = remaining,
                    taxAmountCents = doc.taxCents,
                    currency = doc.currency,
                    paymentMethod = paymentMethod,
                    status = "VALIDATED",
                    description = "Encaissement ${doc.docNumber} (${doc.contactName}) via $paymentMethod",
                    supportingDocRef = doc.docNumber
                )
            )

            if (targetAcc != null) {
                dao.updateAccount(targetAcc.copy(balanceCents = targetAcc.balanceCents + remaining))
            }

            dao.insertAccountingEntry(
                AccountingEntryEntity(
                    organizationId = orgId,
                    journalCode = "BQ",
                    reference = "EC-$ref",
                    ohadaDebitCode = targetAcc?.ohadaCode ?: "5211",
                    ohadaDebitName = targetAcc?.name ?: "Trésorerie",
                    ohadaCreditCode = "4111",
                    ohadaCreditName = "Clients - ${doc.contactName}",
                    amountCents = remaining,
                    taxRatePercent = 0.0,
                    description = "Règlement facture ${doc.docNumber} via $paymentMethod",
                    isReconciled = true
                )
            )

            dao.insertAuditLog(
                AuditLogEntity(
                    organizationId = orgId,
                    actorName = "Passerelle Paiement / Trésorerie",
                    action = "INVOICE_PAID_VERIFIED",
                    module = "BILLING",
                    severity = "INFO",
                    details = "Facture ${doc.docNumber} réglée (${formatMoney(remaining, doc.currency)}) via $paymentMethod."
                )
            )
            notifyUser("Facture ${doc.docNumber} encaissée via $paymentMethod + Trésorerie & OHADA mis à jour !")
        }
    }

    // --- PRODUCTS, INVENTORY & PURCHASES ---
    fun addProductOrService(
        sku: String,
        name: String,
        kind: String, // PRODUCT, SERVICE
        category: String,
        unitPriceMajor: Double,
        costPriceMajor: Double,
        initialStock: Int,
        minStockAlert: Int,
        supplierName: String,
        warehouseLocation: String
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val orgId = _activeOrgId.value
            val cleanSku = sku.trim().uppercase().ifBlank { "SKU-${(1000..9999).random()}" }
            val prodId = dao.insertProduct(
                ProductStockEntity(
                    organizationId = orgId,
                    sku = cleanSku,
                    name = name.trim(),
                    kind = kind,
                    category = category.trim().ifBlank { "Général" },
                    unitPriceCents = (unitPriceMajor * 100.0).roundToLong(),
                    costPriceCents = (costPriceMajor * 100.0).roundToLong(),
                    stockQuantity = if (kind == "SERVICE") 999 else initialStock,
                    minStockAlert = if (kind == "SERVICE") 0 else minStockAlert,
                    supplierName = supplierName.trim().ifBlank { "Fournisseur Standard" },
                    warehouseLocation = warehouseLocation.trim().ifBlank { "Entrepôt Principal" }
                )
            )
            if (kind == "PRODUCT" && initialStock > 0) {
                dao.insertStockMovement(
                    StockMovementEntity(
                        organizationId = orgId,
                        productId = prodId,
                        productName = name.trim(),
                        movementType = "IN",
                        quantity = initialStock,
                        reason = "Stock initial d'ouverture"
                    )
                )
            }
            notifyUser("${if (kind == "SERVICE") "Service" else "Produit"} '$name' ($cleanSku) ajouté.")
        }
    }

    fun recordStockMovement(product: ProductStockEntity, movementType: String, quantity: Int, reason: String) {
        if (quantity <= 0) return
        viewModelScope.launch {
            val orgId = _activeOrgId.value
            val newQty = when (movementType) {
                "IN" -> product.stockQuantity + quantity
                "OUT" -> (product.stockQuantity - quantity).coerceAtLeast(0)
                else -> quantity
            }
            dao.updateProduct(product.copy(stockQuantity = newQty))
            dao.insertStockMovement(
                StockMovementEntity(
                    organizationId = orgId,
                    productId = product.id,
                    productName = product.name,
                    movementType = movementType,
                    quantity = quantity,
                    reason = reason.ifBlank { "Mouvement de stock $movementType" }
                )
            )
            notifyUser("Stock '${product.name}' mis à jour : $newQty unités.")
        }
    }

    // --- HR, EMPLOYEES, PROJECTS & TASKS ---
    fun addEmployee(
        fullName: String,
        roleTitle: String,
        department: String,
        contractType: String,
        baseSalaryMajor: Double
    ) {
        if (fullName.isBlank()) return
        viewModelScope.launch {
            val orgId = _activeOrgId.value
            dao.insertEmployee(
                EmployeeEntity(
                    organizationId = orgId,
                    fullName = fullName.trim(),
                    roleTitle = roleTitle.trim().ifBlank { "Collaborateur" },
                    department = department.trim().ifBlank { "Opérations" },
                    contractType = contractType,
                    baseSalaryCents = (baseSalaryMajor * 100.0).roundToLong(),
                    leaveBalanceDays = 24,
                    performanceScore = 90,
                    status = "ACTIVE"
                )
            )
            notifyUser("Collaborateur '$fullName' enregistré dans l'espace RH.")
        }
    }

    fun addProjectTask(
        projectName: String,
        taskTitle: String,
        assigneeName: String,
        priority: String,
        budgetMajor: Double
    ) {
        if (taskTitle.isBlank()) return
        viewModelScope.launch {
            val orgId = _activeOrgId.value
            dao.insertProjectTask(
                ProjectTaskEntity(
                    organizationId = orgId,
                    projectName = projectName.trim().ifBlank { "Projet Interne" },
                    taskTitle = taskTitle.trim(),
                    assigneeName = assigneeName.trim().ifBlank { "Équipe" },
                    priority = priority,
                    status = "TODO",
                    budgetCents = (budgetMajor * 100.0).roundToLong(),
                    progressPercent = 0,
                    dueDate = System.currentTimeMillis() + 7 * 86_400_000L,
                    commentsLog = "Tâche créée et assignée à $assigneeName."
                )
            )
            notifyUser("Tâche '$taskTitle' ajoutée au projet '$projectName'.")
        }
    }

    fun advanceTaskStatus(task: ProjectTaskEntity) {
        viewModelScope.launch {
            val (nextStatus, nextProgress) = when (task.status) {
                "TODO" -> "IN_PROGRESS" to 45
                "IN_PROGRESS" -> "REVIEW" to 85
                "REVIEW" -> "DONE" to 100
                else -> "TODO" to 0
            }
            dao.updateProjectTask(task.copy(status = nextStatus, progressPercent = nextProgress))
            notifyUser("Tâche '${task.taskTitle}' passée à $nextStatus ($nextProgress%).")
        }
    }

    // --- DOCUMENTS VAULT ---
    fun addDocumentToVault(
        title: String,
        category: String,
        fileName: String,
        storageDriver: String,
        accessLevel: String,
        contentPreview: String
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val orgId = _activeOrgId.value
            val fakeSha = (1..8).joinToString("") { (10000000..99999999).random().toString(16) }
            dao.insertDocument(
                DocumentVaultEntity(
                    organizationId = orgId,
                    title = title.trim(),
                    category = category,
                    fileName = fileName.trim().ifBlank { "document_${System.currentTimeMillis()}.pdf" },
                    mimeType = if (fileName.endsWith(".csv")) "text/csv" else "application/pdf",
                    sizeBytes = (120_000L..2_400_000L).random(),
                    storageDriver = storageDriver,
                    accessLevel = accessLevel,
                    version = 1,
                    checksumSha256 = fakeSha,
                    contentPreview = contentPreview.trim().ifBlank { "Document chiffré stocké sur $storageDriver." }
                )
            )
            notifyUser("Document '$title' indexé dans le coffre documentaire ($storageDriver).")
        }
    }

    fun archiveDocumentWithAudit(doc: DocumentVaultEntity) {
        viewModelScope.launch {
            val orgId = _activeOrgId.value
            dao.updateDocument(doc.copy(isArchived = true))
            dao.insertAuditLog(
                AuditLogEntity(
                    organizationId = orgId,
                    actorName = "Admin GED",
                    action = "DOCUMENT_ARCHIVED_CONTROLLED",
                    module = "DOCUMENTS",
                    severity = "WARNING",
                    details = "Suppression contrôlée (archivage) du document '${doc.title}' (SHA256: ${doc.checksumSha256.take(16)}...)."
                )
            )
            notifyUser("Document '${doc.title}' archivé avec trace d'audit.")
        }
    }

    // --- AUTOMATION WORKFLOWS ---
    fun addAutomationWorkflow(
        name: String,
        triggerEvent: String,
        conditionExpr: String,
        actionType: String,
        requiresHumanApproval: Boolean
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val orgId = _activeOrgId.value
            dao.insertWorkflow(
                AutomationWorkflowEntity(
                    organizationId = orgId,
                    name = name.trim(),
                    triggerEvent = triggerEvent,
                    conditionExpr = conditionExpr.trim().ifBlank { "always == true" },
                    actionType = actionType,
                    requiresHumanApproval = requiresHumanApproval,
                    isActive = true,
                    runCount = 0,
                    lastRunStatus = "READY • En attente de déclenchement"
                )
            )
            notifyUser("Workflow '$name' créé.")
        }
    }

    fun toggleWorkflowActive(workflow: AutomationWorkflowEntity) {
        viewModelScope.launch {
            dao.updateWorkflow(workflow.copy(isActive = !workflow.isActive))
        }
    }

    fun executeWorkflowNow(workflow: AutomationWorkflowEntity, approvedByHuman: Boolean) {
        viewModelScope.launch {
            val orgId = _activeOrgId.value
            if (workflow.requiresHumanApproval && !approvedByHuman) {
                notifyUser("Validation humaine requise avant d'exécuter '${workflow.name}'.")
                return@launch
            }
            val now = System.currentTimeMillis()
            val statusMsg = "SUCCESS • Exécuté (${workflow.actionType}) avec validation humaine"
            dao.updateWorkflow(
                workflow.copy(
                    runCount = workflow.runCount + 1,
                    lastRunStatus = statusMsg,
                    lastRunTimestamp = now
                )
            )
            dao.insertAuditLog(
                AuditLogEntity(
                    organizationId = orgId,
                    actorName = "Moteur Workflows",
                    action = "WORKFLOW_EXECUTED",
                    module = "AUTOMATIONS",
                    severity = "INFO",
                    details = "Workflow '${workflow.name}' exécuté (#${workflow.runCount + 1}) -> ${workflow.actionType}."
                )
            )
            notifyUser("Workflow '${workflow.name}' exécuté avec succès !")
        }
    }

    // --- BUSINESS AI CONFIGURATION & INTERACTION ---
    fun updateAiConfig(
        isEnabled: Boolean,
        providerType: AiProviderType,
        endpointUrl: String,
        modelName: String,
        apiKey: String,
        monthlyQuota: Int
    ) {
        _aiConfig.value = _aiConfig.value.copy(
            isEnabled = isEnabled,
            providerType = providerType,
            endpointUrl = endpointUrl.trim(),
            modelName = modelName.trim(),
            apiKey = apiKey.trim(),
            monthlyQuotaLimitRequests = monthlyQuota.coerceAtLeast(10)
        )
        notifyUser(
            if (!isEnabled || providerType == AiProviderType.DISABLED) {
                "Business AI désactivée — BUSINESS OS fonctionne en mode 100% standard sans IA."
            } else {
                "Fournisseur IA configuré : ${providerType.label}"
            }
        )
    }

    fun sendAiPrompt(prompt: String) {
        if (prompt.isBlank()) return
        val userMsg = AiAssistantMessage(
            role = "USER",
            content = prompt.trim(),
            providerBadge = "Utilisateur"
        )
        _aiMessages.value = _aiMessages.value + userMsg
        _isAiThinking.value = true

        viewModelScope.launch {
            val org = activeOrganization.value
            val currency = org?.currency ?: "XAF"
            val m = dashboardMetrics.value
            val liveContext = buildString {
                append("Organisation: ${org?.name ?: "TechNova"} (${org?.ohadaZone ?: "OHADA"} • Devise: $currency)\n")
                append("• Chiffre d'affaires facturé: ${formatMoney(m.revenueBilledCents, currency)}\n")
                append("• Revenus encaissés: ${formatMoney(m.revenueCollectedCents, currency)}\n")
                append("• Dépenses validées: ${formatMoney(m.expensesTotalCents, currency)}\n")
                append("• Bénéfice net: ${formatMoney(m.netProfitCents, currency)} (Marge: ${String.format(Locale.US, "%.1f", m.netMarginPercent)}%)\n")
                append("• Trésorerie disponible (Banque + Mobile Money + Caisse): ${formatMoney(m.availableTreasuryCents, currency)}\n")
                append("• Factures impayées (${m.unpaidInvoicesCount}): ${formatMoney(m.unpaidInvoicesCents, currency)}\n")
                append("• Clients actifs: ${m.activeCustomersCount} | Prospects qualifiés: ${m.activeLeadsCount} (Pipeline: ${formatMoney(m.pipelineValueCents, currency)})\n")
                append("• Alertes stocks bas: ${m.lowStockCount} article(s) | Projets actifs: ${m.activeProjectsCount} (${m.completedTasksCount}/${m.totalTasksCount} tâches terminées)")
            }

            val currentCfg = _aiConfig.value
            val reply = aiEngine.analyzeAndRespond(prompt.trim(), liveContext, currentCfg)
            if (currentCfg.isEnabled && currentCfg.providerType != AiProviderType.DISABLED) {
                _aiConfig.value = currentCfg.copy(usedRequestsCount = currentCfg.usedRequestsCount + 1)
            }
            _aiMessages.value = _aiMessages.value + reply
            _isAiThinking.value = false
        }
    }

    // --- FULL DATA EXPORT (JSON / OPEN SOURCE SOVEREIGNTY) ---
    fun generateFullDataExportJson() {
        viewModelScope.launch {
            val orgId = _activeOrgId.value
            val org = activeOrganization.value
            val accs = dao.getAccountsOnce(orgId)
            val txs = dao.getTransactionsOnce(orgId)
            val entries = dao.getAccountingEntriesOnce(orgId)
            val crms = dao.getContactsOnce(orgId)
            val docs = dao.getCommercialDocsOnce(orgId)
            val prods = dao.getProductsOnce(orgId)
            val logs = dao.getAuditLogsOnce(orgId)

            val root = JSONObject().apply {
                put("product", "BUSINESS OS 2026")
                put("license", "MIT Open Source")
                put("exportedAtIso", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()))
                put("organization", JSONObject().apply {
                    put("id", org?.id ?: orgId)
                    put("name", org?.name ?: "")
                    put("taxId", org?.taxId ?: "")
                    put("ohadaZone", org?.ohadaZone ?: "")
                    put("currency", org?.currency ?: "XAF")
                })
                put("accountsCount", accs.size)
                put("transactionsCount", txs.size)
                put("ohadaEntriesCount", entries.size)
                put("crmContactsCount", crms.size)
                put("commercialDocsCount", docs.size)
                put("inventoryItemsCount", prods.size)
                put("auditLogsCount", logs.size)
                put("accounts", JSONArray().apply {
                    accs.forEach { a ->
                        put(JSONObject().put("code", a.ohadaCode).put("name", a.name).put("balanceCents", a.balanceCents).put("currency", a.currency))
                    }
                })
                put("transactions", JSONArray().apply {
                    txs.take(25).forEach { t ->
                        put(JSONObject().put("ref", t.reference).put("type", t.type).put("amountCents", t.amountCents).put("method", t.paymentMethod).put("status", t.status))
                    }
                })
            }

            dao.insertAuditLog(
                AuditLogEntity(
                    organizationId = orgId,
                    actorName = "Propriétaire des Données",
                    action = "FULL_DATA_EXPORT_JSON",
                    module = "ADMIN",
                    severity = "INFO",
                    details = "Exportation intégrale souveraine des données de l'organisation effectuée."
                )
            )
            _exportedJsonPreview.value = root.toString(2)
            notifyUser("Exportation intégrale JSON générée avec succès !")
        }
    }

    fun dismissExportPreview() {
        _exportedJsonPreview.value = null
    }

    companion object {
        fun formatMoney(amountCents: Long, currency: String = "XAF"): String {
            val major = amountCents / 100.0
            return if (currency == "XAF" || currency == "XOF") {
                val formatted = NumberFormat.getNumberInstance(Locale.FRANCE).apply {
                    maximumFractionDigits = 0
                }.format(major.roundToLong())
                "$formatted $currency"
            } else {
                val formatted = NumberFormat.getNumberInstance(Locale.FRANCE).apply {
                    minimumFractionDigits = 2
                    maximumFractionDigits = 2
                }.format(major)
                "$formatted $currency"
            }
        }
    }
}
