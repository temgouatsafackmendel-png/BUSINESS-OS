package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.*
import com.example.ui.components.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        "Dépôt GitHub (Mendel Temgoua)",
        "Multi-Tenant & Organisations",
        "RBAC, Sécurité & Audit Logs",
        "Export JSON & Self-Hosting"
    )

    var showNewOrgDialog by remember { mutableStateOf(false) }
    var showNewWsDialog by remember { mutableStateOf(false) }
    var showNewMemberDialog by remember { mutableStateOf(false) }
    var selectedRepoFileIndex by remember { mutableIntStateOf(0) }
    var copiedNotice by remember { mutableStateOf<String?>(null) }

    val clipboardManager = LocalClipboardManager.current
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.FRANCE) }

    val githubRepoFiles = remember {
        listOf(
            Triple(
                "Commandes Git & Publication GitHub",
                "git_publish_commands.sh",
                """
                # Configuration de l'auteur principal : Mendel Temgoua
                git init
                git config user.name "Mendel Temgoua"
                git config user.email "mendeltemgouabusiness@gmail.com"
                
                # Ajout de l'ensemble de l'architecture BUSINESS OS 2026
                git add .
                git commit -m "feat(erp): initial release of BUSINESS OS 2026 (MIT, Multi-Tenant, OHADA, Pluggable AI) by Mendel Temgoua"
                
                # Publication sur votre dépôt GitHub
                git branch -M main
                git remote add origin https://github.com/mendeltemgoua/business-os.git
                git push -u origin main
                """.trimIndent()
            ),
            Triple(
                "Arborescence Complète du Dépôt GitHub",
                "TREE_STRUCTURE.txt",
                """
                business-os/ (Auteur: Mendel Temgoua • Licence MIT)
                ├── README.md                        # Documentation officielle & badges
                ├── LICENSE                          # MIT License (c) 2026 Mendel Temgoua
                ├── SECURITY.md                      # Politique OWASP & Audit Licences
                ├── docker-compose.yml               # PostgreSQL 16 + Redis 7 + Caddy HTTPS + Ollama
                ├── .env.example                     # Configuration serveur & IA optionnelle
                ├── docs/ci-workflow.yml         # Pipeline CI/CD GitHub Actions (Tests & APK)
                ├── prisma/schema.prisma             # Modèle relationnel PostgreSQL Multi-Tenant
                ├── scripts/
                │   ├── backup.sh                    # Sauvegarde automatisée pg_dump + SHA-256
                │   └── restore.sh                   # Restauration vérifiée avec contrôle d'intégrité
                ├── docs/
                │   └── INSTALLATION_GUIDE.md        # Guides Linux, Windows, macOS, Serveur & HTTPS
                └── app/src/main/java/com/example/   # Code source Kotlin & Jetpack Compose M3
                    ├── MainActivity.kt              # Navigation centrale 24 modules & Multi-Tenant
                    ├── data/ai/AiProviderEngine.kt  # Interface AIProvider (Off, Local, Ollama, OpenAI)
                    ├── data/local/Entities.kt       # 15 tables relationnelles isolées par orgId
                    ├── data/local/BusinessOsDao.kt  # DAO réactif Flow & Transactions comptables
                    ├── ui/screens/                  # Dashboard, Finances, OHADA, CRM, Stocks, RH, IA
                    └── ui/viewmodel/                # BusinessOsViewModel & Export JSON souverain
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
            ),
            Triple(
                "Scripts Sauvegarde & Restauration SHA-256",
                "scripts/backup.sh & restore.sh",
                """
                #!/usr/bin/env bash
                # Sauvegarde Souveraine par Mendel Temgoua
                BACKUP_DIR="./backups"
                TIMESTAMP="${'$'}(date -u +"%Y%m%d_%H%M%S")"
                mkdir -p "${'$'}BACKUP_DIR"
                docker exec -t postgres pg_dump -U businessos business_os_db | gzip -9 > "${'$'}BACKUP_DIR/pg_${'$'}TIMESTAMP.sql.gz"
                sha256sum "${'$'}BACKUP_DIR/pg_${'$'}TIMESTAMP.sql.gz" > "${'$'}BACKUP_DIR/pg_${'$'}TIMESTAMP.sql.gz.sha256"
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
        ) {
            when (selectedSubTab) {
                // SUBTAB 0: GITHUB REPOSITORY & AUTHORSHIP BY MENDEL TEMGOUA
                0 -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.5.dp, ElectricGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        ErpStatusBadge("AUTEUR : MENDEL TEMGOUA", ElectricGreen)
                                        ErpStatusBadge("MIT OPEN SOURCE", SecondaryEmerald)
                                    }
                                    Icon(Icons.Default.Verified, contentDescription = "Certifié", tint = ElectricGreen)
                                }

                                Text(
                                    text = "Dépôt GitHub Officiel — BUSINESS OS 2026",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Architecte & Développeur Principal : Mendel Temgoua (mendeltemgouabusiness@gmail.com)\n" +
                                        "Tous les fichiers du dépôt GitHub (README.md, LICENSE MIT, prisma/schema.prisma, docker-compose.yml, .github/workflows/ci.yml, scripts/backup.sh, scripts/restore.sh et docs/INSTALLATION_GUIDE.md) sont générés et prêts à être poussés ou exportés en ZIP.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                copiedNotice?.let { notice ->
                                    ErpStatusBadge(notice, ElectricGreen)
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
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
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
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
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
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
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
