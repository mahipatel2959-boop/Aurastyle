package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiStylistService
import com.example.data.ai.SizeAdvisorEngine
import com.example.data.ai.TryOnAnalysisResult
import com.example.data.ai.TryOnCompositor
import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    SHOP("Shop & Compare"),
    TRY_ON("Virtual Try-On"),
    AI_OUTFITS("AI Stylist"),
    VIP_STYLISTS("VIP Atelier"),
    CLOSET("My Closet & Fit")
}

data class TryOnState(
    val selectedGender: String = "Female",
    val selectedBodyType: BodyType = BodyType.HOURGLASS,
    val selectedSkinTone: SkinTone = SkinTone.MEDIUM_NEUTRAL,
    val selectedHeightCm: Int = 172,
    val topItem: ProductItem? = null,
    val outerwearItem: ProductItem? = null,
    val bottomItem: ProductItem? = null,
    val shoesItem: ProductItem? = null,
    val accessoryItem: ProductItem? = null,
    val analysisResult: TryOnAnalysisResult? = null,
    val isAnalyzing: Boolean = false
)

data class FashionUiState(
    val currentTab: AppTab = AppTab.SHOP,
    val selectedCategory: ProductCategory = ProductCategory.ALL,
    val searchQuery: String = "",
    val selectedAesthetic: String = "All",
    val selectedProduct: ProductItem? = null,
    val tryOnState: TryOnState = TryOnState(),
    val aiPrompt: String = "",
    val aiResponse: String = "",
    val isAiThinking: Boolean = false,
    val selectedStylist: ProfessionalStylist? = null,
    val showSubscriptionDialog: Boolean = false,
    val showBookingDialog: Boolean = false,
    val showPriceAlertDialog: Boolean = false,
    val showCartSheet: Boolean = false,
    val showFitProfileEditor: Boolean = false,
    val toastMessage: String? = null
)

class FashionViewModel(private val repository: FashionRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(FashionUiState())
    val uiState: StateFlow<FashionUiState> = _uiState.asStateFlow()

    val userProfile: StateFlow<UserProfileEntity> = repository.userProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserProfileEntity()
    )

    val savedOutfits: StateFlow<List<SavedOutfitEntity>> = repository.savedOutfits.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val cartItems: StateFlow<List<CartItemEntity>> = repository.cartItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val priceAlerts: StateFlow<List<PriceAlertEntity>> = repository.priceAlerts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val stylistBookings: StateFlow<List<StylistBookingEntity>> = repository.stylistBookings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val products: List<ProductItem> get() = repository.productsCatalog
    val stylists: List<ProfessionalStylist> get() = repository.professionalStylists

    init {
        // Initialize sample virtual try-on items
        val trench = repository.getProductById("prod_001")
        val blouse = repository.getProductById("prod_002")
        val trousers = repository.getProductById("prod_003")
        val heels = repository.getProductById("prod_005")
        val bag = repository.getProductById("prod_006")

        val initialTryOn = TryOnState(
            outerwearItem = trench,
            topItem = blouse,
            bottomItem = trousers,
            shoesItem = heels,
            accessoryItem = bag
        )
        val initialAnalysis = TryOnCompositor.analyzeLook(
            listOfNotNull(trench, blouse, trousers, heels, bag),
            UserProfileEntity()
        )
        _uiState.update {
            it.copy(
                tryOnState = initialTryOn.copy(analysisResult = initialAnalysis),
                selectedProduct = trench
            )
        }
    }

    fun setTab(tab: AppTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun setCategory(category: ProductCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setAesthetic(aesthetic: String) {
        _uiState.update { it.copy(selectedAesthetic = aesthetic) }
    }

    fun selectProduct(product: ProductItem?) {
        _uiState.update { it.copy(selectedProduct = product) }
    }

    fun sendToTryOn(product: ProductItem) {
        val currentTryOn = _uiState.value.tryOnState
        val updatedTryOn = when (product.category) {
            ProductCategory.TOPS -> currentTryOn.copy(topItem = product)
            ProductCategory.OUTERWEAR -> currentTryOn.copy(outerwearItem = product)
            ProductCategory.BOTTOMS -> currentTryOn.copy(bottomItem = product)
            ProductCategory.DRESSES -> currentTryOn.copy(topItem = product, bottomItem = null)
            ProductCategory.SHOES -> currentTryOn.copy(shoesItem = product)
            ProductCategory.ACCESSORIES -> currentTryOn.copy(accessoryItem = product)
            ProductCategory.ALL -> currentTryOn
        }

        val allItems = listOfNotNull(
            updatedTryOn.outerwearItem,
            updatedTryOn.topItem,
            updatedTryOn.bottomItem,
            updatedTryOn.shoesItem,
            updatedTryOn.accessoryItem
        )
        val analysis = TryOnCompositor.analyzeLook(allItems, userProfile.value)

        _uiState.update {
            it.copy(
                currentTab = AppTab.TRY_ON,
                tryOnState = updatedTryOn.copy(analysisResult = analysis),
                toastMessage = "Added ${product.name} to Virtual Try-On Studio!"
            )
        }
    }

    fun removeTryOnSlot(category: ProductCategory) {
        val currentTryOn = _uiState.value.tryOnState
        val updatedTryOn = when (category) {
            ProductCategory.TOPS -> currentTryOn.copy(topItem = null)
            ProductCategory.OUTERWEAR -> currentTryOn.copy(outerwearItem = null)
            ProductCategory.BOTTOMS -> currentTryOn.copy(bottomItem = null)
            ProductCategory.DRESSES -> currentTryOn.copy(topItem = null, bottomItem = null)
            ProductCategory.SHOES -> currentTryOn.copy(shoesItem = null)
            ProductCategory.ACCESSORIES -> currentTryOn.copy(accessoryItem = null)
            ProductCategory.ALL -> currentTryOn
        }

        val allItems = listOfNotNull(
            updatedTryOn.outerwearItem,
            updatedTryOn.topItem,
            updatedTryOn.bottomItem,
            updatedTryOn.shoesItem,
            updatedTryOn.accessoryItem
        )
        val analysis = TryOnCompositor.analyzeLook(allItems, userProfile.value)

        _uiState.update {
            it.copy(tryOnState = updatedTryOn.copy(analysisResult = analysis))
        }
    }

    fun updateTryOnAvatar(gender: String, bodyType: BodyType, skinTone: SkinTone) {
        val current = _uiState.value.tryOnState
        val allItems = listOfNotNull(
            current.outerwearItem,
            current.topItem,
            current.bottomItem,
            current.shoesItem,
            current.accessoryItem
        )
        val analysis = TryOnCompositor.analyzeLook(
            allItems,
            userProfile.value.copy(bodyTypeName = bodyType.name, skinToneName = skinTone.name)
        )
        _uiState.update {
            it.copy(
                tryOnState = current.copy(
                    selectedGender = gender,
                    selectedBodyType = bodyType,
                    selectedSkinTone = skinTone,
                    analysisResult = analysis
                )
            )
        }
    }

    fun saveCurrentTryOnOutfit(name: String, occasion: String) {
        viewModelScope.launch {
            val current = _uiState.value.tryOnState
            val items = listOfNotNull(
                current.outerwearItem,
                current.topItem,
                current.bottomItem,
                current.shoesItem,
                current.accessoryItem
            )
            val ids = items.joinToString(",") { it.id }
            val totalPrice = items.sumOf { it.lowestPrice }
            val score = current.analysisResult?.harmonyScore ?: 92
            val advice = current.analysisResult?.stylistVerdict ?: "Flattering luxury coordination."

            val outfit = SavedOutfitEntity(
                name = name.ifBlank { "Curated Look #${System.currentTimeMillis() % 1000}" },
                occasion = occasion,
                itemIdsCsv = ids,
                harmonyScore = score,
                styleVibe = "Quiet Luxury / Chic",
                aiStylistAdvice = advice,
                totalPrice = totalPrice,
                isFavorite = true
            )
            repository.saveOutfit(outfit)
            _uiState.update { it.copy(toastMessage = "Saved outfit '$name' to your Wardrobe!") }
        }
    }

    fun deleteOutfit(outfitId: Long) {
        viewModelScope.launch {
            repository.deleteOutfit(outfitId)
            _uiState.update { it.copy(toastMessage = "Outfit removed from Lookbook.") }
        }
    }


    fun addAllTryOnToCart() {
        viewModelScope.launch {
            val current = _uiState.value.tryOnState
            val items = listOfNotNull(
                current.outerwearItem,
                current.topItem,
                current.bottomItem,
                current.shoesItem,
                current.accessoryItem
            )
            items.forEach { product ->
                val bestOffer = product.storeOffers.minByOrNull { it.price } ?: product.storeOffers.first()
                val fit = SizeAdvisorEngine.calculateFitAdvice(product, userProfile.value)
                val colorName = product.availableColors.firstOrNull()?.name ?: "Standard"
                repository.addToCart(product, fit.recommendedSize, colorName, bestOffer)
            }
            _uiState.update {
                it.copy(
                    showCartSheet = true,
                    toastMessage = "Added ${items.size} pieces from Try-On Studio to Cart at lowest prices!"
                )
            }
        }
    }

    fun addToCart(product: ProductItem, size: String, color: String, storeOffer: StorePriceOffer) {
        viewModelScope.launch {
            repository.addToCart(product, size, color, storeOffer)
            _uiState.update {
                it.copy(toastMessage = "Added ${product.name} ($size, ${storeOffer.storeName}) to Cart!")
            }
        }
    }

    fun removeCartItem(id: Long) {
        viewModelScope.launch {
            repository.removeCartItem(id)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
            _uiState.update { it.copy(toastMessage = "Order simulated! Multi-store tracking active.") }
        }
    }

    fun createPriceAlert(product: ProductItem, targetPrice: Double, storeName: String) {
        viewModelScope.launch {
            repository.setPriceAlert(product, targetPrice, storeName)
            _uiState.update {
                it.copy(
                    showPriceAlertDialog = false,
                    toastMessage = "Price drop alert set for $${String.format("%.2f", targetPrice)} on ${product.name}!"
                )
            }
        }
    }

    fun removePriceAlert(id: Long) {
        viewModelScope.launch {
            repository.removePriceAlert(id)
        }
    }

    fun requestAiStyling(prompt: String) {
        if (prompt.isBlank()) return
        _uiState.update { it.copy(aiPrompt = prompt, isAiThinking = true) }
        viewModelScope.launch {
            val response = GeminiStylistService.getStylingAdvice(
                prompt = prompt,
                userProfile = userProfile.value,
                relevantProducts = repository.productsCatalog
            )
            _uiState.update {
                it.copy(
                    aiResponse = response,
                    isAiThinking = false
                )
            }
        }
    }

    fun updateUserProfile(
        name: String,
        heightCm: Int,
        weightKg: Int,
        bustCm: Int,
        waistCm: Int,
        hipsCm: Int,
        bodyType: BodyType,
        skinTone: SkinTone,
        preferredStyles: String
    ) {
        viewModelScope.launch {
            val current = userProfile.value
            val updated = current.copy(
                name = name,
                heightCm = heightCm,
                weightKg = weightKg,
                bustCm = bustCm,
                waistCm = waistCm,
                hipsCm = hipsCm,
                bodyTypeName = bodyType.name,
                skinToneName = skinTone.name,
                preferredStylesCsv = preferredStyles
            )
            repository.saveUserProfile(updated)
            _uiState.update {
                it.copy(
                    showFitProfileEditor = false,
                    toastMessage = "Fit Profile updated! AI sizing recalculations applied."
                )
            }
        }
    }

    fun upgradeSubscription(tier: SubscriptionTier) {
        viewModelScope.launch {
            val current = userProfile.value
            val updated = current.copy(subscriptionTierName = tier.name)
            repository.saveUserProfile(updated)
            _uiState.update {
                it.copy(
                    showSubscriptionDialog = false,
                    toastMessage = "Welcome to ${tier.title}! All premium styling perks unlocked."
                )
            }
        }
    }

    fun bookStylist(
        stylist: ProfessionalStylist,
        sessionType: String,
        date: String,
        timeSlot: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.bookStylist(stylist, sessionType, date, timeSlot, notes)
            _uiState.update {
                it.copy(
                    showBookingDialog = false,
                    selectedStylist = null,
                    toastMessage = "Consultation booked with ${stylist.name} for $date ($timeSlot)!"
                )
            }
        }
    }

    fun showSubscriptionDialog(show: Boolean) {
        _uiState.update { it.copy(showSubscriptionDialog = show) }
    }

    fun showBookingDialog(stylist: ProfessionalStylist?) {
        _uiState.update {
            it.copy(
                selectedStylist = stylist,
                showBookingDialog = stylist != null
            )
        }
    }

    fun showPriceAlertDialog(show: Boolean) {
        _uiState.update { it.copy(showPriceAlertDialog = show) }
    }

    fun showCartSheet(show: Boolean) {
        _uiState.update { it.copy(showCartSheet = show) }
    }

    fun showFitProfileEditor(show: Boolean) {
        _uiState.update { it.copy(showFitProfileEditor = show) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
