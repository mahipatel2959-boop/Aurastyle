package com.example.data.ai

import com.example.data.local.UserProfileEntity
import com.example.data.model.BodyType
import com.example.data.model.ProductCategory
import com.example.data.model.ProductItem
import com.example.data.model.SizeFitAdvice

object SizeAdvisorEngine {

    fun calculateFitAdvice(product: ProductItem, user: UserProfileEntity): SizeFitAdvice {
        val userBodyType = try {
            BodyType.valueOf(user.bodyTypeName)
        } catch (e: Exception) {
            BodyType.HOURGLASS
        }

        // Calculate size based on measurements & category
        val sizeBasedOnBust = when {
            user.bustCm < 84 -> "XS"
            user.bustCm in 84..90 -> "S"
            user.bustCm in 91..96 -> "M"
            user.bustCm in 97..104 -> "L"
            else -> "XL"
        }

        val sizeBasedOnWaistHips = when {
            user.waistCm < 66 || user.hipsCm < 90 -> "XS"
            user.waistCm in 66..72 && user.hipsCm in 90..96 -> "S"
            user.waistCm in 73..78 && user.hipsCm in 97..103 -> "M"
            user.waistCm in 79..86 && user.hipsCm in 104..111 -> "L"
            else -> "XL"
        }

        val recommendedSize = when (product.category) {
            ProductCategory.TOPS, ProductCategory.OUTERWEAR -> sizeBasedOnBust
            ProductCategory.BOTTOMS -> sizeBasedOnWaistHips
            ProductCategory.DRESSES -> {
                if (userBodyType == BodyType.PEAR) sizeBasedOnWaistHips else sizeBasedOnBust
            }
            ProductCategory.SHOES -> "EU 38"
            ProductCategory.ACCESSORIES, ProductCategory.ALL -> "Standard"
        }

        val confidence = when {
            product.category == ProductCategory.ACCESSORIES -> 99
            product.fitType.contains("Relaxed", ignoreCase = true) -> 97
            product.fitType.contains("Oversized", ignoreCase = true) -> 95
            product.fitType.contains("Tailored", ignoreCase = true) -> 94
            else -> 92
        }

        val flatteryNote = product.bodyTypeFlattery[userBodyType]
            ?: "The proportions of this garment enhance your natural silhouette."

        val stretchFactor = if (product.fabricComposition.contains("Elastane") || product.fabricComposition.contains("Spandex")) {
            "High Comfort Stretch (3-5% give)"
        } else if (product.fabricComposition.contains("Wool") || product.fabricComposition.contains("Silk")) {
            "Natural Breathable Drape (Structured with zero static)"
        } else {
            "Standard Woven Texture"
        }

        val summary = "Size $recommendedSize will flatter your ${userBodyType.displayName} silhouette with a ${product.fitType.lowercase()} drape."

        return SizeFitAdvice(
            recommendedSize = recommendedSize,
            confidencePercent = confidence,
            fitSummary = summary,
            bustFit = "Ideal contours around your ${user.bustCm} cm bustline without strain.",
            waistFit = "Sits naturally at your ${user.waistCm} cm waistline with clean ease.",
            lengthFit = "Proportioned gracefully for your ${user.heightCm} cm stature.",
            stretchFactor = stretchFactor,
            stylingTip = flatteryNote
        )
    }
}
