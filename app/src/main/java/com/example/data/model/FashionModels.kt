package com.example.data.model

enum class BodyType(val displayName: String, val description: String, val idealSilhouettes: List<String>) {
    HOURGLASS("Hourglass", "Balanced bust and hips with a defined waist", listOf("Wrap Dresses", "High-Waist Trousers", "Belted Trench Coats", "Fitted Blazers")),
    RECTANGLE("Athletic / Rectangle", "Uniform silhouette with subtle waist definition", listOf("Oversized Tailoring", "Cropped Jackets", "Pleated Skirts", "Layered Knits")),
    PEAR("Pear / Triangle", "Hips wider than shoulders with defined waistline", listOf("Statement Tops", "A-Line Skirts", "Wide-Leg Trousers", "Structured Shoulders")),
    INVERTED_TRIANGLE("Inverted Triangle", "Broader shoulders with slimmer hips", listOf("V-Neck Tops", "Flared Pants", "Palazzo Trousers", "Soft Dropped Shoulders")),
    OVAL("Apple / Oval", "Fuller midsection with slender limbs and legs", listOf("Empire Waists", "Fluid Duster Coats", "Monochrome Sets", "Tunic Blouses"))
}

enum class SkinTone(val displayName: String, val undertone: String, val bestColors: List<String>, val bestColorsHex: List<Long>) {
    FAIR_COOL("Fair Porcelain", "Cool Undertone", listOf("Emerald", "Royal Navy", "Icy Berry", "Silver"), listOf(0xFF0F52BA, 0xFF50C878, 0xFF880085, 0xFFC0C0C0)),
    FAIR_WARM("Ivory Warm", "Warm Golden", listOf("Camel", "Peach", "Warm Coral", "Gold"), listOf(0xFFC19A6B, 0xFFFFDAB9, 0xFFFF7F50, 0xFFFFD700)),
    MEDIUM_NEUTRAL("Sand Medium", "Neutral Balance", listOf("Terracotta", "Olive Green", "Espresso", "Cream"), listOf(0xFFE2725B, 0xFF556B2F, 0xFF362B28, 0xFFFFFDD0)),
    OLIVE("Mediterranean Olive", "Olive / Warm", listOf("Burgundy", "Mustard Gold", "Deep Forest", "Rust"), listOf(0xFF800020, 0xFFFFDB58, 0xFF228B22, 0xFFB7410E)),
    TAN_GOLDEN("Golden Bronze", "Rich Warm", listOf("Cobalt", "Saffron Yellow", "Warm Amber", "Tangerine"), listOf(0xFF0047AB, 0xFFF4C430, 0xFFFFBF00, 0xFFF28500)),
    DEEP_RICH("Deep Espresso", "Cool / Neutral Rich", listOf("Fuchsia", "Electric Blue", "Pure White", "Metallic Gold"), listOf(0xFFFF00FF, 0xFF7DF9FF, 0xFFFFFFFF, 0xFFFFD700))
}

enum class StyleAesthetic(val displayName: String, val tag: String, val description: String) {
    QUIET_LUXURY("Quiet Luxury", "Old Money", "Timeless cashmere, impeccable tailoring, subtle neutral palettes"),
    STREETWEAR("Urban Luxe", "Streetwear", "Oversized silhouettes, premium sneakers, high-end graphic textures"),
    MINIMALIST("Modern Minimalist", "Minimalist", "Clean architectural cuts, monochrome neutrals, effortless drape"),
    PARISIAN("Parisian Chic", "Parisian", "Effortless elegance, structured trench coats, tailored denim, silk scarves"),
    COASTAL_RESORT("Riviera Resort", "Resort", "Linen shirts, breezy trousers, woven leather, sun-kissed textures"),
    NIGHT_GLAM("Atelier Gala", "Evening", "Draped satin, structured velvet, gold accents, show-stopping silhouettes")
}

enum class ProductCategory(val displayName: String) {
    ALL("All Pieces"),
    TOPS("Tops & Shirts"),
    OUTERWEAR("Jackets & Coats"),
    BOTTOMS("Trousers & Skirts"),
    DRESSES("Dresses & Sets"),
    SHOES("Footwear"),
    ACCESSORIES("Bags & Jewelry")
}

data class StorePriceOffer(
    val storeName: String,
    val price: Double,
    val originalPrice: Double,
    val discountPercent: Int,
    val shippingInfo: String,
    val stockStatus: String,
    val isBestPrice: Boolean = false,
    val promoCode: String? = null,
    val storeUrl: String = "https://example.com/shop"
)

data class SizeFitAdvice(
    val recommendedSize: String,
    val confidencePercent: Int,
    val fitSummary: String,
    val bustFit: String,
    val waistFit: String,
    val lengthFit: String,
    val stretchFactor: String,
    val stylingTip: String
)

data class ProductItem(
    val id: String,
    val name: String,
    val brand: String,
    val category: ProductCategory,
    val lowestPrice: Double,
    val highestPrice: Double,
    val originalPrice: Double,
    val rating: Float,
    val reviewCount: Int,
    val storeOffers: List<StorePriceOffer>,
    val imageUrl: String,
    val fallbackDrawableRes: Int = 0,
    val description: String,
    val fabricComposition: String,
    val fitType: String,
    val availableSizes: List<String>,
    val availableColors: List<ColorOption>,
    val aestheticTags: List<String>,
    val bodyTypeFlattery: Map<BodyType, String>,
    val isTrending: Boolean = false,
    val isNewArrival: Boolean = false,
    val priceDropPercent: Int = 0,
    val colorHex: Long = 0xFF222222
)

data class ColorOption(
    val name: String,
    val hexColor: Long
)

enum class SubscriptionTier(val title: String, val priceMonthly: String, val badge: String, val features: List<String>) {
    FREE("Aura Starter", "Free", "Free", listOf(
        "Basic Price Comparison across 10+ retailers",
        "Standard AI Sizing Predictor",
        "3 Virtual Try-Ons per day",
        "Community Style Feed"
    )),
    STYLE_PRO("Style Pro", "$14.99/mo", "Popular", listOf(
        "Unlimited AI Virtual Try-On Studio",
        "Instant Occasion & Capsule Outfit Architect",
        "Real-Time Price Drop Alerts & Automatic Coupons",
        "Seasonal Color & Skin Undertone Analysis",
        "Wardrobe Gap Optimizer"
    )),
    VIP_ATELIER("VIP Atelier", "$39.99/mo", "VIP Luxury", listOf(
        "Everything in Style Pro included",
        "1-on-1 Monthly Video Session with Certified Human Stylist",
        "Bespoke Handcrafted Lookbooks delivered weekly",
        "Exclusive Private Luxury Brand Sample Sale Invites",
        "Priority VIP 24/7 Personal Fashion Concierge"
    ))
}

data class ProfessionalStylist(
    val id: String,
    val name: String,
    val title: String,
    val experienceYears: Int,
    val rating: Float,
    val reviewsCount: Int,
    val bio: String,
    val specialties: List<String>,
    val hourlyRate: Double,
    val location: String,
    val avatarRes: Int = 0,
    val sampleLookbooksCount: Int = 18,
    val clientsStyled: Int = 420
)

data class TryOnSlotItem(
    val category: ProductCategory,
    val product: ProductItem?
)
