package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Sophia Laurent",
    val heightCm: Int = 172,
    val weightKg: Int = 58,
    val bustCm: Int = 88,
    val waistCm: Int = 68,
    val hipsCm: Int = 94,
    val bodyTypeName: String = "HOURGLASS",
    val skinToneName: String = "MEDIUM_NEUTRAL",
    val preferredStylesCsv: String = "Quiet Luxury,Minimalist,Parisian Chic",
    val subscriptionTierName: String = "STYLE_PRO",
    val tryOnCreditsLeft: Int = 999
)

@Entity(tableName = "saved_outfits")
data class SavedOutfitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val occasion: String,
    val itemIdsCsv: String,
    val harmonyScore: Int,
    val styleVibe: String,
    val aiStylistAdvice: String,
    val totalPrice: Double,
    val isFavorite: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: String,
    val productName: String,
    val brand: String,
    val selectedSize: String,
    val selectedColor: String,
    val storeName: String,
    val price: Double,
    val originalPrice: Double,
    val quantity: Int = 1,
    val imageUrl: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "price_alerts")
data class PriceAlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: String,
    val productName: String,
    val brand: String,
    val currentLowestPrice: Double,
    val targetPrice: Double,
    val storeName: String,
    val isTriggered: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "stylist_bookings")
data class StylistBookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val stylistId: String,
    val stylistName: String,
    val stylistTitle: String,
    val sessionType: String,
    val dateText: String,
    val timeSlot: String,
    val status: String = "Confirmed",
    val notes: String = "",
    val pricePaid: Double,
    val timestamp: Long = System.currentTimeMillis()
)
