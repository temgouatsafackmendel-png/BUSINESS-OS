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

@Composable
fun CrmSalesBillingScreen(
    organization: OrganizationEntity?,
    contacts: List<CrmContactEntity>,
    commercialDocs: List<CommercialDocEntity>,
    onAddContact: (String, String, String, String, String, String, Double, String, String) -> Unit,
    onConvertProspect: (CrmContactEntity) -> Unit,
    onCreateDoc: (Long, String, String, String, Double, Int) -> Unit,
    onPayInvoice: (CommercialDocEntity, String) -> Unit
) {
    val currency = organization?.currency ?: "XAF"
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf(
        "Prospects & Pipeline",
        "Clients Actifs",
        "Devis, Factures & Contrats",
        "Marketing & Relances"
    )

    var showAddContactDialog by remember { mutableStateOf(false) }
    var defaultContactKind by remember { mutableStateOf("LEAD") }
    var showNewDocDialog by remember { mutableStateOf(false) }
    var invoiceToPay by remember { mutableStateOf<CommercialDocEntity?>(null) }

    val leads = remember(contacts) { contacts.filter { it.kind == "LEAD" } }
    val customers = remember(contacts) { contacts.filter { it.kind == "CUSTOMER" } }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        ErpSubTabRow(
            tabs = subTabs,
            selectedIndex = selectedSubTab,
            onSelect = { selectedSubTab = it },
            tagPrefix = "crm_subtab"
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
        ) {
            when (selectedSubTab) {
                // SUBTAB 0: PROSPECTS & PIPELINE COMMERCIAL
                0 -> {
                    item {
                        ErpSectionHeader(
                            title = "Pipeline Commercial & Opportunités",
                            subtitle = "Convertissez un prospect en client en 1 clic sans perdre son historique",
                            actionLabel = "+ Prospect",
                            onActionClick = {
                                defaultContactKind = "LEAD"
                                showAddContactDialog = true
                            }
                        )
                    }

                    items(leads, key = { it.id }) { lead ->
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
                                        ErpStatusBadge("PROSPECT", StatusInfo)
                                        ErpStatusBadge(lead.stage, ElectricGreen)
                                    }
                                    Text(
                                        text = BusinessOsViewModel.formatMoney(lead.estimatedValueCents, currency),
                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold),
                                        color = ElectricGreen
                                    )
                                }

                                Text(lead.companyName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    "${lead.contactName} • ${lead.email} • ${lead.phone} • Source: ${lead.sourceCampaign}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Historique : ${lead.historyLog}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { onConvertProspect(lead) },
                                        colors = ButtonDefaults.buttonColors(containerColor = ElectricGreen, contentColor = ObsidianBlack),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("convert_lead_${lead.id}")
                                    ) {
                                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Convertir en Client (Conserver Historique)", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // SUBTAB 1: CLIENTS ACTIFS
                1 -> {
                    item {
                        ErpSectionHeader(
                            title = "Portefeuille Clients & Historique Intégral",
                            subtitle = "${customers.size} clients actifs isolés pour cette organisation",
                            actionLabel = "+ Nouveau Client",
                            onActionClick = {
                                defaultContactKind = "CUSTOMER"
                                showAddContactDialog = true
                            }
                        )
                    }

                    items(customers, key = { it.id }) { client ->
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
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        ErpStatusBadge("CLIENT ACTIF", ElectricGreen)
                                        if (client.convertedFromLeadAt != null) {
                                            ErpStatusBadge("EX-PROSPECT CONVERTI", SecondaryEmerald)
                                        }
                                    }
                                    Text(
                                        text = BusinessOsViewModel.formatMoney(client.estimatedValueCents, currency),
                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                        color = ElectricGreen
                                    )
                                }
                                Text(client.companyName, style = MaterialTheme.typography.titleLarge)
                                Text(
                                    "${client.contactName} • ${client.email} • ${client.phone}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = client.historyLog,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // SUBTAB 2: DEVIS, FACTURES, COMMANDES, AVOIRS & CONTRATS
                2 -> {
                    item {
                        ErpSectionHeader(
                            title = "Facturation, Devis, Commandes & Contrats",
                            subtitle = "Calcul automatique TVA (${organization?.defaultVatPercent ?: 19.25}%) & synchronisation OHADA",
                            actionLabel = "+ Créer Document",
                            onActionClick = { showNewDocDialog = true }
                        )
                    }

                    items(commercialDocs, key = { it.id }) { doc ->
                        val statusColor = when (doc.status) {
                            "PAID" -> ElectricGreen
                            "OVERDUE" -> StatusDanger
                            else -> StatusWarning
                        }
                        val remainingCents = (doc.totalCents - doc.paidCents).coerceAtLeast(0L)

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
                                        ErpStatusBadge(doc.docNumber, ElectricGreen)
                                        ErpStatusBadge(doc.docType, StatusInfo)
                                        ErpStatusBadge(doc.status, statusColor)
                                    }
                                    Text(
                                        text = BusinessOsViewModel.formatMoney(doc.totalCents, doc.currency),
                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold),
                                        color = statusColor
                                    )
                                }

                                Text("Client : ${doc.contactName}", style = MaterialTheme.typography.titleMedium)
                                Text(doc.itemsDescription, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "HT: ${BusinessOsViewModel.formatMoney(doc.subtotalCents, doc.currency)} • TVA: ${BusinessOsViewModel.formatMoney(doc.taxCents, doc.currency)} • Reste: ${BusinessOsViewModel.formatMoney(remainingCents, doc.currency)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (doc.docType == "INVOICE" && remainingCents > 0L) {
                                        Button(
                                            onClick = { invoiceToPay = doc },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = ElectricGreen, contentColor = ObsidianBlack),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("pay_invoice_${doc.id}")
                                        ) {
                                            Text("Encaisser", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // SUBTAB 3: MARKETING & RELANCES
                3 -> {
                    item {
                        ErpSectionHeader(
                            title = "Campagnes d'Acquisition & Relances Clients",
                            subtitle = "Attribution du chiffre d'affaires par canal et suivi du recouvrement"
                        )
                    }

                    val campaigns = contacts.groupBy { it.sourceCampaign }
                    items(campaigns.entries.toList(), key = { it.key }) { (campaignName, group) ->
                        val totalVal = group.sumOf { it.estimatedValueCents }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(campaignName, style = MaterialTheme.typography.titleMedium)
                                    Text("${group.size} fiches générées (${group.count { it.kind == "CUSTOMER" }} clients convertis)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = BusinessOsViewModel.formatMoney(totalVal, currency),
                                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                    color = ElectricGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Add Prospect or Customer
    if (showAddContactDialog) {
        var company by remember { mutableStateOf("") }
        var person by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("+237 ") }
        var stage by remember { mutableStateOf(if (defaultContactKind == "LEAD") "PROPOSAL" else "WON_CLIENT") }
        var valueStr by remember { mutableStateOf("") }
        var campaign by remember { mutableStateOf("Acquisition B2B Directe") }
        var notes by remember { mutableStateOf("Intéressé par déploiement auto-hébergé.") }

        AlertDialog(
            onDismissRequest = { showAddContactDialog = false },
            title = { Text(if (defaultContactKind == "LEAD") "Nouveau Prospect (Lead)" else "Nouveau Client") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = company, onValueChange = { company = it }, label = { Text("Entreprise / Raison Sociale") }, singleLine = true)
                    OutlinedTextField(value = person, onValueChange = { person = it }, label = { Text("Interlocuteur Principal") }, singleLine = true)
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Professionnel") }, singleLine = true)
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Téléphone") }, singleLine = true)
                    OutlinedTextField(
                        value = valueStr,
                        onValueChange = { valueStr = it },
                        label = { Text("Valeur Estimée ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(value = campaign, onValueChange = { campaign = it }, label = { Text("Campagne Marketing") }, singleLine = true)
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes initiales") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (company.isNotBlank()) {
                        onAddContact(defaultContactKind, company, person, email, phone, stage, valueStr.toDoubleOrNull() ?: 0.0, campaign, notes)
                        showAddContactDialog = false
                    }
                }) { Text("Enregistrer") }
            },
            dismissButton = {
                TextButton(onClick = { showAddContactDialog = false }) { Text("Annuler") }
            }
        )
    }

    // Dialog: Create Commercial Document (Invoice, Quote, Order, Credit Note, Contract)
    if (showNewDocDialog) {
        var docType by remember { mutableStateOf("INVOICE") }
        var clientName by remember { mutableStateOf(customers.firstOrNull()?.companyName ?: "Client Entreprise") }
        var itemsDesc by remember { mutableStateOf("Déploiement ERP BUSINESS OS + Support Annuel") }
        var subtotalStr by remember { mutableStateOf("") }
        var dueDaysStr by remember { mutableStateOf("15") }

        AlertDialog(
            onDismissRequest = { showNewDocDialog = false },
            title = { Text("Émettre un Document Commercial") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("INVOICE", "QUOTE", "ORDER", "CREDIT_NOTE", "CONTRACT").forEach { t ->
                            FilterChip(selected = docType == t, onClick = { docType = t }, label = { Text(t, style = MaterialTheme.typography.labelSmall) })
                        }
                    }
                    OutlinedTextField(value = clientName, onValueChange = { clientName = it }, label = { Text("Client / Prospect") }, singleLine = true)
                    OutlinedTextField(value = itemsDesc, onValueChange = { itemsDesc = it }, label = { Text("Lignes de facturation / Prestations") })
                    OutlinedTextField(
                        value = subtotalStr,
                        onValueChange = { subtotalStr = it },
                        label = { Text("Montant Hors Taxes ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dueDaysStr,
                        onValueChange = { dueDaysStr = it },
                        label = { Text("Échéance (jours)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val sub = subtotalStr.toDoubleOrNull() ?: 0.0
                    if (sub > 0.0) {
                        onCreateDoc(1L, clientName, docType, itemsDesc, sub, dueDaysStr.toIntOrNull() ?: 15)
                        showNewDocDialog = false
                    }
                }) { Text("Émettre Document") }
            },
            dismissButton = {
                TextButton(onClick = { showNewDocDialog = false }) { Text("Annuler") }
            }
        )
    }

    // Dialog: Collect Invoice Payment via Gateway or Manual
    invoiceToPay?.let { targetDoc ->
        var selectedGateway by remember { mutableStateOf("MTN_MOMO") }
        val gateways = listOf("MTN_MOMO", "ORANGE_MONEY", "CAMPAY", "MONEY_FUSION", "BANK_TRANSFER", "STRIPE", "PAYPAL", "MANUAL")
        AlertDialog(
            onDismissRequest = { invoiceToPay = null },
            title = { Text("Encaisser ${targetDoc.docNumber}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Montant à encaisser : ${BusinessOsViewModel.formatMoney(targetDoc.totalCents - targetDoc.paidCents, targetDoc.currency)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = ElectricGreen
                    )
                    Text("Sélectionnez le connecteur ou mode de règlement vérifié :", style = MaterialTheme.typography.bodySmall)
                    gateways.chunked(2).forEach { rowGateways ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            rowGateways.forEach { gw ->
                                FilterChip(
                                    selected = selectedGateway == gw,
                                    onClick = { selectedGateway = gw },
                                    label = { Text(gw) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onPayInvoice(targetDoc, selectedGateway)
                        invoiceToPay = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricGreen, contentColor = ObsidianBlack)
                ) {
                    Text("Valider Encaissement + Écriture OHADA")
                }
            },
            dismissButton = {
                TextButton(onClick = { invoiceToPay = null }) { Text("Annuler") }
            }
        )
    }
}
