package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.BusinessOsViewModel

@Composable
fun HrProjectsProductivityScreen(
    organization: OrganizationEntity?,
    employees: List<EmployeeEntity>,
    projectTasks: List<ProjectTaskEntity>,
    onAddEmployee: (String, String, String, String, Double) -> Unit,
    onAddProjectTask: (String, String, String, String, Double) -> Unit,
    onAdvanceTask: (ProjectTaskEntity) -> Unit
) {
    val currency = organization?.currency ?: "XAF"
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf(
        "Collaborateurs, Paie & Congés",
        "Projets, Tâches & Kanban"
    )

    var showAddEmpDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        ErpSubTabRow(
            tabs = subTabs,
            selectedIndex = selectedSubTab,
            onSelect = { selectedSubTab = it },
            tagPrefix = "hr_subtab"
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
        ) {
            when (selectedSubTab) {
                // SUBTAB 0: COLLABORATEURS, PAIE, CONTRATS & CONGÉS
                0 -> {
                    val totalPayrollCents = employees.sumOf { it.baseSalaryCents }
                    item {
                        ErpSectionHeader(
                            title = "Ressources Humaines, Contrats & Paie",
                            subtitle = "${employees.size} collaborateurs • Masse salariale mensuelle : ${BusinessOsViewModel.formatMoney(totalPayrollCents, currency)}",
                            actionLabel = "+ Collaborateur",
                            onActionClick = { showAddEmpDialog = true }
                        )
                    }

                    items(employees, key = { it.id }) { emp ->
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
                                        ErpStatusBadge(emp.contractType, ElectricGreen)
                                        ErpStatusBadge(emp.department, StatusInfo)
                                    }
                                    Text(
                                        text = BusinessOsViewModel.formatMoney(emp.baseSalaryCents, currency) + " / mois",
                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold),
                                        color = ElectricGreen
                                    )
                                }

                                Text(emp.fullName, style = MaterialTheme.typography.titleLarge)
                                Text(emp.roleTitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Solde Congés : ${emp.leaveBalanceDays} j. • Performance : ${emp.performanceScore}/100",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = SecondaryEmerald
                                    )
                                    ErpStatusBadge(emp.status, ElectricGreen)
                                }
                            }
                        }
                    }
                }

                // SUBTAB 1: PROJETS, TÂCHES & AFFECTATIONS
                1 -> {
                    item {
                        ErpSectionHeader(
                            title = "Projets, Tâches & Suivi d'Exécution",
                            subtitle = "Avancez le statut d'une tâche (TODO -> IN_PROGRESS -> REVIEW -> DONE)",
                            actionLabel = "+ Nouvelle Tâche",
                            onActionClick = { showAddTaskDialog = true }
                        )
                    }

                    items(projectTasks, key = { it.id }) { task ->
                        val priorityColor = when (task.priority) {
                            "CRITICAL" -> StatusDanger
                            "HIGH" -> StatusWarning
                            else -> StatusInfo
                        }
                        val statusColor = when (task.status) {
                            "DONE" -> ElectricGreen
                            "IN_PROGRESS", "REVIEW" -> SecondaryEmerald
                            else -> MutedSteelGray
                        }

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
                                        ErpStatusBadge(task.priority, priorityColor)
                                        ErpStatusBadge(task.status, statusColor)
                                    }
                                    Text(
                                        text = "Budget: " + BusinessOsViewModel.formatMoney(task.budgetCents, currency),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(task.projectName, style = MaterialTheme.typography.labelLarge, color = ElectricGreen)
                                Text(task.taskTitle, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "Assigné à : ${task.assigneeName} • Note : ${task.commentsLog}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                LinearProgressIndicator(
                                    progress = { task.progressPercent / 100f },
                                    modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)),
                                    color = statusColor,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${task.progressPercent}% complété", style = MaterialTheme.typography.labelMedium, color = statusColor)
                                    OutlinedButton(
                                        onClick = { onAdvanceTask(task) },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("advance_task_${task.id}")
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Avancer Étape", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddEmpDialog) {
        var name by remember { mutableStateOf("") }
        var role by remember { mutableStateOf("Ingénieur Full-Stack") }
        var dept by remember { mutableStateOf("Ingénierie & Cloud") }
        var contract by remember { mutableStateOf("CDI") }
        var salaryStr by remember { mutableStateOf("850000") }

        AlertDialog(
            onDismissRequest = { showAddEmpDialog = false },
            title = { Text("Nouveau Collaborateur RH") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom & Prénom") }, singleLine = true)
                    OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Poste / Fonction") }, singleLine = true)
                    OutlinedTextField(value = dept, onValueChange = { dept = it }, label = { Text("Département") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("CDI", "CDD", "CONSULTANT").forEach { c ->
                            FilterChip(selected = contract == c, onClick = { contract = c }, label = { Text(c) })
                        }
                    }
                    OutlinedTextField(
                        value = salaryStr,
                        onValueChange = { salaryStr = it },
                        label = { Text("Salaire Brut Mensuel ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank()) {
                        onAddEmployee(name, role, dept, contract, salaryStr.toDoubleOrNull() ?: 0.0)
                        showAddEmpDialog = false
                    }
                }) { Text("Ajouter") }
            },
            dismissButton = {
                TextButton(onClick = { showAddEmpDialog = false }) { Text("Annuler") }
            }
        )
    }

    if (showAddTaskDialog) {
        var project by remember { mutableStateOf("Déploiement Client ERP") }
        var title by remember { mutableStateOf("") }
        var assignee by remember { mutableStateOf(employees.firstOrNull()?.fullName ?: "Équipe") }
        var priority by remember { mutableStateOf("HIGH") }
        var budgetStr by remember { mutableStateOf("1000000") }

        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Nouvelle Tâche / Projet") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = project, onValueChange = { project = it }, label = { Text("Nom du Projet") }, singleLine = true)
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Intitulé de la Tâche") }, singleLine = true)
                    OutlinedTextField(value = assignee, onValueChange = { assignee = it }, label = { Text("Responsable Assigné") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("LOW", "MEDIUM", "HIGH", "CRITICAL").forEach { p ->
                            FilterChip(selected = priority == p, onClick = { priority = p }, label = { Text(p, style = MaterialTheme.typography.labelSmall) })
                        }
                    }
                    OutlinedTextField(
                        value = budgetStr,
                        onValueChange = { budgetStr = it },
                        label = { Text("Budget Alloué ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank()) {
                        onAddProjectTask(project, title, assignee, priority, budgetStr.toDoubleOrNull() ?: 0.0)
                        showAddTaskDialog = false
                    }
                }) { Text("Créer Tâche") }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) { Text("Annuler") }
            }
        )
    }
}
