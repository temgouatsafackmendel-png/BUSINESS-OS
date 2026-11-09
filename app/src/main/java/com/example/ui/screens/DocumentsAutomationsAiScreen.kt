package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ai.AiAssistantMessage
import com.example.data.ai.AiConfigState
import com.example.data.ai.AiProviderType
import com.example.data.local.AutomationWorkflowEntity
import com.example.data.local.DocumentVaultEntity
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun DocumentsAutomationsAiScreen(
    documents: List<DocumentVaultEntity>,
    workflows: List<AutomationWorkflowEntity>,
    aiConfig: AiConfigState,
    aiMessages: List<AiAssistantMessage>,
    isAiThinking: Boolean,
    onAddDocument: (String, String, String, String, String, String) -> Unit,
    onArchiveDocument: (DocumentVaultEntity) -> Unit,
    onAddWorkflow: (String, String, String, String, Boolean) -> Unit,
    onToggleWorkflow: (AutomationWorkflowEntity) -> Unit,
    onExecuteWorkflow: (AutomationWorkflowEntity, Boolean) -> Unit,
    onUpdateAiConfig: (Boolean, AiProviderType, String, String, String, Int) -> Unit,
    onSendAiPrompt: (String) -> Unit
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf(
        "Business AI (Interchangeable)",
        "Automatisations & Workflows",
        "GED & Coffre Documentaire"
    )

    var showAiConfigDialog by remember { mutableStateOf(false) }
    var showAddWorkflowDialog by remember { mutableStateOf(false) }
    var showAddDocDialog by remember { mutableStateOf(false) }
    var workflowPendingApproval by remember { mutableStateOf<AutomationWorkflowEntity?>(null) }
    var docPreviewTarget by remember { mutableStateOf<DocumentVaultEntity?>(null) }
    var promptInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        ErpSubTabRow(
            tabs = subTabs,
            selectedIndex = selectedSubTab,
            onSelect = { selectedSubTab = it },
            tagPrefix = "docs_ai_subtab"
        )

        when (selectedSubTab) {
            // SUBTAB 0: PLUGGABLE & OPTIONAL BUSINESS AI
            0 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
                ) {
                    item {
                        ErpSectionHeader(
                            title = "Business AI (Facultative & Interchangeable)",
                            subtitle = "Interface AIProvider ouverte : Moteur Local MIT • Ollama • OpenAI-Compatible • Désactivable",
                            actionLabel = "Configurer IA",
                            onActionClick = { showAiConfigDialog = true }
                        )
                    }

                    // Status & Guardrail Card
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                if (aiConfig.isEnabled && aiConfig.providerType != AiProviderType.DISABLED) ElectricGreen else MutedSteelGray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        ErpStatusBadge(
                                            if (aiConfig.isEnabled && aiConfig.providerType != AiProviderType.DISABLED) "IA ACTIVE" else "IA DÉSACTIVÉE",
                                            if (aiConfig.isEnabled && aiConfig.providerType != AiProviderType.DISABLED) ElectricGreen else StatusWarning
                                        )
                                        ErpStatusBadge(aiConfig.providerType.label, StatusInfo)
                                    }
                                    Switch(
                                        checked = aiConfig.isEnabled && aiConfig.providerType != AiProviderType.DISABLED,
                                        onCheckedChange = { enabled ->
                                            val nextType = if (!enabled) AiProviderType.DISABLED else AiProviderType.LOCAL_DETERMINISTIC
                                            onUpdateAiConfig(enabled, nextType, aiConfig.endpointUrl, aiConfig.modelName, aiConfig.apiKey, aiConfig.monthlyQuotaLimitRequests)
                                        },
                                        modifier = Modifier.testTag("toggle_ai_switch")
                                    )
                                }

                                Text(
                                    text = "${aiConfig.providerType.description}\nQuota : ${aiConfig.usedRequestsCount}/${aiConfig.monthlyQuotaLimitRequests} requêtes • Licence : ${aiConfig.providerType.licenseNote}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Quick analytical prompts
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                    AssistChip(
                                        onClick = { onSendAiPrompt("Analyse financière & trésorerie OHADA") },
                                        label = { Text("Analyse Trésorerie", style = MaterialTheme.typography.labelSmall) }
                                    )
                                    AssistChip(
                                        onClick = { onSendAiPrompt("Plan de relance factures impayées & conversion prospects") },
                                        label = { Text("Relances & CRM", style = MaterialTheme.typography.labelSmall) }
                                    )
                                    AssistChip(
                                        onClick = { onSendAiPrompt("Virement sensible de 5 000 000 XAF") },
                                        label = { Text("Test Garde-Fou", style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                        }
                    }

                    items(aiMessages, key = { it.id }) { msg ->
                        val isUser = msg.role == "USER"
                        val isGuard = msg.role == "SYSTEM_GUARDRAIL"
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = when {
                                isUser -> ElectricGreen.copy(alpha = 0.14f)
                                isGuard -> StatusWarning.copy(alpha = 0.14f)
                                else -> MaterialTheme.colorScheme.surface
                            },
                            border = BorderStroke(
                                1.dp,
                                when {
                                    isUser -> ElectricGreen.copy(alpha = 0.45f)
                                    isGuard -> StatusWarning
                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isUser) "Vous" else "Business OS Analyst",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isUser) ElectricGreen else SecondaryEmerald
                                    )
                                    ErpStatusBadge(msg.providerBadge, if (isGuard) StatusWarning else ElectricGreen)
                                }
                                Text(msg.content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)

                                msg.requiresHumanApprovalAction?.let { guardMsg ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = StatusWarning.copy(alpha = 0.18f),
                                        border = BorderStroke(1.dp, StatusWarning)
                                    ) {
                                        Text(
                                            text = guardMsg,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = StatusWarning,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = promptInput,
                                onValueChange = { promptInput = it },
                                placeholder = { Text("Questionner l'analyste ERP / OHADA...") },
                                modifier = Modifier.weight(1f).testTag("ai_prompt_input"),
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    if (promptInput.isNotBlank()) {
                                        onSendAiPrompt(promptInput)
                                        promptInput = ""
                                    }
                                },
                                enabled = !isAiThinking,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricGreen, contentColor = ObsidianBlack),
                                modifier = Modifier.height(54.dp).testTag("send_ai_prompt_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Envoyer")
                            }
                        }
                    }
                }
            }

            // SUBTAB 1: MOTEUR D'AUTOMATISATIONS & WORKFLOWS
            1 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
                ) {
                    item {
                        ErpSectionHeader(
                            title = "Moteur d'Automatisations & Workflows",
                            subtitle = "Triggers, Conditions, Actions, Historique & Validation Humaine",
                            actionLabel = "+ Workflow",
                            onActionClick = { showAddWorkflowDialog = true }
                        )
                    }

                    items(workflows, key = { it.id }) { wf ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        ErpStatusBadge("TRIGGER: ${wf.triggerEvent}", ElectricGreen)
                                        if (wf.requiresHumanApproval) {
                                            ErpStatusBadge("VALIDATION HUMAINE", StatusWarning)
                                        }
                                    }
                                    Switch(
                                        checked = wf.isActive,
                                        onCheckedChange = { onToggleWorkflow(wf) }
                                    )
                                }

                                Text(wf.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Condition : ${wf.conditionExpr} ➔ Action : ${wf.actionType}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = StatusInfo
                                )
                                Text(
                                    "Exécutions : ${wf.runCount} • Dernier état : ${wf.lastRunStatus}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = {
                                            if (wf.requiresHumanApproval) {
                                                workflowPendingApproval = wf
                                            } else {
                                                onExecuteWorkflow(wf, true)
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ElectricGreen, contentColor = ObsidianBlack),
                                        modifier = Modifier.testTag("run_workflow_${wf.id}")
                                    ) {
                                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(if (wf.requiresHumanApproval) "Vérifier & Exécuter" else "Exécuter Maintenant")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SUBTAB 2: GESTION DOCUMENTAIRE SÉCURISÉE (GED)
            2 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
                ) {
                    item {
                        ErpSectionHeader(
                            title = "Gestionnaire Documentaire Sécurisé (GED)",
                            subtitle = "Stockage interchangeable (Système de fichiers local ou Objet S3/MinIO) + Empreinte SHA-256",
                            actionLabel = "+ Indexer Document",
                            onActionClick = { showAddDocDialog = true }
                        )
                    }

                    items(documents, key = { it.id }) { doc ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        ErpStatusBadge(doc.category, ElectricGreen)
                                        ErpStatusBadge(doc.storageDriver, StatusInfo)
                                        ErpStatusBadge(doc.accessLevel, SecondaryEmerald)
                                    }
                                    Text("v${doc.version}", style = MaterialTheme.typography.labelMedium, color = MutedSteelGray)
                                }

                                Text(doc.title, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${doc.fileName} (${doc.sizeBytes / 1024} KB) • SHA-256: ${doc.checksumSha256.take(20)}...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(onClick = { docPreviewTarget = doc }) {
                                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Prévisualiser", color = ElectricGreen)
                                    }
                                    TextButton(onClick = { onArchiveDocument(doc) }) {
                                        Text("Archivage Contrôlé", color = StatusWarning)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Configure AI Provider
    if (showAiConfigDialog) {
        var enabled by remember { mutableStateOf(aiConfig.isEnabled) }
        var provider by remember { mutableStateOf(aiConfig.providerType) }
        var endpoint by remember { mutableStateOf(aiConfig.endpointUrl) }
        var model by remember { mutableStateOf(aiConfig.modelName) }
        var apiKey by remember { mutableStateOf(aiConfig.apiKey) }
        var quotaStr by remember { mutableStateOf(aiConfig.monthlyQuotaLimitRequests.toString()) }

        AlertDialog(
            onDismissRequest = { showAiConfigDialog = false },
            title = { Text("Configuration Architecture AIProvider") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Activer Business AI")
                        Switch(checked = enabled, onCheckedChange = { enabled = it })
                    }
                    AiProviderType.entries.forEach { p ->
                        FilterChip(
                            selected = provider == p,
                            onClick = {
                                provider = p
                                enabled = (p != AiProviderType.DISABLED)
                            },
                            label = { Text(p.label, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (provider == AiProviderType.OLLAMA_SELF_HOSTED || provider == AiProviderType.OPENAI_COMPATIBLE) {
                        OutlinedTextField(value = endpoint, onValueChange = { endpoint = it }, label = { Text("URL Serveur IA (ex: http://localhost:11434)") }, singleLine = true)
                        OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Modèle (ex: llama3.2, mistral, qwen2.5)") }, singleLine = true)
                    }
                    if (provider == AiProviderType.OPENAI_COMPATIBLE) {
                        OutlinedTextField(value = apiKey, onValueChange = { apiKey = it }, label = { Text("Clé API Serveur (Stockée localement)") }, singleLine = true)
                    }
                    OutlinedTextField(value = quotaStr, onValueChange = { quotaStr = it }, label = { Text("Quota Mensuel Max (Requêtes)") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(onClick = {
                    onUpdateAiConfig(enabled, provider, endpoint, model, apiKey, quotaStr.toIntOrNull() ?: 500)
                    showAiConfigDialog = false
                }) { Text("Enregistrer") }
            },
            dismissButton = {
                TextButton(onClick = { showAiConfigDialog = false }) { Text("Annuler") }
            }
        )
    }

    // Dialog: Human Approval for Workflow Execution
    workflowPendingApproval?.let { wf ->
        AlertDialog(
            onDismissRequest = { workflowPendingApproval = null },
            title = { Text("Validation Humaine Requise") },
            text = {
                Text(
                    "Le workflow '${wf.name}' (${wf.actionType}) requiert une approbation humaine explicite avant d'exécuter l'action métier. Confirmez-vous l'exécution ?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onExecuteWorkflow(wf, true)
                        workflowPendingApproval = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricGreen, contentColor = ObsidianBlack)
                ) {
                    Text("Approuver & Exécuter")
                }
            },
            dismissButton = {
                TextButton(onClick = { workflowPendingApproval = null }) { Text("Refuser") }
            }
        )
    }

    // Dialog: Add Workflow
    if (showAddWorkflowDialog) {
        var name by remember { mutableStateOf("") }
        var trigger by remember { mutableStateOf("INVOICE_OVERDUE") }
        var cond by remember { mutableStateOf("days_overdue >= 3") }
        var action by remember { mutableStateOf("SEND_RELANCE") }
        var humanApproval by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showAddWorkflowDialog = false },
            title = { Text("Créer un Workflow d'Automatisation") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom du Workflow") }, singleLine = true)
                    OutlinedTextField(value = trigger, onValueChange = { trigger = it }, label = { Text("Trigger (INVOICE_OVERDUE, STOCK_LOW...)") }, singleLine = true)
                    OutlinedTextField(value = cond, onValueChange = { cond = it }, label = { Text("Condition d'exécution") }, singleLine = true)
                    OutlinedTextField(value = action, onValueChange = { action = it }, label = { Text("Action (SEND_RELANCE, CREATE_PO...)") }, singleLine = true)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Exiger validation humaine")
                        Switch(checked = humanApproval, onCheckedChange = { humanApproval = it })
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank()) {
                        onAddWorkflow(name, trigger, cond, action, humanApproval)
                        showAddWorkflowDialog = false
                    }
                }) { Text("Créer Workflow") }
            },
            dismissButton = {
                TextButton(onClick = { showAddWorkflowDialog = false }) { Text("Annuler") }
            }
        )
    }

    // Dialog: Add Document to Vault
    if (showAddDocDialog) {
        var title by remember { mutableStateOf("") }
        var cat by remember { mutableStateOf("CONTRACT") }
        var fileName by remember { mutableStateOf("accord_client_2026.pdf") }
        var driver by remember { mutableStateOf("LOCAL_FS") }
        var access by remember { mutableStateOf("FINANCE_ONLY") }
        var preview by remember { mutableStateOf("Contenu vérifié et signé numériquement.") }

        AlertDialog(
            onDismissRequest = { showAddDocDialog = false },
            title = { Text("Indexer un Document dans la GED") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Titre du Document") }, singleLine = true)
                    OutlinedTextField(value = fileName, onValueChange = { fileName = it }, label = { Text("Nom du fichier") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = driver == "LOCAL_FS", onClick = { driver = "LOCAL_FS" }, label = { Text("Disque Local") })
                        FilterChip(selected = driver == "S3_MINIO_COMPATIBLE", onClick = { driver = "S3_MINIO_COMPATIBLE" }, label = { Text("Objet S3 / MinIO") })
                    }
                    OutlinedTextField(value = preview, onValueChange = { preview = it }, label = { Text("Résumé / Extrait indexé") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank()) {
                        onAddDocument(title, cat, fileName, driver, access, preview)
                        showAddDocDialog = false
                    }
                }) { Text("Enregistrer") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDocDialog = false }) { Text("Annuler") }
            }
        )
    }

    // Dialog: Document Preview
    docPreviewTarget?.let { doc ->
        AlertDialog(
            onDismissRequest = { docPreviewTarget = null },
            title = { Text(doc.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ErpStatusBadge("SHA-256: ${doc.checksumSha256.take(24)}...", ElectricGreen)
                    Text("Fichier : ${doc.fileName} • Driver : ${doc.storageDriver} • Accès : ${doc.accessLevel}", style = MaterialTheme.typography.labelMedium)
                    HorizontalDivider()
                    Text(doc.contentPreview, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Button(onClick = { docPreviewTarget = null }) { Text("Fermer") }
            }
        )
    }
}
