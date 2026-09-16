package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.BodyType
import com.example.data.model.SkinTone
import com.example.ui.FashionViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyClosetAndProfileScreen(
    viewModel: FashionViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val priceAlerts by viewModel.priceAlerts.collectAsState()
    val savedOutfits by viewModel.savedOutfits.collectAsState()

    var editName by remember { mutableStateOf(userProfile.name) }
    var editHeight by remember { mutableStateOf(userProfile.heightCm.toString()) }
    var editWeight by remember { mutableStateOf(userProfile.weightKg.toString()) }
    var editBust by remember { mutableStateOf(userProfile.bustCm.toString()) }
    var editWaist by remember { mutableStateOf(userProfile.waistCm.toString()) }
    var editHips by remember { mutableStateOf(userProfile.hipsCm.toString()) }
    var editBodyType by remember {
        mutableStateOf(try { BodyType.valueOf(userProfile.bodyTypeName) } catch (e: Exception) { BodyType.HOURGLASS })
    }
    var editSkinTone by remember {
        mutableStateOf(try { SkinTone.valueOf(userProfile.skinToneName) } catch (e: Exception) { SkinTone.MEDIUM_NEUTRAL })
    }

    val totalCartPrice = cartItems.sumOf { it.price * it.quantity }
    val totalCartOriginal = cartItems.sumOf { it.originalPrice * it.quantity }
    val totalCartSavings = (totalCartOriginal - totalCartPrice).coerceAtLeast(0.0)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("my_closet_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MY ATELIER & FIT PROFILE",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GoldDark,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Body Silhouette, Multi-Store Cart & Price Alerts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { viewModel.showFitProfileEditor(true) },
                    modifier = Modifier.testTag("edit_profile_icon_btn")
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profile", tint = GoldDark)
                }
            }
        }

        // Fit Profile Overview Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, GoldPrimary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(GoldLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = GoldDark)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = userProfile.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${userProfile.heightCm} cm • ${userProfile.weightKg} kg",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GoldLight,
                            contentColor = GoldDark
                        ) {
                            Text(
                                text = userProfile.subscriptionTierName.replace("_", " "),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Measurements 3-stat row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatMeasurementItem(label = "Bust", value = "${userProfile.bustCm} cm")
                        StatMeasurementItem(label = "Waist", value = "${userProfile.waistCm} cm")
                        StatMeasurementItem(label = "Hips", value = "${userProfile.hipsCm} cm")
                        StatMeasurementItem(label = "Silhouette", value = userProfile.bodyTypeName)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SageSavingsGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AI Size & Fit Engine calibrated with 96% confidence",
                            style = MaterialTheme.typography.labelSmall,
                            color = SageSavingsGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Multi-Store Cart Section
        item {
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Outlined.ShoppingBag, contentDescription = null, tint = GoldDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Multi-Store Unified Cart (${cartItems.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (cartItems.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearCart() }) {
                            Text("Clear", color = DealRed, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        if (cartItems.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Your shopping bag is empty", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Add items from the store comparison tables or Virtual Try-On studio.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(cartItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.brand.uppercase(), style = MaterialTheme.typography.labelSmall, color = GoldDark, fontWeight = FontWeight.Bold)
                            Text(text = item.productName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Size: ${item.selectedSize} • Color: ${item.selectedColor} • Store: ${item.storeName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$${item.price.toInt()}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SageSavingsGreen
                            )
                            if (item.originalPrice > item.price) {
                                Text(
                                    text = "$${item.originalPrice.toInt()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    textDecoration = TextDecoration.LineThrough,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { viewModel.removeCartItem(item.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Remove", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Checkout Summary Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldLight.copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, GoldPrimary)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Estimated Value:")
                            Text("$${totalCartOriginal.toInt()}", textDecoration = TextDecoration.LineThrough)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Multi-Store Best Deal Total:", fontWeight = FontWeight.Bold)
                            Text("$${totalCartPrice.toInt()}", fontWeight = FontWeight.ExtraBold, color = SageSavingsGreen)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Your Total Savings:", color = DealRed, fontWeight = FontWeight.Bold)
                            Text("-$${totalCartSavings.toInt()}", color = DealRed, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.clearCart() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("checkout_order_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = NoirObsidian
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Place Multi-Store Order ($${totalCartPrice.toInt()})", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Active Price Alerts Section
        item {
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp)) {
                Text(
                    text = "Active Price Drop Trackers (${priceAlerts.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (priceAlerts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No active price alerts", style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = "Tap the bell icon on any product to get notified when prices drop.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(priceAlerts) { alert ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = alert.productName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Current: $${alert.currentLowestPrice.toInt()} • Target: $${alert.targetPrice.toInt()} (${alert.storeName})",
                                style = MaterialTheme.typography.bodySmall,
                                color = GoldDark
                            )
                        }
                        IconButton(onClick = { viewModel.removePriceAlert(alert.id) }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Delete", tint = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    // Edit Fit Profile Dialog
    if (uiState.showFitProfileEditor) {
        AlertDialog(
            onDismissRequest = { viewModel.showFitProfileEditor(false) },
            title = { Text("Customize Your Fit Profile", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Your Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editHeight,
                                onValueChange = { editHeight = it },
                                label = { Text("Height (cm)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = editWeight,
                                onValueChange = { editWeight = it },
                                label = { Text("Weight (kg)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editBust,
                                onValueChange = { editBust = it },
                                label = { Text("Bust (cm)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = editWaist,
                                onValueChange = { editWaist = it },
                                label = { Text("Waist (cm)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = editHips,
                                onValueChange = { editHips = it },
                                label = { Text("Hips (cm)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        Text("Body Silhouette Type:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        BodyType.values().forEach { bType ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { editBodyType = bType }
                                    .padding(vertical = 2.dp)
                            ) {
                                RadioButton(
                                    selected = editBodyType == bType,
                                    onClick = { editBodyType = bType }
                                )
                                Text(text = "${bType.displayName} (${bType.description})", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val h = editHeight.toIntOrNull() ?: 172
                        val w = editWeight.toIntOrNull() ?: 58
                        val b = editBust.toIntOrNull() ?: 88
                        val wst = editWaist.toIntOrNull() ?: 68
                        val hp = editHips.toIntOrNull() ?: 94
                        viewModel.updateUserProfile(
                            name = editName,
                            heightCm = h,
                            weightKg = w,
                            bustCm = b,
                            waistCm = wst,
                            hipsCm = hp,
                            bodyType = editBodyType,
                            skinTone = editSkinTone,
                            preferredStyles = userProfile.preferredStylesCsv
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NoirObsidian)
                ) {
                    Text("Save & Calibrate AI", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showFitProfileEditor(false) }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StatMeasurementItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
