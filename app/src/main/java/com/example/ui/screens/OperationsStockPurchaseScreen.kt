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
fun OperationsStockPurchaseScreen(
    organization: OrganizationEntity?,
    products: List<ProductStockEntity>,
    stockMovements: List<StockMovementEntity>,
    suppliers: List<CrmContactEntity>,
    onAddProduct: (String, String, String, String, Double, Double, Int, Int, String, String) -> Unit,
    onRecordStockMovement: (ProductStockEntity, String, Int, String) -> Unit,
    onAddSupplier: (String, String, String, String, Double) -> Unit
) {
    val currency = organization?.currency ?: "XAF"
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf(
        "Produits & Services",
        "Mouvements de Stocks",
        "Achats & Fournisseurs"
    )

    var showAddProdDialog by remember { mutableStateOf(false) }
    var productForMovement by remember { mutableStateOf<ProductStockEntity?>(null) }
    var showAddSupplierDialog by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE) }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        ErpSubTabRow(
            tabs = subTabs,
            selectedIndex = selectedSubTab,
            onSelect = { selectedSubTab = it },
            tagPrefix = "ops_subtab"
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
        ) {
            when (selectedSubTab) {
                // SUBTAB 0: CATALOGUE PRODUITS & SERVICES + STOCKS
                0 -> {
                    item {
                        ErpSectionHeader(
                            title = "Catalogue Produits, Services & Stocks",
                            subtitle = "Valorisation d'inventaire, marges unitaires et seuils d'alerte",
                            actionLabel = "+ Article / Service",
                            onActionClick = { showAddProdDialog = true }
                        )
                    }

                    items(products, key = { it.id }) { prod ->
                        val isLowStock = prod.kind == "PRODUCT" && prod.stockQuantity <= prod.minStockAlert
                        val unitMarginCents = prod.unitPriceCents - prod.costPriceCents

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                if (isLowStock) StatusWarning else MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        ErpStatusBadge(prod.sku, ElectricGreen)
                                        ErpStatusBadge(prod.kind, StatusInfo)
                                        if (isLowStock) {
                                            ErpStatusBadge("ALERTE STOCK (${prod.stockQuantity})", StatusWarning)
                                        }
                                    }
                                    Text(
                                        text = BusinessOsViewModel.formatMoney(prod.unitPriceCents, currency),
                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold),
                                        color = ElectricGreen
                                    )
                                }

                                Text(prod.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Catégorie: ${prod.category} • Coût d'achat: ${BusinessOsViewModel.formatMoney(prod.costPriceCents, currency)} • Marge: +${BusinessOsViewModel.formatMoney(unitMarginCents, currency)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (prod.kind == "PRODUCT") {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Stock: ${prod.stockQuantity} u. (Seuil: ${prod.minStockAlert}) • ${prod.warehouseLocation}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isLowStock) StatusWarning else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedButton(
                                            onClick = { productForMovement = prod },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.testTag("stock_move_${prod.id}")
                                        ) {
                                            Text("Entrée / Sortie", style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // SUBTAB 1: JOURNAL DES MOUVEMENTS DE STOCKS
                1 -> {
                    item {
                        ErpSectionHeader(
                            title = "Journal de Traçabilité des Stocks (IN / OUT)",
                            subtitle = "Historique horodaté des réceptions fournisseurs et sorties livraisons"
                        )
                    }

                    items(stockMovements, key = { it.id }) { mov ->
                        val isIn = mov.movementType == "IN"
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
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        ErpStatusBadge(mov.movementType, if (isIn) ElectricGreen else StatusWarning)
                                        Text(mov.productName, style = MaterialTheme.typography.titleSmall)
                                    }
                                    Text(mov.reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(dateFormatter.format(Date(mov.timestamp)), style = MaterialTheme.typography.labelSmall, color = MutedSteelGray)
                                }
                                Text(
                                    text = (if (isIn) "+" else "-") + "${mov.quantity} u.",
                                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold),
                                    color = if (isIn) ElectricGreen else StatusWarning
                                )
                            }
                        }
                    }
                }

                // SUBTAB 2: ACHATS & FOURNISSEURS
                2 -> {
                    item {
                        ErpSectionHeader(
                            title = "Fournisseurs Agréés & Commandes d'Achat",
                            subtitle = "Gestion des partenaires d'approvisionnement et engagements OHADA 401",
                            actionLabel = "+ Fournisseur",
                            onActionClick = { showAddSupplierDialog = true }
                        )
                    }

                    items(suppliers, key = { it.id }) { sup ->
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
                                    ErpStatusBadge("FOURNISSEUR OHADA 401", SecondaryEmerald)
                                    Text(
                                        text = BusinessOsViewModel.formatMoney(sup.estimatedValueCents, currency),
                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                        color = StatusWarning
                                    )
                                }
                                Text(sup.companyName, style = MaterialTheme.typography.titleLarge)
                                Text("${sup.contactName} • ${sup.email} • ${sup.phone}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(sup.historyLog, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Add Product or Service
    if (showAddProdDialog) {
        var kind by remember { mutableStateOf("PRODUCT") }
        var sku by remember { mutableStateOf("") }
        var name by remember { mutableStateOf("") }
        var cat by remember { mutableStateOf("Matériel & Licences") }
        var priceStr by remember { mutableStateOf("") }
        var costStr by remember { mutableStateOf("") }
        var stockStr by remember { mutableStateOf("10") }
        var alertStr by remember { mutableStateOf("3") }
        var supplier by remember { mutableStateOf("DataCenter KamerHost SARL") }
        var warehouse by remember { mutableStateOf("Entrepôt Central") }

        AlertDialog(
            onDismissRequest = { showAddProdDialog = false },
            title = { Text("Nouveau Produit ou Service") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = kind == "PRODUCT", onClick = { kind = "PRODUCT" }, label = { Text("Produit Stockable") })
                        FilterChip(selected = kind == "SERVICE", onClick = { kind = "SERVICE" }, label = { Text("Prestation / Service") })
                    }
                    OutlinedTextField(value = sku, onValueChange = { sku = it }, label = { Text("Code SKU / Référence") }, singleLine = true)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Désignation") }, singleLine = true)
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Prix de Vente HT ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = costStr,
                        onValueChange = { costStr = it },
                        label = { Text("Coût de Revient / Achat ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    if (kind == "PRODUCT") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = stockStr,
                                onValueChange = { stockStr = it },
                                label = { Text("Stock Initial") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = alertStr,
                                onValueChange = { alertStr = it },
                                label = { Text("Seuil Alerte") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank()) {
                        onAddProduct(
                            sku, name, kind, cat,
                            priceStr.toDoubleOrNull() ?: 0.0,
                            costStr.toDoubleOrNull() ?: 0.0,
                            stockStr.toIntOrNull() ?: 0,
                            alertStr.toIntOrNull() ?: 2,
                            supplier, warehouse
                        )
                        showAddProdDialog = false
                    }
                }) { Text("Ajouter au Catalogue") }
            },
            dismissButton = {
                TextButton(onClick = { showAddProdDialog = false }) { Text("Annuler") }
            }
        )
    }

    // Dialog: Stock Movement IN / OUT
    productForMovement?.let { targetProd ->
        var moveType by remember { mutableStateOf("IN") }
        var qtyStr by remember { mutableStateOf("5") }
        var reason by remember { mutableStateOf("Réapprovisionnement fournisseur") }

        AlertDialog(
            onDismissRequest = { productForMovement = null },
            title = { Text("Mouvement de Stock : ${targetProd.sku}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(targetProd.name, style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = moveType == "IN", onClick = { moveType = "IN" }, label = { Text("Entrée Stock (+)") })
                        FilterChip(selected = moveType == "OUT", onClick = { moveType = "OUT" }, label = { Text("Sortie Livraison (-)") })
                    }
                    OutlinedTextField(
                        value = qtyStr,
                        onValueChange = { qtyStr = it },
                        label = { Text("Quantité") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Motif / Bon de Livraison") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val q = qtyStr.toIntOrNull() ?: 0
                    if (q > 0) {
                        onRecordStockMovement(targetProd, moveType, q, reason)
                        productForMovement = null
                    }
                }) { Text("Valider Mouvement") }
            },
            dismissButton = {
                TextButton(onClick = { productForMovement = null }) { Text("Annuler") }
            }
        )
    }

    // Dialog: Add Supplier
    if (showAddSupplierDialog) {
        var company by remember { mutableStateOf("") }
        var contact by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("+237 ") }
        var volumeStr by remember { mutableStateOf("1500000") }

        AlertDialog(
            onDismissRequest = { showAddSupplierDialog = false },
            title = { Text("Référencer un Fournisseur") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = company, onValueChange = { company = it }, label = { Text("Société Fournisseur") }, singleLine = true)
                    OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Contact Commercial") }, singleLine = true)
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true)
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Téléphone") }, singleLine = true)
                    OutlinedTextField(
                        value = volumeStr,
                        onValueChange = { volumeStr = it },
                        label = { Text("Volume d'Achats Annuel Estimé ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (company.isNotBlank()) {
                        onAddSupplier(company, contact, email, phone, volumeStr.toDoubleOrNull() ?: 0.0)
                        showAddSupplierDialog = false
                    }
                }) { Text("Enregistrer") }
            },
            dismissButton = {
                TextButton(onClick = { showAddSupplierDialog = false }) { Text("Annuler") }
            }
        )
    }
}
