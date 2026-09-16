package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.*
import com.example.ui.FashionViewModel
import com.example.ui.TryOnState
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VirtualTryOnScreen(
    viewModel: FashionViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val tryOn = uiState.tryOnState

    var showSlotPickerCategory by remember { mutableStateOf<ProductCategory?>(null) }
    var outfitNameInput by remember { mutableStateOf("Autumn Gala Chic") }
    var showSaveDialog by remember { mutableStateOf(false) }

    val activeItems = remember(tryOn) {
        listOfNotNull(tryOn.outerwearItem, tryOn.topItem, tryOn.bottomItem, tryOn.shoesItem, tryOn.accessoryItem)
    }
    val totalLowestPrice = activeItems.sumOf { it.lowestPrice }
    val totalOriginalPrice = activeItems.sumOf { it.originalPrice }
    val totalSavings = (totalOriginalPrice - totalLowestPrice).coerceAtLeast(0.0)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("virtual_try_on_screen"),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Checkroom,
                            contentDescription = null,
                            tint = GoldDark,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "VIRTUAL TRY-ON STUDIO",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = GoldDark,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "Real-Time 3D Silhouette & Flattery Engine",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GoldLight,
                    contentColor = GoldDark
                ) {
                    Text(
                        text = "AI Drape Active",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Avatar Customizer Strip
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Model Mannequin Settings",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Gender & Body Type Selectors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Body Type Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(BodyType.values()) { bType ->
                                val isSelected = tryOn.selectedBodyType == bType
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            viewModel.updateTryOnAvatar(tryOn.selectedGender, bType, tryOn.selectedSkinTone)
                                        },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) NoirObsidian else MaterialTheme.colorScheme.onSurfaceVariant
                                ) {
                                    Text(
                                        text = bType.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Skin Undertone Swatches
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Skin Undertone:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        SkinTone.values().forEach { sTone ->
                            val isSelected = tryOn.selectedSkinTone == sTone
                            val swatchColor = when (sTone) {
                                SkinTone.FAIR_COOL -> Color(0xFFFBE4D8)
                                SkinTone.FAIR_WARM -> Color(0xFFF5D6BA)
                                SkinTone.MEDIUM_NEUTRAL -> Color(0xFFDEB887)
                                SkinTone.OLIVE -> Color(0xFFC8A165)
                                SkinTone.TAN_GOLDEN -> Color(0xFFB87D4B)
                                SkinTone.DEEP_RICH -> Color(0xFF5A3825)
                            }
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(swatchColor)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 0.5.dp,
                                        color = if (isSelected) GoldPrimary else Color.Gray,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        viewModel.updateTryOnAvatar(tryOn.selectedGender, tryOn.selectedBodyType, sTone)
                                    }
                            )
                        }
                    }
                }
            }
        }

        // Interactive Virtual Studio Canvas & Slot Grid
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NoirDarkCard),
                border = BorderStroke(1.5.dp, GoldPrimary.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Mannequin Display Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF1E1B24), Color(0xFF100F14))
                                )
                            )
                    ) {
                        // Background model silhouette
                        Image(
                            painter = painterResource(id = R.drawable.img_avatar_female_1787220782196),
                            contentDescription = "Virtual Mannequin",
                            modifier = Modifier
                                .fillMaxHeight()
                                .align(Alignment.Center)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )

                        // Overlaid Layer Slots summary
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NoirObsidian.copy(alpha = 0.85f),
                                border = BorderStroke(1.dp, GoldPrimary)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${tryOn.analysisResult?.harmonyScore ?: 96}% Harmony",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldLight
                                    )
                                }
                            }
                        }

                        // Bottom Look Summary
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(NoirObsidian.copy(alpha = 0.85f))
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${activeItems.size} Pieces Layered",
                                style = MaterialTheme.typography.bodySmall,
                                color = CashmereSilk,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Total Look: $${totalLowestPrice.toInt()} (Save $${totalSavings.toInt()})",
                                style = MaterialTheme.typography.labelSmall,
                                color = SageSavingsGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Garment Layer Slots (Tap to swap)",
                        style = MaterialTheme.typography.titleSmall,
                        color = CashmereSilk,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 5 Slot Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TryOnSlotCard(
                            title = "Outerwear",
                            product = tryOn.outerwearItem,
                            onSlotClick = { showSlotPickerCategory = ProductCategory.OUTERWEAR },
                            onRemove = { viewModel.removeTryOnSlot(ProductCategory.OUTERWEAR) },
                            modifier = Modifier.weight(1f)
                        )
                        TryOnSlotCard(
                            title = "Top",
                            product = tryOn.topItem,
                            onSlotClick = { showSlotPickerCategory = ProductCategory.TOPS },
                            onRemove = { viewModel.removeTryOnSlot(ProductCategory.TOPS) },
                            modifier = Modifier.weight(1f)
                        )
                        TryOnSlotCard(
                            title = "Bottom",
                            product = tryOn.bottomItem,
                            onSlotClick = { showSlotPickerCategory = ProductCategory.BOTTOMS },
                            onRemove = { viewModel.removeTryOnSlot(ProductCategory.BOTTOMS) },
                            modifier = Modifier.weight(1f)
                        )
                        TryOnSlotCard(
                            title = "Shoes",
                            product = tryOn.shoesItem,
                            onSlotClick = { showSlotPickerCategory = ProductCategory.SHOES },
                            onRemove = { viewModel.removeTryOnSlot(ProductCategory.SHOES) },
                            modifier = Modifier.weight(1f)
                        )
                        TryOnSlotCard(
                            title = "Bag",
                            product = tryOn.accessoryItem,
                            onSlotClick = { showSlotPickerCategory = ProductCategory.ACCESSORIES },
                            onRemove = { viewModel.removeTryOnSlot(ProductCategory.ACCESSORIES) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // AI Fit & Harmony Report Card
        tryOn.analysisResult?.let { analysis ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = GoldDark
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI Fit & Styling Critique",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = analysis.stylistVerdict,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Score metrics row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ScoreMetricPill(
                                label = "Silhouette Balance",
                                score = analysis.silhouetteBalanceScore,
                                modifier = Modifier.weight(1f)
                            )
                            ScoreMetricPill(
                                label = "Color Undertone",
                                score = analysis.colorHarmonyScore,
                                modifier = Modifier.weight(1f)
                            )
                            ScoreMetricPill(
                                label = "Occasion Synergy",
                                score = analysis.occasionScore,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Key Styling Strengths:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldDark
                        )
                        analysis.keyStrengths.forEach { str ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(text = "• ", fontWeight = FontWeight.Bold, color = GoldDark)
                                Text(text = str, style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Atelier Tips:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        analysis.stylingSuggestions.forEach { sug ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(text = "→ ", fontWeight = FontWeight.Bold, color = GoldPrimary)
                                Text(text = sug, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        // Actions: Buy Complete Look & Save Look
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.addAllTryOnToCart() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("buy_complete_look_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = NoirObsidian
                    ),
                    enabled = activeItems.isNotEmpty()
                ) {
                    Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Buy Complete ${activeItems.size}-Piece Look ($${totalLowestPrice.toInt()})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                OutlinedButton(
                    onClick = { showSaveDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_outfit_lookbook_btn"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GoldDark),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldDark),
                    enabled = activeItems.isNotEmpty()
                ) {
                    Icon(imageVector = Icons.Default.BookmarkAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Outfit to My Wardrobe Lookbook", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Garment Slot Picker Bottom Sheet / Dialog
    if (showSlotPickerCategory != null) {
        val cat = showSlotPickerCategory!!
        val availableForCat = remember(cat) { viewModel.products.filter { it.category == cat } }

        AlertDialog(
            onDismissRequest = { showSlotPickerCategory = null },
            title = {
                Text("Select ${cat.displayName}", fontWeight = FontWeight.Bold)
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableForCat) { product ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    viewModel.sendToTryOn(product)
                                    showSlotPickerCategory = null
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.LightGray)
                                ) {
                                    if (product.fallbackDrawableRes != 0) {
                                        Image(
                                            painter = painterResource(id = product.fallbackDrawableRes),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        AsyncImage(
                                            model = product.imageUrl,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${product.brand} • $${product.lowestPrice.toInt()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SageSavingsGreen
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSlotPickerCategory = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Save Outfit Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Look to Wardrobe", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Give your styled look a name:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = outfitNameInput,
                        onValueChange = { outfitNameInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveCurrentTryOnOutfit(outfitNameInput, "Occasion Styling")
                        showSaveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NoirObsidian)
                ) {
                    Text("Save Look", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TryOnSlotCard(
    title: String,
    product: ProductItem?,
    onSlotClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onSlotClick),
        shape = RoundedCornerShape(10.dp),
        color = if (product != null) NoirElevated else NoirObsidian,
        border = BorderStroke(1.dp, if (product != null) GoldPrimary else NoirBorder)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (product != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = GoldLight,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = CashmereSilk
                    )
                    Text(
                        text = "$${product.lowestPrice.toInt()}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SageSavingsGreen
                    )
                }

                // Remove button
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = Color.LightGray,
                        modifier = Modifier.size(12.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add $title",
                        tint = GoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = CashmereBorder
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreMetricPill(
    label: String,
    score: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$score%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (score >= 90) SageSavingsGreen else GoldDark
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
