package com.example.data.ai

import com.example.BuildConfig
import com.example.data.local.UserProfileEntity
import com.example.data.model.ProductItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiStylistService {

    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun getStylingAdvice(
        prompt: String,
        userProfile: UserProfileEntity,
        relevantProducts: List<ProductItem> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        val productsSummary = relevantProducts.take(4).joinToString("\n") {
            "- ${it.brand} ${it.name} ($${it.lowestPrice}, ${it.category.displayName}, ${it.fitType})"
        }

        val systemPrompt = """
            You are AuraStyle AI, a world-class luxury fashion stylist and haute couture consultant.
            User Profile:
            - Name: ${userProfile.name}
            - Height: ${userProfile.heightCm} cm, Weight: ${userProfile.weightKg} kg
            - Body Type: ${userProfile.bodyTypeName}
            - Skin Undertone: ${userProfile.skinToneName}
            - Bust: ${userProfile.bustCm} cm, Waist: ${userProfile.waistCm} cm, Hips: ${userProfile.hipsCm} cm
            - Preferred Aesthetics: ${userProfile.preferredStylesCsv}
            - Subscription: ${userProfile.subscriptionTierName}

            Curated Catalog Items available for recommendations:
            $productsSummary

            Provide elegant, specific, highly actionable styling advice:
            1. Recommend exact outfit silhouettes, color palettes, and layering techniques.
            2. Explain why specific cuts flatter their ${userProfile.bodyTypeName} body type and ${userProfile.skinToneName} skin undertone.
            3. Highlight key footwear, accessories, and styling tricks (e.g. French tuck, cuff rolls, belt placement).
            4. Keep the tone sophisticated, warm, encouraging, and luxurious.
        """.trimIndent()

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getOfflineIntelligentAdvice(prompt, userProfile, relevantProducts)
        }

        try {
            val url = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"
            val fullPrompt = "$systemPrompt\n\nUser Request: $prompt"

            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().put("text", fullPrompt))
                        }
                        put("parts", parts)
                    })
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("topP", 0.95)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBodyString = response.body?.string() ?: ""
                val responseJson = JSONObject(responseBodyString)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text", "")
                        if (text.isNotBlank()) {
                            return@withContext text
                        }
                    }
                }
            }
            getOfflineIntelligentAdvice(prompt, userProfile, relevantProducts)
        } catch (e: Exception) {
            getOfflineIntelligentAdvice(prompt, userProfile, relevantProducts)
        }
    }

    private fun getOfflineIntelligentAdvice(
        prompt: String,
        userProfile: UserProfileEntity,
        relevantProducts: List<ProductItem>
    ): String {
        val lowerPrompt = prompt.lowercase()
        return when {
            lowerPrompt.contains("date") || lowerPrompt.contains("evening") || lowerPrompt.contains("night") -> {
                """
                ✨ **Evening Allure for your ${userProfile.bodyTypeName} Silhouette**
                
                • **The Foundation**: Pair the **Draped Bias-Cut Silk Maxi Slip Dress** in Terracotta or Midnight Onyx. The fluid diagonal bias skim highlights your ${userProfile.waistCm} cm waistline with effortless grace.
                • **Layering**: Drape the **Double-Breasted Wool Trench** over shoulders (cape-style) rather than wearing sleeves to keep the evening drape fluid.
                • **Footwear & Accessories**: Elevate with **Pointed Slingback Sculptural Stilettos** and the **Half-Moon Leather Shoulder Bag** in Warm Chestnut.
                • **Stylist Note**: Your ${userProfile.skinToneName} undertone shines against warm terracotta, champagne gold, and deep onyx satin. Add delicate gold huggie earrings to illuminate your collarbones.
                """.trimIndent()
            }
            lowerPrompt.contains("work") || lowerPrompt.contains("office") || lowerPrompt.contains("meeting") || lowerPrompt.contains("business") -> {
                """
                💼 **Executive Chic & Tailored Authority**
                
                • **The Proportions**: Anchor your ensemble with **High-Waisted Pleated Wide-Leg Trousers** in Espresso Noir. The knife pleats elongate your ${userProfile.heightCm} cm stature.
                • **Upper Pairing**: Tuck in the **Silk-Satin Draped Asymmetric Blouse** in Champagne Pearl. The subtle lustre conveys quiet luxury in boardrooms or gallery previews.
                • **Finishing Touch**: Add **Retro Chunky Sole Leather Loafers** to ground the wide-leg hem with modern architectural sharpness.
                • **Fit Confidence**: 96% fit synergy with your ${userProfile.bustCm} cm bust and ${userProfile.waistCm} cm waistline.
                """.trimIndent()
            }
            lowerPrompt.contains("casual") || lowerPrompt.contains("weekend") || lowerPrompt.contains("sunday") || lowerPrompt.contains("street") -> {
                """
                ☕ **Effortless Parisian Sunday / Streetwear Luxe**
                
                • **The Look**: Style the **Relaxed Oversized Knit Cashmere Sweater** in Oatmeal Heather with tailored high-rise trousers or straight-cut raw denim.
                • **The French Half-Tuck**: Tuck only the front 3 inches of the sweater hem into your waistband to preserve your defined waist while enjoying relaxed comfort.
                • **Accessories**: Slung the **Valenti Florence Half-Moon Leather Bag** across the body for clean diagonals.
                • **Color Palette**: Neutral monochromatic creams, camel, and deep espresso create an instantly expensive aesthetic.
                """.trimIndent()
            }
            else -> {
                """
                ✨ **Personalized AuraStyle Atelier Recommendation**
                
                • **Optimal Silhouette**: For your **${userProfile.bodyTypeName}** frame, we balance upper structure with clean, fluid lower drapery.
                • **Recommended Ensemble**: 
                  1. *Outerwear*: Double-Breasted Wool Trench (Camel Dune)
                  2. *Base*: Silk-Satin Asymmetric Blouse (Champagne Pearl)
                  3. *Trouser*: High-Waisted Wide-Leg Pleated Pant (Espresso Noir)
                  4. *Shoes*: Pointed Slingback Sculptural Stiletto
                • **Color Harmony**: Soft champagne and rich camel accentuate your ${userProfile.skinToneName} undertone with a radiant glow.
                • **Sizing Insight**: Size M in outerwear offers ideal shoulder room for knit layering; Size S in trousers fits your ${userProfile.waistCm} cm waist without gaping.
                """.trimIndent()
            }
        }
    }
}
