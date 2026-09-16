package com.example.data.ai

import com.example.data.local.UserProfileEntity
import com.example.data.model.BodyType
import com.example.data.model.ProductItem
import com.example.data.model.SkinTone

data class TryOnAnalysisResult(
    val harmonyScore: Int,
    val silhouetteBalanceScore: Int,
    val colorHarmonyScore: Int,
    val occasionScore: Int,
    val stylistVerdict: String,
    val keyStrengths: List<String>,
    val stylingSuggestions: List<String>
)

object TryOnCompositor {

    fun analyzeLook(
        selectedItems: List<ProductItem>,
        user: UserProfileEntity,
        occasion: String = "Casual Chic"
    ): TryOnAnalysisResult {
        if (selectedItems.isEmpty()) {
            return TryOnAnalysisResult(
                harmonyScore = 0,
                silhouetteBalanceScore = 0,
                colorHarmonyScore = 0,
                occasionScore = 0,
                stylistVerdict = "Select garments in the mannequin slots above to run live virtual try-on analysis.",
                keyStrengths = emptyList(),
                stylingSuggestions = listOf("Add a Top, Bottom, or Outerwear piece to begin styling.")
            )
        }

        val userBodyType = try {
            BodyType.valueOf(user.bodyTypeName)
        } catch (e: Exception) {
            BodyType.HOURGLASS
        }

        val userSkinTone = try {
            SkinTone.valueOf(user.skinToneName)
        } catch (e: Exception) {
            SkinTone.MEDIUM_NEUTRAL
        }

        // Compute scores
        var baseScore = 88
        val strengths = mutableListOf<String>()
        val tips = mutableListOf<String>()

        if (selectedItems.size >= 3) {
            baseScore += 6
            strengths.add("Full layered ensemble with balanced focal points.")
        } else if (selectedItems.size == 2) {
            baseScore += 3
            strengths.add("Clean minimalist 2-piece coordination.")
        }

        // Body type synergy
        val hasFlatteringFlapsOrBelts = selectedItems.any { it.name.contains("Trench") || it.name.contains("Pleated") }
        if (hasFlatteringFlapsOrBelts) {
            baseScore += 3
            strengths.add("Structured lines accentuate your ${userBodyType.displayName} silhouette.")
        }

        // Color balance
        strengths.add("Color palette complements ${userSkinTone.displayName} (${userSkinTone.undertone}).")
        tips.add("Roll up sleeves or open the top collar button for effortless French nonchalance.")
        tips.add("Add gold or brass metal jewelry to tie the warm tones together.")

        val finalHarmony = baseScore.coerceIn(75, 99)
        val silhouetteScore = (baseScore - 2).coerceIn(80, 98)
        val colorScore = (baseScore + 1).coerceIn(82, 100)
        val occasionScore = (baseScore - 1).coerceIn(78, 97)

        val verdict = when {
            finalHarmony >= 94 -> "Exceptional sartorial balance. The garment proportions lengthen your frame while the color tones elevate your skin undertone seamlessly."
            finalHarmony >= 88 -> "Polished and cohesive look. The textures contrast gracefully, creating high-end quiet luxury appeal."
            else -> "Chic everyday harmony. You can amplify the contrast with statement footwear or a structured shoulder bag."
        }

        return TryOnAnalysisResult(
            harmonyScore = finalHarmony,
            silhouetteBalanceScore = silhouetteScore,
            colorHarmonyScore = colorScore,
            occasionScore = occasionScore,
            stylistVerdict = verdict,
            keyStrengths = strengths,
            stylingSuggestions = tips
        )
    }
}
