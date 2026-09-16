package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FashionDao {
    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserProfile(profile: UserProfileEntity)

    // Saved Outfits
    @Query("SELECT * FROM saved_outfits ORDER BY timestamp DESC")
    fun getAllSavedOutfits(): Flow<List<SavedOutfitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedOutfit(outfit: SavedOutfitEntity): Long

    @Query("DELETE FROM saved_outfits WHERE id = :outfitId")
    suspend fun deleteSavedOutfit(outfitId: Long)

    // Cart Items
    @Query("SELECT * FROM cart_items ORDER BY timestamp DESC")
    fun getAllCartItems(): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity): Long

    @Update
    suspend fun updateCartItem(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItem(id: Long)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    // Price Alerts
    @Query("SELECT * FROM price_alerts ORDER BY timestamp DESC")
    fun getAllPriceAlerts(): Flow<List<PriceAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceAlert(alert: PriceAlertEntity): Long

    @Query("DELETE FROM price_alerts WHERE id = :id")
    suspend fun deletePriceAlert(id: Long)

    // Stylist Bookings
    @Query("SELECT * FROM stylist_bookings ORDER BY timestamp DESC")
    fun getAllStylistBookings(): Flow<List<StylistBookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStylistBooking(booking: StylistBookingEntity): Long
}
