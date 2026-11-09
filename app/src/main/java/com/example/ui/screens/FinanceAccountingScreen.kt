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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.BusinessOsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FinanceAccountingScreen(
    organization: OrganizationEntity?,
    accounts: List<AccountEntity>,
    transactions: List<TransactionEntity>,
    accountingEntries: List<AccountingEntryEntity>,
    onRecordTransaction: (Long, String, String, Double, String, String, String) -> Unit,
    onReverseTransaction: (TransactionEntity, String) -> Unit,
    onCreateAccount: (String, String, String, String, Double) -> Unit,
    onAddOhadaEntry: (String, String, String, String, String, Double, String) -> Unit,
    onToggleReconcile: (AccountingEntryEntity) -> Unit
) {
    val currency = organization?.currency ?: "XAF"
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf(
        "Trésorerie & Comptes",
        "Transactions & Paiements",
        "Journal & Grand Livre OHADA",
        "Bilan, Résultat & Taxes"
    )

    var showNewTxDialog by remember { mutableStateOf(false) }
    var showNewAccDialog by remember { mutableStateOf(false) }
    var showNewOhadaDialog by remember { mutableStateOf(false) }
    var txToReverse by remember { mutableStateOf<TransactionEntity?>(null) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE) }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        ErpSubTabRow(
            tabs = subTabs,
            selectedIndex = selectedSubTab,
            onSelect = { selectedSubTab = it },
            tagPrefix = "finance_subtab"
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
        ) {
            when (selectedSubTab) {
                // SUBTAB 0: TRÉSORERIE & COMPTES
                0 -> {
                    item {
                        ErpSectionHeader(
                            title = "Comptes de Trésorerie, Créances & Dettes",
                            subtitle = "Codification SYSCOHADA Révisé (Classes 4 & 5)",
                            actionLabel = "+ Nouveau Compte",
                            onActionClick = { showNewAccDialog = true }
                        )
                    }

                    items(accounts, key = { it.id }) { acc ->
                        val badgeColor = when (acc.type) {
                            "BANK" -> ElectricGreen
                            "MOBILE_MONEY" -> SecondaryEmerald
                            "CASH" -> StatusInfo
                            "RECEIVABLE" -> StatusWarning
                            else -> StatusDanger
                        }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        ErpStatusBadge("OHADA ${acc.ohadaCode}", badgeColor)
                                        ErpStatusBadge(acc.type, MutedSteelGray)
                                    }
                                    Text(acc.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Réf: ${acc.accountNumber}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = BusinessOsViewModel.formatMoney(acc.balanceCents, acc.currency),
                                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                                    color = badgeColor
                                )
                            }
                        }
                    }
                }

                // SUBTAB 1: TRANSACTIONS & PAIEMENTS
                1 -> {
                    item {
                        ErpSectionHeader(
                            title = "Transactions & Passerelles de Paiement",
                            subtitle = "MTN MoMo • Orange Money • Campay • Money Fusion • Stripe • PayPal • Manuel",
                            actionLabel = "+ Opération",
                            onActionClick = { showNewTxDialog = true }
                        )
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ElectricGreen.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, ElectricGreen.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = "Audit", tint = ElectricGreen)
                                Text(
                                    text = "Règle d'Intégrité Comptable : Une transaction validée ne peut jamais être supprimée silencieusement. Toute annulation génère une écriture de contre-passation OHADA et un Audit Log.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    items(transactions, key = { it.id }) { tx ->
                        val isIncome = tx.type == "INCOME"
                        val isReversed = tx.status == "REVERSED"
                        val amountColor = when {
                            isReversed -> MutedSteelGray
                            isIncome -> ElectricGreen
                            else -> StatusDanger
                        }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        ErpStatusBadge(tx.reference, if (isIncome) ElectricGreen else StatusDanger)
                                        ErpStatusBadge(tx.paymentMethod, StatusInfo)
                                        ErpStatusBadge(tx.status, if (isReversed) StatusWarning else SecondaryEmerald)
                                    }
                                    Text(
                                        text = (if (isIncome) "+" else "-") + BusinessOsViewModel.formatMoney(tx.amountCents, tx.currency),
                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold),
                                        color = amountColor
                                    )
                                }

                                Text(tx.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${tx.accountName} • Justif: ${tx.supportingDocRef} • ${dateFormatter.format(Date(tx.timestamp))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (!isReversed) {
                                        TextButton(
                                            onClick = { txToReverse = tx },
                                            modifier = Modifier.testTag("reverse_tx_${tx.id}")
                                        ) {
                                            Text("Contre-passer", color = StatusWarning, style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // SUBTAB 2: JOURNAL & GRAND LIVRE OHADA
                2 -> {
                    item {
                        ErpSectionHeader(
                            title = "Journal Général & Grand Livre OHADA",
                            subtitle = "Partie double équilibrée (Journaux VE, AC, BQ, CA, OD) & Rapprochement Bancaire",
                            actionLabel = "+ Écriture OHADA",
                            onActionClick = { showNewOhadaDialog = true }
                        )
                    }

                    val totalDebit = accountingEntries.sumOf { it.amountCents }
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Contrôle d'Équilibre de la Balance", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        "Total Débit = Total Crédit : ${BusinessOsViewModel.formatMoney(totalDebit, currency)}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                        color = ElectricGreen
                                    )
                                }
                                ErpStatusBadge("ÉQUILIBRÉ 100%", ElectricGreen)
                            }
                        }
                    }

                    items(accountingEntries, key = { it.id }) { entry ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        ErpStatusBadge("Journal ${entry.journalCode}", ElectricGreen)
                                        ErpStatusBadge(entry.reference, StatusInfo)
                                    }
                                    Text(
                                        text = BusinessOsViewModel.formatMoney(entry.amountCents, currency),
                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("DÉBIT : ${entry.ohadaDebitCode}", style = MaterialTheme.typography.labelMedium, color = ElectricGreen)
                                        Text(entry.ohadaDebitName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("CRÉDIT : ${entry.ohadaCreditCode}", style = MaterialTheme.typography.labelMedium, color = StatusInfo)
                                        Text(entry.ohadaCreditName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${entry.description} • TVA: ${entry.taxRatePercent}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                    FilterChip(
                                        selected = entry.isReconciled,
                                        onClick = { onToggleReconcile(entry) },
                                        label = {
                                            Text(
                                                if (entry.isReconciled) "Rapproché" else "Rapprocher",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // SUBTAB 3: BILAN, COMPTE DE RÉSULTAT & TAXES OHADA
                3 -> {
                    item {
                        ErpSectionHeader(
                            title = "États Financiers SYSCOHADA & Fiscalité",
                            subtitle = "Bilan Synthétique, Compte de Résultat & TVA Collectée/Déductible"
                        )
                    }

                    val totalTreasury = accounts.filter { it.type == "BANK" || it.type == "MOBILE_MONEY" || it.type == "CASH" }.sumOf { it.balanceCents }
                    val totalReceivables = accounts.filter { it.type == "RECEIVABLE" }.sumOf { it.balanceCents }
                    val totalPayables = accounts.filter { it.type == "PAYABLE" }.sumOf { it.balanceCents }
                    val totalIncome = transactions.filter { it.type == "INCOME" && it.status != "REVERSED" }.sumOf { it.amountCents }
                    val totalExpense = transactions.filter { it.type == "EXPENSE" && it.status != "REVERSED" }.sumOf { it.amountCents }
                    val vatCollected = transactions.filter { it.type == "INCOME" && it.status != "REVERSED" }.sumOf { it.taxAmountCents }
                    val vatDeductible = transactions.filter { it.type == "EXPENSE" && it.status != "REVERSED" }.sumOf { it.taxAmountCents }
                    val netResult = totalIncome - totalExpense

                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("1. COMPTE DE RÉSULTAT (Classes 6 & 7 OHADA)", style = MaterialTheme.typography.titleMedium, color = ElectricGreen)
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Text("Produits d'Exploitation (Classe 70-75)", style = MaterialTheme.typography.bodyMedium)
                                    Text(BusinessOsViewModel.formatMoney(totalIncome, currency), style = MaterialTheme.typography.labelMedium, color = ElectricGreen)
                                }
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Text("Charges d'Exploitation & Personnel (Classe 60-66)", style = MaterialTheme.typography.bodyMedium)
                                    Text(BusinessOsViewModel.formatMoney(totalExpense, currency), style = MaterialTheme.typography.labelMedium, color = StatusDanger)
                                }
                                HorizontalDivider()
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Text("Résultat Net de l'Exercice (Compte 131)", fontWeight = FontWeight.Bold)
                                    Text(
                                        BusinessOsViewModel.formatMoney(netResult, currency),
                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                        color = if (netResult >= 0) ElectricGreen else StatusDanger
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("2. BILAN PATRIMONIAL (Actif Circulant, Trésorerie & Passif)", style = MaterialTheme.typography.titleMedium, color = SecondaryEmerald)
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Text("Trésorerie-Actif (Classes 52, 55, 57)", style = MaterialTheme.typography.bodyMedium)
                                    Text(BusinessOsViewModel.formatMoney(totalTreasury, currency), style = MaterialTheme.typography.labelMedium)
                                }
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Text("Créances Clients (Compte 411)", style = MaterialTheme.typography.bodyMedium)
                                    Text(BusinessOsViewModel.formatMoney(totalReceivables, currency), style = MaterialTheme.typography.labelMedium)
                                }
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Text("Dettes Fournisseurs (Compte 401)", style = MaterialTheme.typography.bodyMedium)
                                    Text(BusinessOsViewModel.formatMoney(totalPayables, currency), style = MaterialTheme.typography.labelMedium, color = StatusWarning)
                                }
                            }
                        }
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("3. DÉCLARATION FISCALE & TVA (${organization?.defaultVatPercent ?: 19.25}%)", style = MaterialTheme.typography.titleMedium, color = StatusInfo)
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Text("TVA Facturée / Collectée (443)", style = MaterialTheme.typography.bodyMedium)
                                    Text(BusinessOsViewModel.formatMoney(vatCollected, currency), style = MaterialTheme.typography.labelMedium)
                                }
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Text("TVA Récupérable / Déductible (445)", style = MaterialTheme.typography.bodyMedium)
                                    Text(BusinessOsViewModel.formatMoney(vatDeductible, currency), style = MaterialTheme.typography.labelMedium)
                                }
                                HorizontalDivider()
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Text("TVA Nette à Décaisser (4441)", fontWeight = FontWeight.Bold)
                                    Text(
                                        BusinessOsViewModel.formatMoney((vatCollected - vatDeductible).coerceAtLeast(0L), currency),
                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                        color = StatusWarning
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: New Financial Transaction
    if (showNewTxDialog) {
        var txType by remember { mutableStateOf("INCOME") }
        var category by remember { mutableStateOf("Prestation & Licence ERP") }
        var amountStr by remember { mutableStateOf("") }
        var method by remember { mutableStateOf("MTN_MOMO") }
        var desc by remember { mutableStateOf("") }
        var docRef by remember { mutableStateOf("") }
        val selectedAccId = accounts.firstOrNull()?.id ?: 1L

        val methods = listOf("MTN_MOMO", "ORANGE_MONEY", "CAMPAY", "MONEY_FUSION", "BANK_TRANSFER", "STRIPE", "PAYPAL", "MANUAL")

        AlertDialog(
            onDismissRequest = { showNewTxDialog = false },
            title = { Text("Enregistrer une Transaction Validée") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = txType == "INCOME", onClick = { txType = "INCOME" }, label = { Text("Revenu (+)") })
                        FilterChip(selected = txType == "EXPENSE", onClick = { txType = "EXPENSE" }, label = { Text("Dépense (-)") })
                    }
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Montant ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Catégorie Financière") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Mode de Paiement :", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        methods.take(4).forEach { m ->
                            FilterChip(selected = method == m, onClick = { method = m }, label = { Text(m, style = MaterialTheme.typography.labelSmall) })
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        methods.drop(4).forEach { m ->
                            FilterChip(selected = method == m, onClick = { method = m }, label = { Text(m, style = MaterialTheme.typography.labelSmall) })
                        }
                    }
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Libellé & Justification") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = docRef,
                        onValueChange = { docRef = it },
                        label = { Text("Réf. Justificatif (Facture / Reçu)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountStr.toDoubleOrNull() ?: 0.0
                        if (amt > 0.0) {
                            onRecordTransaction(selectedAccId, txType, category, amt, method, desc, docRef)
                            showNewTxDialog = false
                        }
                    }
                ) {
                    Text("Valider & Comptabiliser")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewTxDialog = false }) { Text("Annuler") }
            }
        )
    }

    // Dialog: Reverse Validated Transaction (Contre-passation)
    txToReverse?.let { targetTx ->
        var reason by remember { mutableStateOf("Erreur d'imputation / Annulation documentée") }
        AlertDialog(
            onDismissRequest = { txToReverse = null },
            title = { Text("Contre-Passation Comptable (${targetTx.reference})") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Conformément aux normes d'audit BUSINESS OS, cette transaction validée ne sera pas supprimée de la base de données. Une écriture inverse d'extourne sera générée au journal OD.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Motif obligatoire d'audit") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReverseTransaction(targetTx, reason)
                        txToReverse = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusWarning, contentColor = ObsidianBlack)
                ) {
                    Text("Exécuter Contre-Passation")
                }
            },
            dismissButton = {
                TextButton(onClick = { txToReverse = null }) { Text("Annuler") }
            }
        )
    }

    // Dialog: New Account
    if (showNewAccDialog) {
        var name by remember { mutableStateOf("") }
        var accNum by remember { mutableStateOf("") }
        var ohadaCode by remember { mutableStateOf("5212") }
        var accType by remember { mutableStateOf("BANK") }
        var balStr by remember { mutableStateOf("0") }

        AlertDialog(
            onDismissRequest = { showNewAccDialog = false },
            title = { Text("Nouveau Compte Trésorerie / OHADA") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Intitulé du Compte") }, singleLine = true)
                    OutlinedTextField(value = ohadaCode, onValueChange = { ohadaCode = it }, label = { Text("Code Compte OHADA (ex: 521, 552, 571)") }, singleLine = true)
                    OutlinedTextField(value = accNum, onValueChange = { accNum = it }, label = { Text("RIB / Identifiant") }, singleLine = true)
                    OutlinedTextField(
                        value = balStr,
                        onValueChange = { balStr = it },
                        label = { Text("Solde initial ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank()) {
                        onCreateAccount(name, accNum, ohadaCode, accType, balStr.toDoubleOrNull() ?: 0.0)
                        showNewAccDialog = false
                    }
                }) { Text("Créer") }
            },
            dismissButton = {
                TextButton(onClick = { showNewAccDialog = false }) { Text("Annuler") }
            }
        )
    }

    // Dialog: New Manual OHADA Entry
    if (showNewOhadaDialog) {
        var journal by remember { mutableStateOf("OD") }
        var debitCode by remember { mutableStateOf("6221") }
        var debitName by remember { mutableStateOf("Locations & Charges Locatives") }
        var creditCode by remember { mutableStateOf("5211") }
        var creditName by remember { mutableStateOf("Banque Locale") }
        var amountStr by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNewOhadaDialog = false },
            title = { Text("Saisie d'Écriture Comptable OHADA") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("VE", "AC", "BQ", "CA", "OD").forEach { j ->
                            FilterChip(selected = journal == j, onClick = { journal = j }, label = { Text(j) })
                        }
                    }
                    OutlinedTextField(value = debitCode, onValueChange = { debitCode = it }, label = { Text("Compte Débit OHADA") }, singleLine = true)
                    OutlinedTextField(value = debitName, onValueChange = { debitName = it }, label = { Text("Intitulé Débit") }, singleLine = true)
                    OutlinedTextField(value = creditCode, onValueChange = { creditCode = it }, label = { Text("Compte Crédit OHADA") }, singleLine = true)
                    OutlinedTextField(value = creditName, onValueChange = { creditName = it }, label = { Text("Intitulé Crédit") }, singleLine = true)
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Montant Équilibré ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Libellé de l'écriture") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt > 0.0) {
                        onAddOhadaEntry(journal, debitCode, debitName, creditCode, creditName, amt, desc)
                        showNewOhadaDialog = false
                    }
                }) { Text("Valider Écriture") }
            },
            dismissButton = {
                TextButton(onClick = { showNewOhadaDialog = false }) { Text("Annuler") }
            }
        )
    }
}
