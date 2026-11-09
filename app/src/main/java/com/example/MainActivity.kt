package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.BusinessOsViewModel
import com.example.ui.viewmodel.ErpPrimaryModule

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: BusinessOsViewModel = viewModel()
            val isDark by vm.isDarkMode.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = isDark) {
                BusinessOsRootShell(vm = vm)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessOsRootShell(vm: BusinessOsViewModel) {
    val isDark by vm.isDarkMode.collectAsStateWithLifecycle()
    val currentModule by vm.currentModule.collectAsStateWithLifecycle()
    val selectedPeriod by vm.selectedPeriod.collectAsStateWithLifecycle()
    val widgetConfig by vm.widgetConfig.collectAsStateWithLifecycle()
    val toastMessage by vm.toastBannerMessage.collectAsStateWithLifecycle()

    val organizations by vm.organizations.collectAsStateWithLifecycle()
    val activeOrg by vm.activeOrganization.collectAsStateWithLifecycle()
    val workspaces by vm.workspaces.collectAsStateWithLifecycle()
    val memberships by vm.memberships.collectAsStateWithLifecycle()
    val accounts by vm.accounts.collectAsStateWithLifecycle()
    val transactions by vm.transactions.collectAsStateWithLifecycle()
    val accountingEntries by vm.accountingEntries.collectAsStateWithLifecycle()
    val contacts by vm.contacts.collectAsStateWithLifecycle()
    val commercialDocs by vm.commercialDocs.collectAsStateWithLifecycle()
    val products by vm.products.collectAsStateWithLifecycle()
    val stockMovements by vm.stockMovements.collectAsStateWithLifecycle()
    val employees by vm.employees.collectAsStateWithLifecycle()
    val projectTasks by vm.projectTasks.collectAsStateWithLifecycle()
    val documents by vm.documents.collectAsStateWithLifecycle()
    val workflows by vm.workflows.collectAsStateWithLifecycle()
    val auditLogs by vm.auditLogs.collectAsStateWithLifecycle()
    val metrics by vm.dashboardMetrics.collectAsStateWithLifecycle()

    val aiConfig by vm.aiConfig.collectAsStateWithLifecycle()
    val aiMessages by vm.aiMessages.collectAsStateWithLifecycle()
    val isAiThinking by vm.isAiThinking.collectAsStateWithLifecycle()
    val exportedJsonPreview by vm.exportedJsonPreview.collectAsStateWithLifecycle()

    var showOrgSwitcherMenu by remember { mutableStateOf(false) }

    // BackHandler returns to DASHBOARD when in any secondary ERP module
    BackHandler(enabled = currentModule != ErpPrimaryModule.DASHBOARD) {
        vm.selectModule(ErpPrimaryModule.DASHBOARD)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ElectricGreen.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Hexagon,
                                    contentDescription = "Business OS Logo",
                                    tint = ElectricGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "BUSINESS OS",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = ElectricGreen.copy(alpha = 0.18f)
                                    ) {
                                        Text(
                                            text = "2026 MIT",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ElectricGreen,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = currentModule.title + " • " + currentModule.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Organization Multi-Tenant Switcher & Theme Toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box {
                                OutlinedButton(
                                    onClick = { showOrgSwitcherMenu = true },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    border = BorderStroke(1.dp, ElectricGreen.copy(alpha = 0.55f)),
                                    modifier = Modifier.testTag("org_switcher_button")
                                ) {
                                    Icon(Icons.Default.Business, contentDescription = null, tint = ElectricGreen, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = activeOrg?.currency ?: "XAF",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                }

                                DropdownMenu(
                                    expanded = showOrgSwitcherMenu,
                                    onDismissRequest = { showOrgSwitcherMenu = false }
                                ) {
                                    organizations.forEach { org ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(org.name, fontWeight = FontWeight.Bold)
                                                    Text("${org.ohadaZone} • ${org.currency}", style = MaterialTheme.typography.bodySmall)
                                                }
                                            },
                                            onClick = {
                                                vm.switchOrganization(org.id)
                                                showOrgSwitcherMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = { vm.toggleDarkMode() },
                                modifier = Modifier.size(40.dp).testTag("theme_toggle_button")
                            ) {
                                Icon(
                                    imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Mode Sombre / Clair",
                                    tint = ElectricGreen
                                )
                            }
                        }
                    }

                    // Toast / Notification Feedback Banner
                    AnimatedVisibility(visible = toastMessage != null) {
                        toastMessage?.let { msg ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ElectricGreen.copy(alpha = 0.16f),
                                border = BorderStroke(1.dp, ElectricGreen),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { vm.clearToastBanner() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(Icons.Default.Close, contentDescription = "Fermer", tint = ElectricGreen, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val navItems: List<Pair<ErpPrimaryModule, ImageVector>> = listOf(
                        ErpPrimaryModule.DASHBOARD to Icons.Default.SpaceDashboard,
                        ErpPrimaryModule.FINANCES to Icons.Default.AccountBalance,
                        ErpPrimaryModule.CRM_SALES to Icons.Default.PointOfSale,
                        ErpPrimaryModule.OPERATIONS to Icons.Default.Inventory2,
                        ErpPrimaryModule.HR_PROJECTS to Icons.Default.Badge,
                        ErpPrimaryModule.DOCS_AI_AUTO to Icons.Default.AutoAwesome,
                        ErpPrimaryModule.ADMIN_SECURITY to Icons.Default.AdminPanelSettings
                    )

                    navItems.forEach { (mod, icon) ->
                        val selected = currentModule == mod
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) ElectricGreen else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                1.dp,
                                if (selected) ElectricGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .clickable { vm.selectModule(mod) }
                                .testTag("nav_tab_${mod.name}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = mod.title,
                                    tint = if (selected) ObsidianBlack else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = mod.title.substringBefore(","),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) ObsidianBlack else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentModule) {
                ErpPrimaryModule.DASHBOARD -> DashboardScreen(
                    organization = activeOrg,
                    metrics = metrics,
                    selectedPeriod = selectedPeriod,
                    widgetConfig = widgetConfig,
                    accounts = accounts,
                    transactions = transactions,
                    commercialDocs = commercialDocs,
                    products = products,
                    onSelectPeriod = vm::selectTimePeriod,
                    onUpdateWidgets = vm::updateWidgetConfig,
                    onNavigateModule = vm::selectModule
                )

                ErpPrimaryModule.FINANCES -> FinanceAccountingScreen(
                    organization = activeOrg,
                    accounts = accounts,
                    transactions = transactions,
                    accountingEntries = accountingEntries,
                    onRecordTransaction = vm::recordTransaction,
                    onReverseTransaction = vm::reverseValidatedTransaction,
                    onCreateAccount = vm::createTreasuryAccount,
                    onAddOhadaEntry = vm::addManualOhadaEntry,
                    onToggleReconcile = vm::toggleEntryReconciliation
                )

                ErpPrimaryModule.CRM_SALES -> CrmSalesBillingScreen(
                    organization = activeOrg,
                    contacts = contacts,
                    commercialDocs = commercialDocs,
                    onAddContact = vm::addCrmContact,
                    onConvertProspect = vm::convertProspectToClient,
                    onCreateDoc = vm::createCommercialDocument,
                    onPayInvoice = vm::recordInvoicePayment
                )

                ErpPrimaryModule.OPERATIONS -> OperationsStockPurchaseScreen(
                    organization = activeOrg,
                    products = products,
                    stockMovements = stockMovements,
                    suppliers = contacts.filter { it.kind == "SUPPLIER" },
                    onAddProduct = vm::addProductOrService,
                    onRecordStockMovement = vm::recordStockMovement,
                    onAddSupplier = { comp, person, email, phone, vol ->
                        vm.addCrmContact("SUPPLIER", comp, person, email, phone, "ACTIVE_PARTNER", vol, "Fournisseur Agréé", "Fournisseur référencé")
                    }
                )

                ErpPrimaryModule.HR_PROJECTS -> HrProjectsProductivityScreen(
                    organization = activeOrg,
                    employees = employees,
                    projectTasks = projectTasks,
                    onAddEmployee = vm::addEmployee,
                    onAddProjectTask = vm::addProjectTask,
                    onAdvanceTask = vm::advanceTaskStatus
                )

                ErpPrimaryModule.DOCS_AI_AUTO -> DocumentsAutomationsAiScreen(
                    documents = documents,
                    workflows = workflows,
                    aiConfig = aiConfig,
                    aiMessages = aiMessages,
                    isAiThinking = isAiThinking,
                    onAddDocument = vm::addDocumentToVault,
                    onArchiveDocument = vm::archiveDocumentWithAudit,
                    onAddWorkflow = vm::addAutomationWorkflow,
                    onToggleWorkflow = vm::toggleWorkflowActive,
                    onExecuteWorkflow = vm::executeWorkflowNow,
                    onUpdateAiConfig = vm::updateAiConfig,
                    onSendAiPrompt = vm::sendAiPrompt
                )

                ErpPrimaryModule.ADMIN_SECURITY -> AdminSecuritySettingsScreen(
                    organizations = organizations,
                    activeOrganization = activeOrg,
                    workspaces = workspaces,
                    memberships = memberships,
                    auditLogs = auditLogs,
                    exportedJsonPreview = exportedJsonPreview,
                    onSwitchOrganization = vm::switchOrganization,
                    onCreateOrganization = vm::createOrganization,
                    onCreateWorkspace = vm::createWorkspace,
                    onAddMember = vm::addRoleMember,
                    onExportFullJson = vm::generateFullDataExportJson,
                    onDismissExportPreview = vm::dismissExportPreview
                )
            }
        }
    }
}
