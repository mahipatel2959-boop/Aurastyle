package com.example.data.local

import com.example.R
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FashionRepository(private val dao: FashionDao) {

    // Database access
    val userProfile: Flow<UserProfileEntity> = dao.getUserProfile().map { entity ->
        entity ?: UserProfileEntity()
    }

    val savedOutfits: Flow<List<SavedOutfitEntity>> = dao.getAllSavedOutfits()
    val cartItems: Flow<List<CartItemEntity>> = dao.getAllCartItems()
    val priceAlerts: Flow<List<PriceAlertEntity>> = dao.getAllPriceAlerts()
    val stylistBookings: Flow<List<StylistBookingEntity>> = dao.getAllStylistBookings()

    suspend fun saveUserProfile(profile: UserProfileEntity) {
        dao.insertOrUpdateUserProfile(profile)
    }

    suspend fun saveOutfit(outfit: SavedOutfitEntity): Long {
        return dao.insertSavedOutfit(outfit)
    }

    suspend fun deleteOutfit(outfitId: Long) {
        dao.deleteSavedOutfit(outfitId)
    }

    suspend fun addToCart(
        product: ProductItem,
        size: String,
        color: String,
        storeOffer: StorePriceOffer
    ) {
        dao.insertCartItem(
            CartItemEntity(
                productId = product.id,
                productName = product.name,
                brand = product.brand,
                selectedSize = size,
                selectedColor = color,
                storeName = storeOffer.storeName,
                price = storeOffer.price,
                originalPrice = storeOffer.originalPrice,
                quantity = 1,
                imageUrl = product.imageUrl
            )
        )
    }

    suspend fun removeCartItem(id: Long) {
        dao.deleteCartItem(id)
    }

    suspend fun clearCart() {
        dao.clearCart()
    }

    suspend fun setPriceAlert(product: ProductItem, targetPrice: Double, storeName: String) {
        dao.insertPriceAlert(
            PriceAlertEntity(
                productId = product.id,
                productName = product.name,
                brand = product.brand,
                currentLowestPrice = product.lowestPrice,
                targetPrice = targetPrice,
                storeName = storeName
            )
        )
    }

    suspend fun removePriceAlert(id: Long) {
        dao.deletePriceAlert(id)
    }

    suspend fun bookStylist(
        stylist: ProfessionalStylist,
        sessionType: String,
        date: String,
        timeSlot: String,
        notes: String
    ): Long {
        return dao.insertStylistBooking(
            StylistBookingEntity(
                stylistId = stylist.id,
                stylistName = stylist.name,
                stylistTitle = stylist.title,
                sessionType = sessionType,
                dateText = date,
                timeSlot = timeSlot,
                notes = notes,
                pricePaid = stylist.hourlyRate
            )
        )
    }

    // Curated catalog with rich store price offers and fashion styling metadata
    val productsCatalog: List<ProductItem> = listOf(
        ProductItem(
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
                StorePriceOffer("Nordstrom", 189.0, 320.0, 40, "Free 2-day delivery", "In Stock (Sizes S, M, L)", true, "STYLE20"),
                StorePriceOffer("Farfetch", 225.0, 320.0, 30, "$15 Express shipping", "Low Stock (3 left)", false),
                StorePriceOffer("ASOS Premier", 249.0, 320.0, 22, "Free shipping over $50", "In Stock (All sizes)", false, "ASOSNEW"),
                StorePriceOffer("SSENSE", 295.0, 320.0, 8, "Standard shipping $12", "In Stock", false)
            ),
            imageUrl = "https://images.unsplash.com/photo-1544022613-e87ca75a784a?auto=format&fit=crop&w=800&q=80",
            fallbackDrawableRes = R.drawable.img_hero_fashion_1787220763894,
            description = "Crafted in bespoke heavy Italian wool blend with architectural storm flaps, horn buttons, and a removable waist cinch belt for an elongated, commanding silhouette.",
            fabricComposition = "85% Virgin Wool, 15% Mulberry Silk Lining",
            fitType = "Tailored Modern Drop",
            availableSizes = listOf("XS", "S", "M", "L", "XL"),
            availableColors = listOf(
                ColorOption("Camel Dune", 0xFFC29B62),
                ColorOption("Noir Obsidian", 0xFF18171C),
                ColorOption("Sage Shadow", 0xFF606D5E)
            ),
            aestheticTags = listOf("Quiet Luxury", "Old Money", "Parisian Chic", "Office Chic"),
            bodyTypeFlattery = mapOf(
                BodyType.HOURGLASS to "Belt accentuates your natural waistline while structured lapels balance bust proportion perfectly.",
                BodyType.RECTANGLE to "Adds structured depth to shoulders and creates waist definition with the tie cinch.",
                BodyType.PEAR to "Broad peak lapels draw the gaze upward to balance hip width seamlessly.",
                BodyType.INVERTED_TRIANGLE to "A-line drape beneath the belt softens upper shoulder line.",
                BodyType.OVAL to "Wear unbelted for an elongated vertical streamlining effect."
            ),
            isTrending = true,
            isNewArrival = true,
            priceDropPercent = 40,
            colorHex = 0xFFC29B62
        ),
        ProductItem(
            id = "prod_002",
            name = "Silk-Satin Draped Asymmetric Blouse",
            brand = "L'Étoile Studio",
            category = ProductCategory.TOPS,
            lowestPrice = 88.0,
            highestPrice = 145.0,
            originalPrice = 160.0,
            rating = 4.8f,
            reviewCount = 98,
            storeOffers = listOf(
                StorePriceOffer("Zara Atelier", 88.0, 160.0, 45, "Free click & collect", "In Stock", true, "ZARA10"),
                StorePriceOffer("Revolve", 112.0, 160.0, 30, "Free 2-day delivery", "In Stock (XS, S, M)", false),
                StorePriceOffer("Nordstrom", 135.0, 160.0, 15, "Standard shipping", "In Stock", false),
                StorePriceOffer("Farfetch", 145.0, 160.0, 9, "Express courier $18", "Last 2 items", false)
            ),
            imageUrl = "https://images.unsplash.com/photo-1551803091-e20673f15770?auto=format&fit=crop&w=800&q=80",
            fallbackDrawableRes = R.drawable.img_avatar_female_1787220782196,
            description = "Fluid heavyweight mulberry silk with cowl neckline and subtle gathered shoulder detail that drapes effortlessly for candlelit dinners or gallery openings.",
            fabricComposition = "92% Pure Mulberry Silk, 8% Elastane",
            fitType = "Fluid Relaxed Drape",
            availableSizes = listOf("XS", "S", "M", "L"),
            availableColors = listOf(
                ColorOption("Champagne Pearl", 0xFFEEDBB2),
                ColorOption("Emerald Glow", 0xFF14532D),
                ColorOption("Rose Quartz", 0xFFDDA7A5)
            ),
            aestheticTags = listOf("Date Night", "Quiet Luxury", "Parisian Chic", "Night Glam"),
            bodyTypeFlattery = mapOf(
                BodyType.HOURGLASS to "Subtle fluid fall skims curves without cling.",
                BodyType.RECTANGLE to "Cowl neck creates dimension across chest and shoulders.",
                BodyType.PEAR to "Lustrous upper texture draws eyes to collarbones and neckline.",
                BodyType.INVERTED_TRIANGLE to "Soft cowl softens broad shoulder angles.",
                BodyType.OVAL to "Fluid drape creates an effortless elegant silhouette."
            ),
            isTrending = true,
            isNewArrival = false,
            priceDropPercent = 45,
            colorHex = 0xFFEEDBB2
        ),
        ProductItem(
            id = "prod_003",
            name = "High-Waisted Pleated Wide-Leg Trousers",
            brand = "Atelier Vesper",
            category = ProductCategory.BOTTOMS,
            lowestPrice = 95.0,
            highestPrice = 170.0,
            originalPrice = 180.0,
            rating = 4.9f,
            reviewCount = 210,
            storeOffers = listOf(
                StorePriceOffer("Uniqlo U Collab", 95.0, 180.0, 47, "Standard Free over $45", "In Stock (All sizes)", true, "UNIQLO5"),
                StorePriceOffer("COS", 130.0, 180.0, 28, "Free in-store returns", "In Stock (S, M, L)", false),
                StorePriceOffer("ASOS Edition", 145.0, 180.0, 19, "2-Day shipping", "In Stock", false),
                StorePriceOffer("Net-a-Porter", 170.0, 180.0, 5, "Luxury gift box shipping", "Low Stock", false)
            ),
            imageUrl = "https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?auto=format&fit=crop&w=800&q=80",
            description = "Double knife pleats with a continuous waistband and dramatic wide-leg hemline that creates endless leg lines with heels or pointed boots.",
            fabricComposition = "70% Fine Wool Gabardine, 28% Viscose, 2% Spandex",
            fitType = "High-Rise Wide Leg",
            availableSizes = listOf("XS", "S", "M", "L", "XL"),
            availableColors = listOf(
                ColorOption("Espresso Noir", 0xFF2A2321),
                ColorOption("Ivory Oat", 0xFFEAE5D9),
                ColorOption("Navy Midnight", 0xFF1E293B)
            ),
            aestheticTags = listOf("Old Money", "Modern Minimalist", "Office Chic", "Streetwear"),
            bodyTypeFlattery = mapOf(
                BodyType.HOURGLASS to "Fitted high waistline anchors hips while drape falls cleanly.",
                BodyType.RECTANGLE to "Knife pleats introduce dimensional movement and curvature.",
                BodyType.PEAR to "Wide leg provides a seamless line directly from hips to floor.",
                BodyType.INVERTED_TRIANGLE to "Wide legs balance broader upper silhouette harmoniously.",
                BodyType.OVAL to "High-rise waistband offers gentle core support and posture elongation."
            ),
            isTrending = true,
            isNewArrival = false,
            priceDropPercent = 47,
            colorHex = 0xFF2A2321
        ),
        ProductItem(
            id = "prod_004",
            name = "Draped Bias-Cut Silk Maxi Slip Dress",
            brand = "Solstice Paris",
            category = ProductCategory.DRESSES,
            lowestPrice = 140.0,
            highestPrice = 260.0,
            originalPrice = 280.0,
            rating = 4.95f,
            reviewCount = 312,
            storeOffers = listOf(
                StorePriceOffer("Revolve", 140.0, 280.0, 50, "Free 2-day express", "In Stock", true, "SUMMER50"),
                StorePriceOffer("Nordstrom", 175.0, 280.0, 37, "Free returns", "In Stock (XS, S, M)", false),
                StorePriceOffer("Farfetch", 210.0, 280.0, 25, "Express Worldwide", "In Stock", false),
                StorePriceOffer("SSENSE", 260.0, 280.0, 7, "Standard shipping", "Last 1 left", false)
            ),
            imageUrl = "https://images.unsplash.com/photo-1595777457583-95e059d581b8?auto=format&fit=crop&w=800&q=80",
            description = "Cut on the true bias to follow the natural contours of the body with a delicate scoop back and floor-skimming hemline. Layer with blazers or leather jackets.",
            fabricComposition = "100% Heavyweight Crepe Silk",
            fitType = "Body-Skimming Bias Drape",
            availableSizes = listOf("XS", "S", "M", "L"),
            availableColors = listOf(
                ColorOption("Terracotta Sunset", 0xFFC85A32),
                ColorOption("Midnight Onyx", 0xFF121118),
                ColorOption("Sage Pistachio", 0xFF8FA88B)
            ),
            aestheticTags = listOf("Date Night", "Night Glam", "Parisian Chic", "Riviera Resort"),
            bodyTypeFlattery = mapOf(
                BodyType.HOURGLASS to "Highlights natural curves with zero restriction.",
                BodyType.RECTANGLE to "Bias cut hugs the body dynamically creating subtle contour.",
                BodyType.PEAR to "Skims gently over hips without pulling or bunching.",
                BodyType.INVERTED_TRIANGLE to "Scoop neckline and fluid skirt equalize proportions.",
                BodyType.OVAL to "Pair with an open structured blazer for clean vertical framing."
            ),
            isTrending = true,
            isNewArrival = true,
            priceDropPercent = 50,
            colorHex = 0xFFC85A32
        ),
        ProductItem(
            id = "prod_005",
            name = "Pointed Slingback Sculptural Stiletto",
            brand = "Aura Atelier",
            category = ProductCategory.SHOES,
            lowestPrice = 110.0,
            highestPrice = 210.0,
            originalPrice = 220.0,
            rating = 4.75f,
            reviewCount = 84,
            storeOffers = listOf(
                StorePriceOffer("Zara Studio", 110.0, 220.0, 50, "Free pickup", "In Stock", true),
                StorePriceOffer("Farfetch", 165.0, 220.0, 25, "$15 shipping", "In Stock (38, 39, 40)", false),
                StorePriceOffer("Nordstrom", 195.0, 220.0, 11, "Free shipping", "In Stock", false)
            ),
            imageUrl = "https://images.unsplash.com/photo-1543163521-1bf539c55dd2?auto=format&fit=crop&w=800&q=80",
            description = "Architectural 75mm angled block heel with Italian nappa leather upper, cushioned insole, and micro elasticated slingback strap.",
            fabricComposition = "100% Calfskin Nappa Leather, Memory Foam Insole",
            fitType = "True to Size (Narrow to Medium)",
            availableSizes = listOf("EU 36", "EU 37", "EU 38", "EU 39", "EU 40", "EU 41"),
            availableColors = listOf(
                ColorOption("Noir Gloss", 0xFF0D0D11),
                ColorOption("Alabaster Cream", 0xFFF2ECE1),
                ColorOption("Metallic Gold", 0xFFD4AF37)
            ),
            aestheticTags = listOf("Old Money", "Modern Minimalist", "Night Glam", "Office Chic"),
            bodyTypeFlattery = mapOf(
                BodyType.HOURGLASS to "Elongates ankle and calf lines seamlessly.",
                BodyType.RECTANGLE to "Pointed toe gives dynamic sharpness to outfits.",
                BodyType.PEAR to "Vertical heel lift visually lengthens lower leg line.",
                BodyType.INVERTED_TRIANGLE to "Refined silhouette anchors look gracefully.",
                BodyType.OVAL to "Creates an instant posture lift."
            ),
            isTrending = false,
            isNewArrival = true,
            priceDropPercent = 50,
            colorHex = 0xFF0D0D11
        ),
        ProductItem(
            id = "prod_006",
            name = "Architectural Half-Moon Leather Shoulder Bag",
            brand = "Valenti Florence",
            category = ProductCategory.ACCESSORIES,
            lowestPrice = 165.0,
            highestPrice = 280.0,
            originalPrice = 310.0,
            rating = 4.9f,
            reviewCount = 175,
            storeOffers = listOf(
                StorePriceOffer("Farfetch", 165.0, 310.0, 46, "Express delivery", "In Stock", true, "VIPLUCKEY"),
                StorePriceOffer("SSENSE", 215.0, 310.0, 30, "Standard Free", "In Stock", false),
                StorePriceOffer("Nordstrom", 280.0, 310.0, 10, "Free in-store pickup", "Low stock (1 left)", false)
            ),
            imageUrl = "https://images.unsplash.com/photo-1584917865442-de89df76afd3?auto=format&fit=crop&w=800&q=80",
            description = "Structured vegetable-tanned Italian saddle leather with magnetic flap closure, brushed gold hardware, and interchangeable crossbody and shoulder straps.",
            fabricComposition = "100% Full-Grain Calfskin Leather",
            fitType = "One Size (Compact Day-to-Night)",
            availableSizes = listOf("Standard (24 x 16 x 7 cm)"),
            availableColors = listOf(
                ColorOption("Warm Chestnut", 0xFF663B2A),
                ColorOption("Deep Olive", 0xFF354230),
                ColorOption("Raven Black", 0xFF141318)
            ),
            aestheticTags = listOf("Quiet Luxury", "Modern Minimalist", "Parisian Chic", "Old Money"),
            bodyTypeFlattery = mapOf(
                BodyType.HOURGLASS to "Rests comfortably below shoulder without adding hip bulk.",
                BodyType.RECTANGLE to "Curved half-moon geometry introduces soft curvature.",
                BodyType.PEAR to "Shoulder length draws visual anchor above the waist.",
                BodyType.INVERTED_TRIANGLE to "Wear as crossbody to place visual focal point at lower hip.",
                BodyType.OVAL to "Structured clean lines contrast nicely with fluid garments."
            ),
            isTrending = true,
            isNewArrival = false,
            priceDropPercent = 46,
            colorHex = 0xFF663B2A
        ),
        ProductItem(
            id = "prod_007",
            name = "Relaxed Oversized Knit Cashmere Sweater",
            brand = "Nordic Line",
            category = ProductCategory.TOPS,
            lowestPrice = 120.0,
            highestPrice = 210.0,
            originalPrice = 240.0,
            rating = 4.85f,
            reviewCount = 164,
            storeOffers = listOf(
                StorePriceOffer("ASOS Premium", 120.0, 240.0, 50, "Free shipping over $50", "In Stock", true, "WARM50"),
                StorePriceOffer("Nordstrom", 160.0, 240.0, 33, "Free delivery", "In Stock (S, M)", false),
                StorePriceOffer("Farfetch", 210.0, 240.0, 12, "Express 2 days", "In Stock", false)
            ),
            imageUrl = "https://images.unsplash.com/photo-1576566588028-4147f3842f27?auto=format&fit=crop&w=800&q=80",
            description = "Cloud-soft 7-gauge Mongolian cashmere with dropped shoulders, ribbed turtleneck, and side split hems for the effortless half-tuck.",
            fabricComposition = "100% Grade-A Pure Mongolian Cashmere",
            fitType = "Slouchy Oversized",
            availableSizes = listOf("XS", "S", "M", "L", "XL"),
            availableColors = listOf(
                ColorOption("Oatmeal Heather", 0xFFD8D0C5),
                ColorOption("Charcoal Slate", 0xFF3E4048),
                ColorOption("Soft Butter", 0xFFF5E6BF)
            ),
            aestheticTags = listOf("Quiet Luxury", "Modern Minimalist", "Old Money"),
            bodyTypeFlattery = mapOf(
                BodyType.HOURGLASS to "French-tuck into high waist pants to maintain waist definition.",
                BodyType.RECTANGLE to "Oversized slouch adds relaxed volume and chic texture.",
                BodyType.PEAR to "Side slit hem allows it to sit loosely without catching on hips.",
                BodyType.INVERTED_TRIANGLE to "Dropped shoulder seam softens broad angles.",
                BodyType.OVAL to "Relaxed straight fall skims torso with supreme comfort."
            ),
            isTrending = false,
            isNewArrival = true,
            priceDropPercent = 50,
            colorHex = 0xFFD8D0C5
        ),
        ProductItem(
            id = "prod_008",
            name = "Retro Chunky Sole Leather Loafer",
            brand = "Vagabond Milano",
            category = ProductCategory.SHOES,
            lowestPrice = 115.0,
            highestPrice = 180.0,
            originalPrice = 190.0,
            rating = 4.8f,
            reviewCount = 129,
            storeOffers = listOf(
                StorePriceOffer("ASOS", 115.0, 190.0, 39, "Free delivery", "In Stock", true),
                StorePriceOffer("Zara", 129.0, 190.0, 32, "In-store pickup", "In Stock", false),
                StorePriceOffer("Farfetch", 180.0, 190.0, 5, "Express courier", "Last 3 pairs", false)
            ),
            imageUrl = "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?auto=format&fit=crop&w=800&q=80",
            description = "High-shine polished box calf leather with penny strap slot, lightweight lug sole, and padded collar for all-day city walks.",
            fabricComposition = "100% Italian Box Leather, Ultra-Light EVA Sole",
            fitType = "True to Size",
            availableSizes = listOf("EU 37", "EU 38", "EU 39", "EU 40", "EU 41", "EU 42"),
            availableColors = listOf(
                ColorOption("Glossy Burgundy", 0xFF4A1525),
                ColorOption("Obsidian Black", 0xFF111013)
            ),
            aestheticTags = listOf("Streetwear", "Parisian Chic", "Modern Minimalist", "Old Money"),
            bodyTypeFlattery = mapOf(
                BodyType.HOURGLASS to "Pairs sharply with cropped trousers and midis.",
                BodyType.RECTANGLE to "Grounds outfits with bold architectural weight.",
                BodyType.PEAR to "Chunky platform balances silhouette proportions.",
                BodyType.INVERTED_TRIANGLE to "Adds bottom grounding weight against broad blazers.",
                BodyType.OVAL to "Effortless styling with straight trousers or trench coats."
            ),
            isTrending = true,
            isNewArrival = false,
            priceDropPercent = 39,
            colorHex = 0xFF4A1525
        )
    )

    // Certified Professional Stylists available for VIP booking
    val professionalStylists: List<ProfessionalStylist> = listOf(
        ProfessionalStylist(
            id = "stylist_01",
            name = "Camille Devereaux",
            title = "Senior Celebrity Stylist & Red Carpet Advisor",
            experienceYears = 11,
            rating = 4.98f,
            reviewsCount = 186,
            bio = "Former Vogue Paris editorial director and stylist to Cannes Film Festival attendees. Specializes in Quiet Luxury, capsule wardrobe minimalism, and flattering silhouettes for high-profile engagements.",
            specialties = listOf("Quiet Luxury", "Capsule Wardrobes", "Red Carpet / Gala", "Color Harmony"),
            hourlyRate = 85.0,
            location = "Paris / New York",
            avatarRes = R.drawable.img_stylist_pro_1787220814689,
            sampleLookbooksCount = 42,
            clientsStyled = 680
        ),
        ProfessionalStylist(
            id = "stylist_02",
            name = "Marcus Vance",
            title = "Luxury Streetwear & Sneaker Couturier",
            experienceYears = 8,
            rating = 4.94f,
            reviewsCount = 142,
            bio = "Styling high-profile athletes and musicians across London and Tokyo. Expert in luxury streetwear proportion pairing, limited edition sneaker integration, and high-low wardrobe curation.",
            specialties = listOf("Luxury Streetwear", "Athleisure", "Sneaker Styling", "Proportion Theory"),
            hourlyRate = 65.0,
            location = "London / Tokyo",
            avatarRes = R.drawable.img_avatar_male_1787220800239,
            sampleLookbooksCount = 28,
            clientsStyled = 410
        ),
        ProfessionalStylist(
            id = "stylist_03",
            name = "Elena Rostova",
            title = "Certified Seasonal Color & Body Architect",
            experienceYears = 9,
            rating = 4.96f,
            reviewsCount = 215,
            bio = "Certified image consultant specializing in precision 12-season color analysis, bust-to-hip silhouette balancing, and professional executive presence wardrobe transitions.",
            specialties = listOf("Seasonal Color Analysis", "Body Silhouette Flattery", "Executive Chic", "Travel Wardrobe"),
            hourlyRate = 75.0,
            location = "Milan / Digital Worldwide",
            avatarRes = R.drawable.img_avatar_female_1787220782196,
            sampleLookbooksCount = 35,
            clientsStyled = 550
        )
    )

    fun getProductById(id: String): ProductItem? {
        return productsCatalog.find { it.id == id }
    }

    fun getProductsByCategory(category: ProductCategory): List<ProductItem> {
        return if (category == ProductCategory.ALL) productsCatalog else productsCatalog.filter { it.category == category }
    }
}
