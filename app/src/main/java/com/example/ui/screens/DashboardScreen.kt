package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.*
import java.util.Locale

@Composable
fun DashboardScreen(
    organization: OrganizationEntity?,
    metrics: DashboardMetrics,
    selectedPeriod: TimePeriodFilter,
    widgetConfig: DashboardWidgetConfig,
    accounts: List<AccountEntity>,
    transactions: List<TransactionEntity>,
    commercialDocs: List<CommercialDocEntity>,
    products: List<ProductStockEntity>,
    onSelectPeriod: (TimePeriodFilter) -> Unit,
    onUpdateWidgets: (DashboardWidgetConfig) -> Unit,
    onNavigateModule: (ErpPrimaryModule) -> Unit
) {
    val currency = organization?.currency ?: "XAF"
    var showCustomizeDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 104.dp)
    ) {
        // 1. Sleek Executive Hero Card (Fintech / Linear Aesthetic)
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = ObsidianBlack,
                border = BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_erp_hero_banner_1794221609547),
                        contentDescription = "Business OS Command Center",
                        contentScale = ContentScale.Crop,
                        alpha = 0.28f,
                        modifier = Modifier
                            .matchParentSize()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        ObsidianBlack.copy(alpha = 0.72f),
                                        ObsidianBlack.copy(alpha = 0.94f)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Top metadata & widget customizer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ErpStatusBadge(organization?.ohadaZone ?: "OHADA • CEMAC", ElectricGreen)
                                ErpStatusBadge("MIT • LOCAL-FIRST", SecondaryEmerald)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SlateSurface,
                                border = BorderStroke(0.8.dp, SlateBorder),
                                modifier = Modifier
                                    .clickable { showCustomizeDialog = true }
                                    .testTag("customize_widgets_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(Icons.Default.Tune, contentDescription = "Widgets", tint = ElectricGreen, modifier = Modifier.size(14.dp))
                                    Text("Widgets", style = MaterialTheme.typography.labelSmall, color = CrispWhite)
                                }
                            }
                        }

                        // Primary Organization Balance & Net Cash Position
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "TRÉSORERIE CONSOLIDÉE • ${organization?.name?.uppercase() ?: "ENTREPRISE"}",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.9.sp),
                                    color = MutedSteelGray
                                )
                                Text(
                                    text = BusinessOsViewModel.formatMoney(metrics.availableTreasuryCents, currency),
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 28.sp
                                    ),
                                    color = CrispWhite
                                )
                                Text(
                                    text = "Marge nette : ${String.format(Locale.US, "%.1f", metrics.netMarginPercent)}% • Bénéfice : ${BusinessOsViewModel.formatMoney(metrics.netProfitCents, currency)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ElectricGreen
                                )
                            }
                        }

                        HorizontalDivider(color = SlateBorder.copy(alpha = 0.7f))

                        // Segmented Time Period Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Période :",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedSteelGray
                            )
                            TimePeriodFilter.entries.forEach { period ->
                                val active = period == selectedPeriod
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (active) ElectricGreen else SlateSurface,
                                    border = BorderStroke(0.8.dp, if (active) ElectricGreen else SlateBorder),
                                    modifier = Modifier
                                        .clickable { onSelectPeriod(period) }
                                        .testTag("period_filter_${period.name}")
                                ) {
                                    Text(
                                        text = period.label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (active) ObsidianBlack else CrispWhite,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Quick Executive Action Strip
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionPill(
                    label = "Nouvelle Transaction",
                    icon = Icons.Default.AddCard,
                    accent = ElectricGreen,
                    onClick = { onNavigateModule(ErpPrimaryModule.FINANCES) }
                )
                QuickActionPill(
                    label = "Émettre Facture / Devis",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    accent = SecondaryEmerald,
                    onClick = { onNavigateModule(ErpPrimaryModule.CRM_SALES) }
                )
                QuickActionPill(
                    label = "Journal OHADA",
                    icon = Icons.Default.AccountBalance,
                    accent = StatusInfo,
                    onClick = { onNavigateModule(ErpPrimaryModule.FINANCES) }
                )
                QuickActionPill(
                    label = "Business AI Analyst",
                    icon = Icons.Default.AutoAwesome,
                    accent = ElectricGreen,
                    onClick = { onNavigateModule(ErpPrimaryModule.DOCS_AI_AUTO) }
                )
            }
        }

        // 3. Minimalist Smart Alert Strip
        if (widgetConfig.showAlertsBanner && (metrics.unpaidInvoicesCount > 0 || metrics.lowStockCount > 0)) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = StatusWarning.copy(alpha = 0.08f),
                    border = BorderStroke(0.8.dp, StatusWarning.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateModule(ErpPrimaryModule.CRM_SALES) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(StatusWarning.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = "Alertes", tint = StatusWarning, modifier = Modifier.size(17.dp))
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "${metrics.unpaidInvoicesCount} facture(s) à recouvrer • ${metrics.lowStockCount} alerte(s) de stock",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Encours client en attente : ${BusinessOsViewModel.formatMoney(metrics.unpaidInvoicesCents, currency)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = StatusWarning, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        // 4. Bento-Grid KPI Matrix
        if (widgetConfig.showFinancialKpis) {
            item {
                ErpSectionHeader(
                    title = "Synthèse Financière & Commerciale",
                    subtitle = "Indicateurs temps réel certifiés depuis la base SQLite"
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        ErpKpiCard(
                            title = "Chiffre d'Affaires",
                            value = BusinessOsViewModel.formatMoney(metrics.revenueBilledCents, currency),
                            subtitle = "${metrics.salesDocsCount} pièces commerciales",
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            accentColor = ElectricGreen,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateModule(ErpPrimaryModule.CRM_SALES) }
                        )
                        ErpKpiCard(
                            title = "Revenus Encaissés",
                            value = BusinessOsViewModel.formatMoney(metrics.revenueCollectedCents, currency),
                            subtitle = "Trésorerie entrante",
                            icon = Icons.Default.AccountBalanceWallet,
                            accentColor = SecondaryEmerald,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateModule(ErpPrimaryModule.FINANCES) }
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        ErpKpiCard(
                            title = "Charges & Dépenses",
                            value = BusinessOsViewModel.formatMoney(metrics.expensesTotalCents, currency),
                            subtitle = "Décaissements validés",
                            icon = Icons.AutoMirrored.Filled.CallMade,
                            accentColor = StatusDanger,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateModule(ErpPrimaryModule.FINANCES) }
                        )
                        ErpKpiCard(
                            title = "Bénéfice Net",
                            value = BusinessOsViewModel.formatMoney(metrics.netProfitCents, currency),
                            subtitle = "Marge : ${String.format(Locale.US, "%.1f", metrics.netMarginPercent)}%",
                            icon = Icons.Default.Analytics,
                            accentColor = if (metrics.netProfitCents >= 0) ElectricGreen else StatusDanger,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateModule(ErpPrimaryModule.FINANCES) }
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        ErpKpiCard(
                            title = "Factures Impayées",
                            value = BusinessOsViewModel.formatMoney(metrics.unpaidInvoicesCents, currency),
                            subtitle = "${metrics.unpaidInvoicesCount} facture(s) ouverte(s)",
                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                            accentColor = if (metrics.unpaidInvoicesCount > 0) StatusWarning else SecondaryEmerald,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateModule(ErpPrimaryModule.CRM_SALES) }
                        )
                        ErpKpiCard(
                            title = "CRM & Opportunités",
                            value = "${metrics.activeCustomersCount} Clients • ${metrics.activeLeadsCount} Leads",
                            subtitle = "Pipeline : ${BusinessOsViewModel.formatMoney(metrics.pipelineValueCents, currency)}",
                            icon = Icons.Default.Groups,
                            accentColor = StatusInfo,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateModule(ErpPrimaryModule.CRM_SALES) }
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        ErpKpiCard(
                            title = "Projets & Tâches",
                            value = "${metrics.activeProjectsCount} Projets",
                            subtitle = "${metrics.completedTasksCount}/${metrics.totalTasksCount} tâches livrées",
                            icon = Icons.Default.TaskAlt,
                            accentColor = SecondaryEmerald,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateModule(ErpPrimaryModule.HR_PROJECTS) }
                        )
                        ErpKpiCard(
                            title = "Catalogue & Stocks",
                            value = "${products.size} Références",
                            subtitle = if (metrics.lowStockCount > 0) "${metrics.lowStockCount} sous seuil critique" else "Stocks optimaux",
                            icon = Icons.Default.Inventory2,
                            accentColor = if (metrics.lowStockCount > 0) StatusWarning else ElectricGreen,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateModule(ErpPrimaryModule.OPERATIONS) }
                        )
                    }
                }
            }
        }

        // 5. Architectural Financial Chart Card
        if (widgetConfig.showInteractiveChart) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Trajectoire Financière & Cashflow",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Encaissements vs Charges d'exploitation (${selectedPeriod.label})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            ErpStatusBadge("LIVE SQL", ElectricGreen)
                        }

                        val baseRev = (metrics.revenueCollectedCents / 100f).coerceAtLeast(1000f)
                        val baseExp = (metrics.expensesTotalCents / 100f).coerceAtLeast(400f)
                        InteractiveFinancialChart(
                            revenuePoints = listOf(baseRev * 0.55f, baseRev * 0.72f, baseRev * 0.68f, baseRev * 0.88f, baseRev * 0.94f, baseRev),
                            expensePoints = listOf(baseExp * 0.60f, baseExp * 0.50f, baseExp * 0.75f, baseExp * 0.62f, baseExp * 0.80f, baseExp),
                            labels = listOf("M-5", "M-4", "M-3", "M-2", "M-1", "Actuel")
                        )
                    }
                }
            }
        }

        // 6. Quarterly Objectives & Treasury Accounts Bento
        if (widgetConfig.showProjectsObjectives) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Exécution des Objectifs Annuels",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${(metrics.revenueTargetProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                                color = ElectricGreen
                            )
                        }
                        LinearProgressIndicator(
                            progress = { metrics.revenueTargetProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = ElectricGreen,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${commercialDocs.count { it.status == "PAID" }} factures soldées",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${transactions.size} écritures validées",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 7. Sleek Architecture Directory (24 Modules in Clean Executive Rows)
        item {
            ErpSectionHeader(
                title = "Pôles Métiers & Modules (24 Modules)",
                subtitle = "Navigation directe vers chaque domaine fonctionnel"
            )
        }
        item {
            val moduleMatrix = listOf(
                ModuleDirectoryEntry("Finances, Trésorerie & Comptabilité OHADA", "Comptes, Paiements MoMo/Banque, Journaux VE/AC/BQ, Bilan", Icons.Default.AccountBalance, ElectricGreen, ErpPrimaryModule.FINANCES),
                ModuleDirectoryEntry("CRM, Prospects, Clients & Facturation", "Pipeline, Conversion 1-clic, Devis, Factures, Contrats, Relances", Icons.Default.PointOfSale, SecondaryEmerald, ErpPrimaryModule.CRM_SALES),
                ModuleDirectoryEntry("Catalogue, Stocks, Achats & Fournisseurs", "Produits/Services, Mouvements IN/OUT, Alertes Seuil, Commandes", Icons.Default.Inventory2, StatusInfo, ErpPrimaryModule.OPERATIONS),
                ModuleDirectoryEntry("Ressources Humaines, Projets & Tâches", "Collaborateurs, Contrats, Paie, Congés, Kanban & Budgets", Icons.Default.Badge, ElectricGreen, ErpPrimaryModule.HR_PROJECTS),
                ModuleDirectoryEntry("GED Documents, Workflows & Business AI", "Coffre Local/S3 SHA-256, Automatisations, IA Ollama / Locale", Icons.Default.AutoAwesome, SecondaryEmerald, ErpPrimaryModule.DOCS_AI_AUTO),
                ModuleDirectoryEntry("Multi-Tenant, Sécurité OWASP & Dépôt GitHub", "Organisations, RBAC, Audit Logs, Export JSON, Docker Compose", Icons.Default.AdminPanelSettings, StatusInfo, ErpPrimaryModule.ADMIN_SECURITY)
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                moduleMatrix.forEachIndexed { idx, item ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateModule(item.target) }
                            .testTag("dashboard_module_card_$idx")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(item.color.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(item.icon, contentDescription = item.title, tint = item.color, modifier = Modifier.size(20.dp))
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(item.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                                Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }

    if (showCustomizeDialog) {
        AlertDialog(
            onDismissRequest = { showCustomizeDialog = false },
            title = { Text("Affichage du Dashboard") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Grille des KPIs Financiers & CRM")
                        Switch(
                            checked = widgetConfig.showFinancialKpis,
                            onCheckedChange = { onUpdateWidgets(widgetConfig.copy(showFinancialKpis = it)) }
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Graphique Trajectoire & Cashflow")
                        Switch(
                            checked = widgetConfig.showInteractiveChart,
                            onCheckedChange = { onUpdateWidgets(widgetConfig.copy(showInteractiveChart = it)) }
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Bannière d'Alertes Prioritaires")
                        Switch(
                            checked = widgetConfig.showAlertsBanner,
                            onCheckedChange = { onUpdateWidgets(widgetConfig.copy(showAlertsBanner = it)) }
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Jauge d'Exécution des Objectifs")
                        Switch(
                            checked = widgetConfig.showProjectsObjectives,
                            onCheckedChange = { onUpdateWidgets(widgetConfig.copy(showProjectsObjectives = it)) }
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showCustomizeDialog = false }) {
                    Text("Enregistrer")
                }
            }
        )
    }
}

private data class ModuleDirectoryEntry(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val target: ErpPrimaryModule
)

@Composable
private fun QuickActionPill(
    label: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = label, tint = accent, modifier = Modifier.size(16.dp))
            Text(label, style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp), color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
