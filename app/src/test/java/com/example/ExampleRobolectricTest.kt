package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.SizeAdvisorEngine
import com.example.data.ai.TryOnCompositor
import com.example.data.local.FashionDao
import com.example.data.local.FashionRepository
import com.example.data.local.UserProfileEntity
import com.example.data.model.BodyType
import com.example.data.model.ProductCategory
import com.example.data.model.ProductItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AuraStyle", appName)
    }

    @Test
    fun `test size advisor engine recommendation`() {
        val user = UserProfileEntity(
            bustCm = 88,
            waistCm = 68,
            hipsCm = 94,
            bodyTypeName = "HOURGLASS"
        )
        val dummyProduct = ProductItem(
            id = "test_1",
            name = "Silk Blouse",
            brand = "Studio",
            category = ProductCategory.TOPS,
            lowestPrice = 80.0,
            highestPrice = 120.0,
            originalPrice = 120.0,
            rating = 4.8f,
            reviewCount = 10,
            storeOffers = emptyList(),
            imageUrl = "",
            description = "Silk blouse",
            fabricComposition = "Silk",
            fitType = "Relaxed",
            availableSizes = listOf("XS", "S", "M", "L"),
            availableColors = emptyList(),
            aestheticTags = listOf("Quiet Luxury"),
            bodyTypeFlattery = emptyMap()
        )
        val advice = SizeAdvisorEngine.calculateFitAdvice(dummyProduct, user)
        assertEquals("S", advice.recommendedSize)
        assertTrue(advice.confidencePercent >= 90)
    }

    @Test
    fun `test try on compositor scoring`() {
        val user = UserProfileEntity()
        val dummyProduct = ProductItem(
            id = "test_2",
            name = "Wool Trench",
            brand = "Studio",
            category = ProductCategory.OUTERWEAR,
            lowestPrice = 200.0,
            highestPrice = 300.0,
            originalPrice = 300.0,
            rating = 4.9f,
            reviewCount = 15,
            storeOffers = emptyList(),
            imageUrl = "",
            description = "Trench",
            fabricComposition = "Wool",
            fitType = "Tailored",
            availableSizes = listOf("S", "M", "L"),
            availableColors = emptyList(),
            aestheticTags = listOf("Old Money"),
            bodyTypeFlattery = emptyMap()
        )
        val analysis = TryOnCompositor.analyzeLook(listOf(dummyProduct), user)
        assertTrue(analysis.harmonyScore in 75..100)
        assertNotNull(analysis.stylistVerdict)
    }
}

