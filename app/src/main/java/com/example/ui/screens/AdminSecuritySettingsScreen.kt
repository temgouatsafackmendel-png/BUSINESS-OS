package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.*
import com.example.ui.components.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class ScreenshotShowcaseItem(
    val title: String,
    val subtitle: String,
    val gitPath: String,
    val drawableRes: Int
)

@Composable
fun AdminSecuritySettingsScreen(
    organizations: List<OrganizationEntity>,
    activeOrganization: OrganizationEntity?,
    workspaces: List<WorkspaceEntity>,
    memberships: List<UserRoleMemberEntity>,
    auditLogs: List<AuditLogEntity>,
    exportedJsonPreview: String?,
    onSwitchOrganization: (Long) -> Unit,
    onCreateOrganization: (String, String, String, String, String, Double) -> Unit,
    onCreateWorkspace: (String, String, String) -> Unit,
    onAddMember: (String, String, String, String, Boolean) -> Unit,
    onExportFullJson: () -> Unit,
    onDismissExportPreview: () -> Unit
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf(
        "APK & Captures GitHub",
        "Multi-Tenant & Organisations",
        "RBAC, Sécurité & Audit Logs",
        "Export JSON & Self-Hosting"
    )

    var showNewOrgDialog by remember { mutableStateOf(false) }
    var showNewWsDialog by remember { mutableStateOf(false) }
    var showNewMemberDialog by remember { mutableStateOf(false) }
    var selectedRepoFileIndex by remember { mutableIntStateOf(0) }
    var copiedNotice by remember { mutableStateOf<String?>(null) }
    var zoomedScreenshot by remember { mutableStateOf<ScreenshotShowcaseItem?>(null) }

    val clipboardManager = LocalClipboardManager.current
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.FRANCE) }

    val screenshots = remember {
        listOf(
            ScreenshotShowcaseItem(
                title = "1. Dashboard Exécutif & Cashflow",
                subtitle = "Trésorerie consolidée, KPIs Bento & Graphique temps réel",
                gitPath = "docs/screenshots/01_dashboard_executive.jpg",
                drawableRes = R.drawable.img_preview_dashboard_1794224788236
            ),
            ScreenshotShowcaseItem(
                title = "2. Comptabilité OHADA & Mobile Money",
                subtitle = "Journaux SYSCOHADA équilibrés, Banque 5211 & MoMo 5521",
                gitPath = "docs/screenshots/02_ohada_accounting_treasury.jpg",
                drawableRes = R.drawable.img_preview_ohada_1794224789766
            ),
            ScreenshotShowcaseItem(
                title = "3. Pipeline CRM & Business AI (Ollama/MIT)",
                subtitle = "Conversion Prospect->Client 1-clic & IA locale sécurisée",
                gitPath = "docs/screenshots/03_crm_pipeline_business_ai.jpg",
                drawableRes = R.drawable.img_preview_crm_ai_1794224791024
            )
        )
    }

    val githubRepoFiles = remember {
        listOf(
            Triple(
                "Commandes Git (APK + Captures incluses)",
                "git_publish_with_apk.sh",
                """
                # Configuration de l'auteur : Mendel Temgoua
                git init
                git config user.name "Mendel Temgoua"
                git config user.email "mendeltemgouabusiness@gmail.com"
                
                # Ajout du code source, de l'APK prêt à l'emploi (releases/) et des captures (docs/screenshots/)
                git add .
                git commit -m "feat(release): BUSINESS OS 2026 v2026.1.0-MIT with ready-to-use APK and UI screenshots by Mendel Temgoua"
                
                # Envoi sur GitHub
                git branch -M main
                git remote add origin https://github.com/mendeltemgoua/business-os.git
                git push -u origin main
                """.trimIndent()
            ),
            Triple(
                "Structure du Dépôt GitHub (avec APK & Captures)",
                "TREE_STRUCTURE.txt",
                """
                business-os/ (Auteur: Mendel Temgoua • Licence MIT)
                ├── README.md                                            # Vitrine GitHub avec bouton Télécharger APK & Galerie Captures
                ├── LICENSE                                              # MIT License (c) 2026 Mendel Temgoua
                ├── releases/
                │   ├── business-os-2026.1.0-MIT-universal.apk           # APK Android compilé prêt à installer !
                │   ├── business-os-2026.1.0-MIT-universal.apk.sha256    # Empreinte d'intégrité SHA-256 de l'APK
                │   └── RELEASE_NOTES.md                                 # Notes de version officielles v2026.1.0-MIT
                ├── docs/
                │   ├── screenshots/
                │   │   ├── 01_dashboard_executive.jpg                   # Capture 1 : Dashboard Exécutif
                │   │   ├── 02_ohada_accounting_treasury.jpg             # Capture 2 : Comptabilité OHADA & Trésorerie
                │   │   ├── 03_crm_pipeline_business_ai.jpg              # Capture 3 : Pipeline CRM & Business AI
                │   │   └── 04_hero_command_center.jpg                   # Bannière Architecture Business OS
                │   ├── INSTALLATION_GUIDE.md                            # Guides Linux, Windows, macOS, Android & HTTPS
                │   └── ci-workflow.yml                                  # Pipeline CI/CD
                ├── prisma/schema.prisma                                 # Schéma PostgreSQL 16 Multi-Tenant (36 tables)
                ├── scripts/backup.sh & restore.sh                       # Sauvegarde & Restauration chiffrée
                └── app/src/main/java/com/example/                       # Code source Kotlin & Jetpack Compose M3
                """.trimIndent()
            ),
            Triple(
                "Stack Docker Compose Auto-Hébergée",
                "docker-compose.yml",
                """
                version: "3.9"
                # BUSINESS OS 2026 — Créé par Mendel Temgoua (MIT License)
                services:
                  caddy:
                    image: caddy:2.8-alpine
                    restart: unless-stopped
                    ports: ["80:80", "443:443"]
                  postgres:
                    image: postgres:16-alpine
                    restart: unless-stopped
                    environment:
                      POSTGRES_USER: businessos
                      POSTGRES_DB: business_os_db
                    ports: ["5432:5432"]
                  redis:
                    image: redis:7.4-alpine
                    restart: unless-stopped
                  ollama:
                    image: ollama/ollama:latest
                    profiles: ["ai-local"]
                    ports: ["11434:11434"]
                """.trimIndent()
            )
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        ErpSubTabRow(
            tabs = subTabs,
            selectedIndex = selectedSubTab,
            onSelect = { selectedSubTab = it },
            tagPrefix = "admin_subtab"
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 104.dp)
        ) {
            when (selectedSubTab) {
                // SUBTAB 0: READY-TO-USE APK RELEASE & SCREENSHOTS GALLERY IN GITHUB
                0 -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.2.dp, ElectricGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        ErpStatusBadge("APK INCLUS DANS GITHUB", ElectricGreen)
                                        ErpStatusBadge("v2026.1.0-MIT", SecondaryEmerald)
                                    }
                                    Icon(Icons.Default.Android, contentDescription = "APK", tint = ElectricGreen)
                                }

                                Text(
                                    text = "Release APK Prête à l'Emploi — par Mendel Temgoua",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Fichier inclus dans le dépôt : releases/business-os-2026.1.0-MIT-universal.apk\n" +
                                        "Signé et vérifié avec somme de contrôle SHA-256 + Galerie de captures d'écran intégrée dans docs/screenshots/ et affichée sur la page d'accueil du README GitHub.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = ObsidianBlack,
                                    border = BorderStroke(0.8.dp, SlateBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("releases/business-os-2026.1.0-MIT-universal.apk", style = MaterialTheme.typography.labelMedium, color = ElectricGreen)
                                            Text("Android 7.0+ (API 24–36) • Architecture Universelle • 100% Autonome", style = MaterialTheme.typography.bodySmall, color = MutedSteelGray)
                                        }
                                        Button(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString("releases/business-os-2026.1.0-MIT-universal.apk"))
                                                copiedNotice = "Chemin APK copié : releases/business-os-2026.1.0-MIT-universal.apk"
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = ElectricGreen, contentColor = ObsidianBlack),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("Copier Lien", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                copiedNotice?.let { notice ->
                                    ErpStatusBadge(notice, ElectricGreen)
                                }
                            }
                        }
                    }

                    // Interactive Gallery of Official GitHub Screenshots
                    item {
                        ErpSectionHeader(
                            title = "Galerie des Captures d'Écran GitHub (docs/screenshots/)",
                            subtitle = "Touchez une capture pour l'agrandir en plein écran"
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            screenshots.forEachIndexed { index, shot ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
                                    modifier = Modifier
                                        .width(210.dp)
                                        .clickable { zoomedScreenshot = shot }
                                        .testTag("screenshot_card_$index")
                                ) {
                                    Column {
                                        Image(
                                            painter = painterResource(id = shot.drawableRes),
                                            contentDescription = shot.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(310.dp)
                                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                        )
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(shot.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), maxLines = 1)
                                            Text(shot.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                                            Text(shot.gitPath, style = MaterialTheme.typography.labelSmall, color = ElectricGreen)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            githubRepoFiles.forEachIndexed { idx, (_, shortName, _) ->
                                FilterChip(
                                    selected = selectedRepoFileIndex == idx,
                                    onClick = { selectedRepoFileIndex = idx },
                                    label = { Text(shortName.substringBefore(" "), style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }

                    item {
                        val (title, fileName, codeContent) = githubRepoFiles[selectedRepoFileIndex]
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = ObsidianBlack,
                            border = BorderStroke(1.dp, SlateBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(title, style = MaterialTheme.typography.titleMedium, color = CrispWhite)
                                        Text(fileName, style = MaterialTheme.typography.labelSmall, color = ElectricGreen)
                                    }
                                    Button(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(codeContent))
                                            copiedNotice = "Copié dans le presse-papiers : $fileName"
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ElectricGreen, contentColor = ObsidianBlack),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("copy_github_snippet_button")
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Copier", fontWeight = FontWeight.Bold)
                                    }
                                }
                                HorizontalDivider(color = SlateBorder)
                                Text(
                                    text = codeContent,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ElectricGreen
                                )
                            }
                        }
                    }
                }

                // SUBTAB 1: MULTI-TENANT ORGANIZATIONS & WORKSPACES
                1 -> {
                    item {
                        ErpSectionHeader(
                            title = "Architecture Multi-Tenant & Isolation des Données",
                            subtitle = "Basculez entre plusieurs sociétés avec isolation stricte par organizationId",
                            actionLabel = "+ Organisation",
                            onActionClick = { showNewOrgDialog = true }
                        )
                    }

                    items(organizations, key = { it.id }) { org ->
                        val isCurrent = org.id == activeOrganization?.id
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.5.dp,
                                if (isCurrent) ElectricGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        ErpStatusBadge(org.legalForm, ElectricGreen)
                                        ErpStatusBadge(org.ohadaZone, SecondaryEmerald)
                                        ErpStatusBadge(org.currency, StatusInfo)
                                    }
                                    Text(org.name, style = MaterialTheme.typography.titleLarge)
                                    Text("ID Fiscal: ${org.taxId} • TVA: ${org.defaultVatPercent}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (isCurrent) {
                                    ErpStatusBadge("ACTIVE", ElectricGreen)
                                } else {
                                    Button(
                                        onClick = { onSwitchOrganization(org.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("switch_org_${org.id}")
                                    ) {
                                        Text("Activer")
                                    }
                                }
                            }
                        }
                    }

                    item {
                        ErpSectionHeader(
                            title = "Espaces de Travail (Workspaces) de ${activeOrganization?.name ?: ""}",
                            subtitle = "Cloisonnement opérationnel par direction ou filiale",
                            actionLabel = "+ Workspace",
                            onActionClick = { showNewWsDialog = true }
                        )
                    }

                    items(workspaces, key = { it.id }) { ws ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(ws.name, style = MaterialTheme.typography.titleMedium)
                                    Text("Code: ${ws.code} • Département: ${ws.department}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (ws.isDefault) {
                                    ErpStatusBadge("SIÈGE PAR DÉFAUT", ElectricGreen)
                                }
                            }
                        }
                    }
                }

                // SUBTAB 2: RBAC, SECURITY & AUDIT LOGS
                2 -> {
                    item {
                        ErpSectionHeader(
                            title = "Utilisateurs, Rôles RBAC & MFA",
                            subtitle = "Contrôle d'accès granulaire et authentification multifacteur",
                            actionLabel = "+ Rôle / Membre",
                            onActionClick = { showNewMemberDialog = true }
                        )
                    }

                    items(memberships, key = { it.id }) { member ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(member.fullName, style = MaterialTheme.typography.titleMedium)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        ErpStatusBadge(member.roleName, ElectricGreen)
                                        if (member.mfaEnabled) {
                                            ErpStatusBadge("MFA 2FA ACTIF", SecondaryEmerald)
                                        }
                                    }
                                }
                                Text(member.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Permissions : ${member.permissionsCsv}", style = MaterialTheme.typography.labelSmall, color = StatusInfo)
                            }
                        }
                    }

                    item {
                        ErpSectionHeader(
                            title = "Journal d'Audit Immuable (OWASP & Traçabilité)",
                            subtitle = "Enregistrement horodaté de toutes les opérations sensibles"
                        )
                    }

                    items(auditLogs, key = { it.id }) { log ->
                        val sevColor = when (log.severity) {
                            "WARNING" -> StatusWarning
                            "SECURITY_CRITICAL" -> StatusDanger
                            else -> ElectricGreen
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        ErpStatusBadge(log.action, sevColor)
                                        ErpStatusBadge(log.module, StatusInfo)
                                    }
                                    Text(dateFormatter.format(Date(log.timestamp)), style = MaterialTheme.typography.labelSmall, color = MutedSteelGray)
                                }
                                Text(log.details, style = MaterialTheme.typography.bodySmall)
                                Text("Acteur : ${log.actorName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                // SUBTAB 3: SOVEREIGN DATA EXPORT, DOCKER COMPOSE & MIT LICENSE
                3 -> {
                    item {
                        ErpSectionHeader(
                            title = "Souveraineté des Données, Export Intégral & Self-Hosting",
                            subtitle = "Licence MIT (Mendel Temgoua) • Export JSON/CSV/SQLite"
                        )
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, ElectricGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = ElectricGreen)
                                    Text("Exportation Intégrale des Données (Zéro Lock-In)", style = MaterialTheme.typography.titleMedium)
                                }
                                Text(
                                    "Exportez à tout moment 100% des tables de l'organisation active (Comptes, Transactions, Journal OHADA, Clients, Devis/Factures, Stocks, RH, Audit Logs) au format ouvert JSON.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = onExportFullJson,
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricGreen, contentColor = ObsidianBlack),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("export_full_json_button")
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Générer l'Export Intégral JSON de l'Organisation", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Full-Screen Screenshot Preview Modal
    zoomedScreenshot?.let { shot ->
        AlertDialog(
            onDismissRequest = { zoomedScreenshot = null },
            title = { Text(shot.title, style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Image(
                        painter = painterResource(id = shot.drawableRes),
                        contentDescription = shot.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Text(shot.subtitle, style = MaterialTheme.typography.bodySmall)
                    Text("Fichier GitHub : ${shot.gitPath}", style = MaterialTheme.typography.labelSmall, color = ElectricGreen)
                }
            },
            confirmButton = {
                Button(onClick = { zoomedScreenshot = null }) { Text("Fermer") }
            }
        )
    }

    // Dialog: Exported JSON Preview
    exportedJsonPreview?.let { jsonStr ->
        AlertDialog(
            onDismissRequest = onDismissExportPreview,
            title = { Text("Export Intégral JSON (Souveraineté MIT)") },
            text = {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ObsidianBlack,
                    border = BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 340.dp)
                ) {
                    LazyColumn(modifier = Modifier.padding(10.dp)) {
                        item {
                            Text(
                                text = jsonStr,
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricGreen
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = onDismissExportPreview) { Text("Fermer") }
            }
        )
    }

    // Dialog: Create New Multi-Tenant Organization
    if (showNewOrgDialog) {
        var name by remember { mutableStateOf("") }
        var legalForm by remember { mutableStateOf("SARL") }
        var taxId by remember { mutableStateOf("RCCM-2026-B-901") }
        var zone by remember { mutableStateOf("OHADA - CEMAC") }
        var currency by remember { mutableStateOf("XAF") }
        var vatStr by remember { mutableStateOf("19.25") }

        AlertDialog(
            onDismissRequest = { showNewOrgDialog = false },
            title = { Text("Nouvelle Organisation Multi-Tenant") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom de l'Entreprise") }, singleLine = true)
                    OutlinedTextField(value = legalForm, onValueChange = { legalForm = it }, label = { Text("Forme Juridique (SARL, SAS, SA)") }, singleLine = true)
                    OutlinedTextField(value = taxId, onValueChange = { taxId = it }, label = { Text("NIU / RCCM / SIRET") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("XAF", "XOF", "EUR", "USD").forEach { cur ->
                            FilterChip(selected = currency == cur, onClick = { currency = cur }, label = { Text(cur) })
                        }
                    }
                    OutlinedTextField(value = vatStr, onValueChange = { vatStr = it }, label = { Text("Taux TVA par défaut (%)") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank()) {
                        onCreateOrganization(name, legalForm, taxId, zone, currency, vatStr.toDoubleOrNull() ?: 19.25)
                        showNewOrgDialog = false
                    }
                }) { Text("Créer & Isoler") }
            },
            dismissButton = {
                TextButton(onClick = { showNewOrgDialog = false }) { Text("Annuler") }
            }
        )
    }

    // Dialog: Create Workspace
    if (showNewWsDialog) {
        var name by remember { mutableStateOf("") }
        var code by remember { mutableStateOf("WS-02") }
        var dept by remember { mutableStateOf("Logistique & Stocks") }

        AlertDialog(
            onDismissRequest = { showNewWsDialog = false },
            title = { Text("Nouvel Espace de Travail") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom du Workspace") }, singleLine = true)
                    OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Code Court") }, singleLine = true)
                    OutlinedTextField(value = dept, onValueChange = { dept = it }, label = { Text("Département") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank()) {
                        onCreateWorkspace(name, code, dept)
                        showNewWsDialog = false
                    }
                }) { Text("Créer") }
            },
            dismissButton = {
                TextButton(onClick = { showNewWsDialog = false }) { Text("Annuler") }
            }
        )
    }

    // Dialog: Add RBAC Member
    if (showNewMemberDialog) {
        var fullName by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var role by remember { mutableStateOf("Auditeur & Comptable OHADA") }
        var perms by remember { mutableStateOf("FINANCE_READ,ACCOUNTING_WRITE,EXPORT_ALL") }
        var mfa by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showNewMemberDialog = false },
            title = { Text("Ajouter un Membre RBAC") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Nom Complet") }, singleLine = true)
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true)
                    OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Rôle RBAC") }, singleLine = true)
                    OutlinedTextField(value = perms, onValueChange = { perms = it }, label = { Text("Permissions CSV") }, singleLine = true)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("MFA Obligatoire")
                        Switch(checked = mfa, onCheckedChange = { mfa = it })
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (fullName.isNotBlank() && email.isNotBlank()) {
                        onAddMember(fullName, email, role, perms, mfa)
                        showNewMemberDialog = false
                    }
                }) { Text("Ajouter") }
            },
            dismissButton = {
                TextButton(onClick = { showNewMemberDialog = false }) { Text("Annuler") }
            }
        )
    }
}
