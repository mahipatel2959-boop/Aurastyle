package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ai.SizeAdvisorEngine
import com.example.data.model.ColorOption
import com.example.data.model.ProductItem
import com.example.data.model.StorePriceOffer
import com.example.ui.FashionViewModel
import com.example.ui.components.PriceComparisonTable
import com.example.ui.components.ProductCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: ProductItem,
    viewModel: FashionViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val fitAdvice = remember(product, userProfile) {
        SizeAdvisorEngine.calculateFitAdvice(product, userProfile)
    }

    var selectedSize by remember { mutableStateOf(fitAdvice.recommendedSize) }
    var selectedColor by remember { mutableStateOf(product.availableColors.firstOrNull()?.name ?: "") }
    var selectedStoreOffer by remember {
        mutableStateOf(product.storeOffers.minByOrNull { it.price } ?: product.storeOffers.first())
    }

    val complementaryItems = remember(product) {
        viewModel.products.filter { it.id != product.id }.take(3)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = product.brand.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GoldDark,
                        letterSpacing = 1.5.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showPriceAlertDialog(true) }) {
                        Icon(imageVector = Icons.Outlined.NotificationsActive, contentDescription = "Price Alert", tint = GoldDark)
                    }
                    IconButton(onClick = { viewModel.sendToTryOn(product) }) {
                        Icon(imageVector = Icons.Default.Checkroom, contentDescription = "Virtual Try-On", tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 12.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Try-on CTA
                    OutlinedButton(
                        onClick = { viewModel.sendToTryOn(product) },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GoldDark),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldDark),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("detail_try_on_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Checkroom, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("3D Try-On", fontWeight = FontWeight.Bold)
                    }

                    // Add to cart with lowest store offer
                    Button(
                        onClick = {
                            viewModel.addToCart(product, selectedSize, selectedColor, selectedStoreOffer)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = NoirObsidian
                        ),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(50.dp)
                            .testTag("detail_add_to_cart_btn")
                    ) {
                        Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "Buy on ${selectedStoreOffer.storeName}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "$${selectedStoreOffer.price.toInt()}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Product Hero Image
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (product.fallbackDrawableRes != 0) {
                        Image(
                            painter = painterResource(id = product.fallbackDrawableRes),
                            contentDescription = product.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        AsyncImage(
                            model = product.imageUrl,
                            contentDescription = product.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Floating Price Deal Tag
                    if (product.priceDropPercent > 0) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = DealRed,
                            contentColor = Color.White
                        ) {
                            Text(
                                text = "SAVE ${product.priceDropPercent}% ACROSS STORES",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // Title & Price Summary
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "$${product.lowestPrice.toInt()}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = SageSavingsGreen
                        )
                        if (product.originalPrice > product.lowestPrice) {
                            Text(
                                text = "$${product.originalPrice.toInt()}",
                                style = MaterialTheme.typography.titleLarge,
                                textDecoration = TextDecoration.LineThrough,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BestPriceBg,
                            contentColor = BestPriceBadge
                        ) {
                            Text(
                                text = "Best Price on ${product.storeOffers.minByOrNull { it.price }?.storeName}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // AI Size & Body Fit Advisor Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("ai_size_advisor_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = GoldLight.copy(alpha = 0.2f)
                    ),
                    border = BorderStroke(1.5.dp, GoldPrimary)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = GoldDark
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI Size & Silhouette Advisor",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldDark
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = GoldPrimary,
                                contentColor = NoirObsidian
                            ) {
                                Text(
                                    text = "${fitAdvice.confidencePercent}% Match",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Recommended: Size ${fitAdvice.recommendedSize}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = fitAdvice.fitSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = GoldPrimary.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Bust / Chest Drape",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldDark,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = fitAdvice.bustFit,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Fabric Stretch",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldDark,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = fitAdvice.stretchFactor,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Stylist Flattery Tip: ${fitAdvice.stylingTip}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Size Selector
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Size",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { viewModel.showFitProfileEditor(true) }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Measurements", fontSize = 12.sp, color = GoldDark)
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        product.availableSizes.forEach { size ->
                            val isSelected = selectedSize == size
                            val isRecommended = size == fitAdvice.recommendedSize

                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedSize = size },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.surface,
                                contentColor = if (isSelected) NoirObsidian else MaterialTheme.colorScheme.onSurface,
                                border = BorderStroke(
                                    width = if (isRecommended) 2.dp else 1.dp,
                                    color = if (isSelected) GoldPrimary else if (isRecommended) GoldDark else MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = size, fontWeight = FontWeight.Bold)
                                        if (isRecommended) {
                                            Text(
                                                text = "AI FIT",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isSelected) NoirObsidian else GoldDark
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Color Selector
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "Color: $selectedColor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        product.availableColors.forEach { colorOpt ->
                            val isSelected = selectedColor == colorOpt.name
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorOpt.hexColor))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) GoldPrimary else Color.Gray,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = colorOpt.name }
                            )
                        }
                    }
                }
            }

            // Multi-Store Price Comparison Table
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    PriceComparisonTable(
                        storeOffers = product.storeOffers,
                        originalPrice = product.originalPrice,
                        onStoreSelected = { offer ->
                            selectedStoreOffer = offer
                        }
                    )
                }
            }

            // Description & Fabric Details
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Garment Details & Fabric",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = product.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Composition: ${product.fabricComposition}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Fit Cut: ${product.fitType}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Complementary Pieces / Style This Item
            item {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        text = "Complete The Ensemble",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(complementaryItems) { compItem ->
                            ProductCard(
                                product = compItem,
                                onClick = { viewModel.selectProduct(compItem) },
                                onTryOn = { viewModel.sendToTryOn(compItem) },
                                modifier = Modifier.width(200.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
