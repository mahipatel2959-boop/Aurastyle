package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.*
import com.example.ui.AppTab
import com.example.ui.FashionUiState
import com.example.ui.FashionViewModel
import com.example.ui.components.ProductCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: FashionViewModel,
    uiState: FashionUiState,
    onProductClick: (ProductItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val aesthetics = listOf("All", "Quiet Luxury", "Old Money", "Modern Minimalist", "Parisian Chic", "Date Night", "Streetwear")
    val filteredProducts = remember(uiState.selectedCategory, uiState.selectedAesthetic, uiState.searchQuery) {
        viewModel.products.filter { product ->
            val matchCat = uiState.selectedCategory == ProductCategory.ALL || product.category == uiState.selectedCategory
            val matchAesthetic = uiState.selectedAesthetic == "All" || product.aestheticTags.any { it.contains(uiState.selectedAesthetic, ignoreCase = true) }
            val matchSearch = uiState.searchQuery.isBlank() || product.name.contains(uiState.searchQuery, ignoreCase = true) || product.brand.contains(uiState.searchQuery, ignoreCase = true)
            matchCat && matchAesthetic && matchSearch
        }
    }

    val trendingDrops = remember { viewModel.products.filter { it.priceDropPercent >= 40 } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("home_screen_feed"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // App Header
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
                        text = "VogueAI",
                        style = MaterialTheme.typography.titleLarge,
                        color = MinimalVioletPrimary,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Smart Styling & Price Intelligence",
                        style = MaterialTheme.typography.bodySmall,
                        color = MinimalTextMuted
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MinimalBorderSubtle)
                            .clickable { viewModel.showFitProfileEditor(true) }
                            .testTag("open_fit_profile_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Fit Profile",
                            tint = MinimalTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.showCartSheet(true) },
                        modifier = Modifier.testTag("open_cart_btn")
                    ) {
                        val cartCount by viewModel.cartItems.collectAsState()
                        BadgedBox(
                            badge = {
                                if (cartCount.isNotEmpty()) {
                                    Badge(containerColor = MinimalVioletPrimary) {
                                        Text(text = "${cartCount.size}", color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingBag,
                                contentDescription = "Cart",
                                tint = MinimalTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Hero Minimalist Fitting Room Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MinimalVioletLight),
                border = BorderStroke(1.dp, MinimalVioletContainer)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_fashion_1787220763894),
                        contentDescription = "Fashion Hero",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MinimalVioletLight.copy(alpha = 0.45f),
                                        MinimalVioletDark.copy(alpha = 0.88f)
                                    ),
                                    startY = 40f
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MinimalVioletLight,
                            contentColor = MinimalVioletPrimary
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MinimalVioletPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "AI FITTING ROOM",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Virtual Try-On\nReady to View",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Light,
                            lineHeight = 26.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Compare prices across Zara, Farfetch, Nordstrom & ASOS",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { viewModel.setTab(AppTab.TRY_ON) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MinimalVioletPrimary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(50),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Checkroom,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Start 3D Try-On", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = { viewModel.setTab(AppTab.AI_OUTFITS) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
                                shape = RoundedCornerShape(50),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MinimalVioletLight
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("AI Stylist", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search trench coats, silk blouses, loafers...", fontSize = 14.sp, color = MinimalTextMuted) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = MinimalTextSecondary)
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = MinimalTextSecondary)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .testTag("search_bar_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MinimalVioletPrimary,
                    unfocusedBorderColor = MinimalBorder,
                    focusedContainerColor = CleanMinimalSurface,
                    unfocusedContainerColor = CleanMinimalSurface
                ),
                singleLine = true
            )
        }

        // Style Aesthetic Filters
        item {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                Text(
                    text = "Aesthetic Vibes",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MinimalTextPrimary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(aesthetics) { aesthetic ->
                        val isSelected = uiState.selectedAesthetic == aesthetic
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setAesthetic(aesthetic) },
                            label = {
                                Text(
                                    text = aesthetic,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MinimalVioletLight,
                                selectedLabelColor = MinimalVioletPrimary,
                                containerColor = CleanMinimalSurface,
                                labelColor = MinimalTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) MinimalVioletPrimary else MinimalBorder
                            ),
                            shape = RoundedCornerShape(50)
                        )
                    }
                }
            }
        }

        // Category Tabs
        item {
            ScrollableTabRow(
                selectedTabIndex = ProductCategory.values().indexOf(uiState.selectedCategory),
                edgePadding = 16.dp,
                divider = {},
                indicator = {},
                containerColor = Color.Transparent,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                ProductCategory.values().forEach { cat ->
                    val isSelected = uiState.selectedCategory == cat
                    Surface(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable { viewModel.setCategory(cat) },
                        color = if (isSelected) MinimalVioletPrimary else CleanMinimalSurface,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, if (isSelected) MinimalVioletPrimary else MinimalBorder)
                    ) {
                        Text(
                            text = cat.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MinimalTextSecondary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Flash Multi-Store Price Drop Banner
        if (trendingDrops.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = DealRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Major Multi-Store Price Drops",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Up to 50% Off",
                            style = MaterialTheme.typography.labelSmall,
                            color = DealRed,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(trendingDrops) { product ->
                            ProductCard(
                                product = product,
                                onClick = { onProductClick(product) },
                                onTryOn = { viewModel.sendToTryOn(product) },
                                modifier = Modifier.width(220.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section Title: Curated Catalog
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Curated For Your Body Profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${filteredProducts.size} pieces",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Product Items List (2 columns in pairs)
        items(filteredProducts.chunked(2)) { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProductCard(
                    product = pair[0],
                    onClick = { onProductClick(pair[0]) },
                    onTryOn = { viewModel.sendToTryOn(pair[0]) },
                    modifier = Modifier.weight(1f)
                )

                if (pair.size > 1) {
                    ProductCard(
                        product = pair[1],
                        onClick = { onProductClick(pair[1]) },
                        onTryOn = { viewModel.sendToTryOn(pair[1]) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
