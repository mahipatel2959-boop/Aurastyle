package com.example

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.data.model.ColorOption
import com.example.data.model.ProductCategory
import com.example.data.model.ProductItem
import com.example.data.model.StorePriceOffer
import com.example.ui.components.ProductCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun aura_product_card_screenshot() {
        val sampleProduct = ProductItem(
            id = "prod_001",
            name = "Structured Double-Breasted Wool Trench",
            brand = "Maison Margaux",
            category = ProductCategory.OUTERWEAR,
            lowestPrice = 189.0,
            highestPrice = 295.0,
            originalPrice = 320.0,
            rating = 4.9f,
            reviewCount = 142,
            storeOffers = listOf(
                StorePriceOffer("Nordstrom", 189.0, 320.0, 40, "Free 2-day delivery", "In Stock", true),
                StorePriceOffer("Farfetch", 225.0, 320.0, 30, "$15 shipping", "In Stock", false)
            ),
            imageUrl = "",
            description = "Heavy wool trench coat.",
            fabricComposition = "85% Virgin Wool",
            fitType = "Tailored Modern Drop",
            availableSizes = listOf("XS", "S", "M", "L"),
            availableColors = listOf(ColorOption("Camel Dune", 0xFFC29B62)),
            aestheticTags = listOf("Quiet Luxury", "Old Money"),
            bodyTypeFlattery = emptyMap(),
            priceDropPercent = 40
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                ProductCard(
                    product = sampleProduct,
                    onClick = {},
                    onTryOn = {},
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
    }
}

