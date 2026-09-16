package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.local.FashionRepository
import com.example.ui.AppTab
import com.example.ui.FashionViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val database = remember { AppDatabase.getInstance(context) }
                val repository = remember { FashionRepository(database.fashionDao()) }
                val viewModel: FashionViewModel = viewModel {
                    FashionViewModel(repository)
                }

                AuraStyleApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuraStyleApp(
    viewModel: FashionViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var targetAlertPriceInput by remember { mutableStateOf("150.0") }

    // Toast message handling
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearToast()
        }
    }

    // Intercept back button if a product is selected
    BackHandler(enabled = uiState.selectedProduct != null) {
        viewModel.selectProduct(null)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (uiState.selectedProduct == null) {
                NavigationBar(
                    containerColor = CleanMinimalNav,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = uiState.currentTab == AppTab.SHOP,
                        onClick = { viewModel.setTab(AppTab.SHOP) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.currentTab == AppTab.SHOP) Icons.Filled.Storefront else Icons.Outlined.Storefront,
                                contentDescription = "Shop"
                            )
                        },
                        label = { Text("Shop", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MinimalVioletPrimary,
                            selectedTextColor = MinimalVioletPrimary,
                            indicatorColor = MinimalVioletLight,
                            unselectedIconColor = MinimalTextSecondary,
                            unselectedTextColor = MinimalTextSecondary
                        ),
                        modifier = Modifier.testTag("tab_shop")
                    )

                    NavigationBarItem(
                        selected = uiState.currentTab == AppTab.TRY_ON,
                        onClick = { viewModel.setTab(AppTab.TRY_ON) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.currentTab == AppTab.TRY_ON) Icons.Filled.Checkroom else Icons.Outlined.Checkroom,
                                contentDescription = "Try-On"
                            )
                        },
                        label = { Text("3D Try-On", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MinimalVioletPrimary,
                            selectedTextColor = MinimalVioletPrimary,
                            indicatorColor = MinimalVioletLight,
                            unselectedIconColor = MinimalTextSecondary,
                            unselectedTextColor = MinimalTextSecondary
                        ),
                        modifier = Modifier.testTag("tab_try_on")
                    )

                    NavigationBarItem(
                        selected = uiState.currentTab == AppTab.AI_OUTFITS,
                        onClick = { viewModel.setTab(AppTab.AI_OUTFITS) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.currentTab == AppTab.AI_OUTFITS) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                                contentDescription = "AI Stylist"
                            )
                        },
                        label = { Text("AI Stylist", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MinimalVioletPrimary,
                            selectedTextColor = MinimalVioletPrimary,
                            indicatorColor = MinimalVioletLight,
                            unselectedIconColor = MinimalTextSecondary,
                            unselectedTextColor = MinimalTextSecondary
                        ),
                        modifier = Modifier.testTag("tab_ai_stylist")
                    )

                    NavigationBarItem(
                        selected = uiState.currentTab == AppTab.VIP_STYLISTS,
                        onClick = { viewModel.setTab(AppTab.VIP_STYLISTS) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.currentTab == AppTab.VIP_STYLISTS) Icons.Filled.Diamond else Icons.Outlined.Diamond,
                                contentDescription = "VIP Atelier"
                            )
                        },
                        label = { Text("VIP Atelier", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MinimalVioletPrimary,
                            selectedTextColor = MinimalVioletPrimary,
                            indicatorColor = MinimalVioletLight,
                            unselectedIconColor = MinimalTextSecondary,
                            unselectedTextColor = MinimalTextSecondary
                        ),
                        modifier = Modifier.testTag("tab_vip_stylists")
                    )

                    NavigationBarItem(
                        selected = uiState.currentTab == AppTab.CLOSET,
                        onClick = { viewModel.setTab(AppTab.CLOSET) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.currentTab == AppTab.CLOSET) Icons.Filled.Straighten else Icons.Outlined.Straighten,
                                contentDescription = "My Closet"
                            )
                        },
                        label = { Text("Fit & Bag", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MinimalVioletPrimary,
                            selectedTextColor = MinimalVioletPrimary,
                            indicatorColor = MinimalVioletLight,
                            unselectedIconColor = MinimalTextSecondary,
                            unselectedTextColor = MinimalTextSecondary
                        ),
                        modifier = Modifier.testTag("tab_my_closet")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.selectedProduct != null) {
                ProductDetailScreen(
                    product = uiState.selectedProduct!!,
                    viewModel = viewModel,
                    onBack = { viewModel.selectProduct(null) }
                )
            } else {
                AnimatedContent(
                    targetState = uiState.currentTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_transition"
                ) { tab ->
                    when (tab) {
                        AppTab.SHOP -> HomeScreen(
                            viewModel = viewModel,
                            uiState = uiState,
                            onProductClick = { product ->
                                viewModel.selectProduct(product)
                            }
                        )
                        AppTab.TRY_ON -> VirtualTryOnScreen(viewModel = viewModel)
                        AppTab.AI_OUTFITS -> OutfitStudioScreen(viewModel = viewModel)
                        AppTab.VIP_STYLISTS -> StylistSubscriptionScreen(viewModel = viewModel)
                        AppTab.CLOSET -> MyClosetAndProfileScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    // Price Alert Dialog
    if (uiState.showPriceAlertDialog && uiState.selectedProduct != null) {
        val product = uiState.selectedProduct!!
        AlertDialog(
            onDismissRequest = { viewModel.showPriceAlertDialog(false) },
            title = {
                Text(
                    text = "Track Price Drop",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "We will notify you the moment ${product.name} falls below your target price on any partner store (Zara, Nordstrom, Farfetch, etc.).",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Current Lowest: $${product.lowestPrice.toInt()}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SageSavingsGreen
                    )
                    OutlinedTextField(
                        value = targetAlertPriceInput,
                        onValueChange = { targetAlertPriceInput = it },
                        label = { Text("Your Target Price ($)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = targetAlertPriceInput.toDoubleOrNull() ?: (product.lowestPrice * 0.9)
                        viewModel.createPriceAlert(product, target, product.storeOffers.first().storeName)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NoirObsidian)
                ) {
                    Text("Set Alert", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showPriceAlertDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Cart Bottom Sheet
    if (uiState.showCartSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.showCartSheet(false) },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            MyClosetAndProfileScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

