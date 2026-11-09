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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ErpStatusBadge
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.BusinessOsViewModel
import com.example.ui.viewmodel.ErpPrimaryModule
import kotlinx.coroutines.launch

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
    var showCommandPalette by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val allNavModules: List<Triple<ErpPrimaryModule, String, ImageVector>> = remember {
        listOf(
            Triple(ErpPrimaryModule.DASHBOARD, "Pilotage", Icons.Default.SpaceDashboard),
            Triple(ErpPrimaryModule.FINANCES, "Finances", Icons.Default.AccountBalance),
            Triple(ErpPrimaryModule.CRM_SALES, "CRM & Ventes", Icons.Default.PointOfSale),
            Triple(ErpPrimaryModule.OPERATIONS, "Stocks", Icons.Default.Inventory2),
            Triple(ErpPrimaryModule.HR_PROJECTS, "RH & Projets", Icons.Default.Badge),
            Triple(ErpPrimaryModule.DOCS_AI_AUTO, "IA & GED", Icons.Default.AutoAwesome),
            Triple(ErpPrimaryModule.ADMIN_SECURITY, "Admin & Git", Icons.Default.AdminPanelSettings)
        )
    }

    // BackHandler returns to DASHBOARD when in any secondary ERP module
    BackHandler(enabled = currentModule != ErpPrimaryModule.DASHBOARD) {
        vm.selectModule(ErpPrimaryModule.DASHBOARD)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerContentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Drawer Brand Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ElectricGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Hexagon, contentDescription = null, tint = ElectricGreen, modifier = Modifier.size(22.dp))
                        }
                        Column {
                            Text("BUSINESS OS", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                            Text("Par Mendel Temgoua • MIT", style = MaterialTheme.typography.labelSmall, color = ElectricGreen)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(activeOrg?.name ?: "TechNova Africa SARL", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text("${activeOrg?.ohadaZone ?: "OHADA"} • Devise : ${activeOrg?.currency ?: "XAF"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

                    Text(
                        text = "MODULES ERP (24 ESPACES)",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    allNavModules.forEach { (mod, shortTitle, icon) ->
                        val selected = currentModule == mod
                        NavigationDrawerItem(
                            label = {
                                Column {
                                    Text(mod.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium))
                                    Text(mod.subtitle, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            },
                            icon = {
                                Icon(icon, contentDescription = shortTitle, tint = if (selected) ElectricGreen else MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            selected = selected,
                            onClick = {
                                vm.selectModule(mod)
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = ElectricGreen.copy(alpha = 0.12f),
                                unselectedContainerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("drawer_nav_${mod.name}")
                        )
                    }
                }
            }
        }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth >= 640.dp

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    Surface(
                        color = MaterialTheme.colorScheme.background,
                        border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(WindowInsets.statusBars)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left: Menu Drawer Button + Minimal Brand Identity
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    IconButton(
                                        onClick = { scope.launch { drawerState.open() } },
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .testTag("open_drawer_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = "Menu Navigation",
                                            tint = ElectricGreen,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }

                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = currentModule.title,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontFamily = SpaceGroteskFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = activeOrg?.name ?: "BUSINESS OS",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                // Right: Command Search (⌘K), Organization Pill, Theme Toggle
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Global Search / Command Palette Button
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                        modifier = Modifier
                                            .clickable { showCommandPalette = true }
                                            .testTag("command_palette_button")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Icon(Icons.Default.Search, contentDescription = "Recherche", tint = ElectricGreen, modifier = Modifier.size(15.dp))
                                            Text("⌘K", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    // Multi-Tenant Switcher Pill
                                    Box {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(0.8.dp, ElectricGreen.copy(alpha = 0.45f)),
                                            modifier = Modifier
                                                .clickable { showOrgSwitcherMenu = true }
                                                .testTag("org_switcher_button")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(ElectricGreen))
                                                Text(
                                                    text = activeOrg?.currency ?: "XAF",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
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

                                    // Dark / Light Theme Button
                                    IconButton(
                                        onClick = { vm.toggleDarkMode() },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .testTag("theme_toggle_button")
                                    ) {
                                        Icon(
                                            imageVector = if (isDark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                                            contentDescription = "Mode Sombre / Clair",
                                            tint = ElectricGreen,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }
                            }

                            // Subtle Toast Notification Banner
                            AnimatedVisibility(visible = toastMessage != null) {
                                toastMessage?.let { msg ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = ElectricGreen.copy(alpha = 0.12f),
                                        border = BorderStroke(0.8.dp, ElectricGreen.copy(alpha = 0.5f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { vm.clearToastBanner() }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ElectricGreen, modifier = Modifier.size(16.dp))
                                                Text(
                                                    text = msg,
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = ElectricGreen, modifier = Modifier.size(15.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                bottomBar = {
                    if (!isWideScreen) {
                        // Clean, Modern Material 3 NavigationBar with 5 Core Pillars + Drawer Trigger for 100% Uncluttered Ergonomics
                        val primaryFive = listOf(
                            Triple(ErpPrimaryModule.DASHBOARD, "Pilotage", Icons.Default.SpaceDashboard),
                            Triple(ErpPrimaryModule.FINANCES, "Finances", Icons.Default.AccountBalance),
                            Triple(ErpPrimaryModule.CRM_SALES, "CRM", Icons.Default.PointOfSale),
                            Triple(ErpPrimaryModule.OPERATIONS, "Stocks", Icons.Default.Inventory2),
                            Triple(ErpPrimaryModule.DOCS_AI_AUTO, "IA & Auto", Icons.Default.AutoAwesome)
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        ) {
                            NavigationBar(
                                containerColor = Color.Transparent,
                                tonalElevation = 0.dp,
                                modifier = Modifier.height(68.dp)
                            ) {
                                primaryFive.forEach { (mod, label, icon) ->
                                    val selected = currentModule == mod
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { vm.selectModule(mod) },
                                        icon = {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = label,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = 10.5.sp
                                                ),
                                                maxLines = 1
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = ObsidianBlack,
                                            selectedTextColor = ElectricGreen,
                                            indicatorColor = ElectricGreen,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.testTag("nav_tab_${mod.name}")
                                    )
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isWideScreen) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            allNavModules.forEach { (mod, label, icon) ->
                                val selected = currentModule == mod
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { vm.selectModule(mod) },
                                    icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp)) },
                                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = ObsidianBlack,
                                        selectedTextColor = ElectricGreen,
                                        indicatorColor = ElectricGreen
                                    ),
                                    modifier = Modifier.testTag("rail_tab_${mod.name}")
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
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
        }
    }

    // Global Command Palette & Quick Search Modal (⌘K)
    if (showCommandPalette) {
        var query by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCommandPalette = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = ElectricGreen)
                    Text("Palette de Commande (⌘K)", style = MaterialTheme.typography.titleMedium)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Rechercher un module, client, facture, compte OHADA...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ElectricGreen) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    val filteredModules = allNavModules.filter {
                        query.isBlank() || it.first.title.contains(query, ignoreCase = true) || it.first.subtitle.contains(query, ignoreCase = true)
                    }

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 260.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredModules) { (mod, _, icon) ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        vm.selectModule(mod)
                                        showCommandPalette = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(icon, contentDescription = null, tint = ElectricGreen, modifier = Modifier.size(18.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(mod.title, style = MaterialTheme.typography.titleSmall)
                                        Text(mod.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    ErpStatusBadge("OUVRIR", ElectricGreen)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCommandPalette = false }) {
                    Text("Fermer")
                }
            }
        )
    }
}
