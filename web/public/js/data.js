// Fashion Data Models & Mock Data mirrored from Android FashionRepository.kt & FashionModels.kt

export const CATEGORIES = {
  ALL: 'ALL',
  TOPS: 'TOPS',
  BOTTOMS: 'BOTTOMS',
  OUTERWEAR: 'OUTERWEAR',
  DRESSES: 'DRESSES',
  SHOES: 'SHOES',
  ACCESSORIES: 'ACCESSORIES'
};

export const CATEGORY_LABELS = {
  ALL: 'All Pieces',
  TOPS: 'Tops & Shirts',
  OUTERWEAR: 'Jackets & Coats',
  BOTTOMS: 'Trousers & Skirts',
  DRESSES: 'Dresses & Sets',
  SHOES: 'Footwear',
  ACCESSORIES: 'Bags & Jewelry'
};

export const BODY_TYPES = [
  { id: 'HOURGLASS', name: 'Hourglass', description: 'Balanced bust & hips with defined waist', bestCuts: ['Wrap Dresses', 'High-Rise Trousers', 'Fitted Blazers', 'Bias-Cut Silhouettes'] },
  { id: 'RECTANGLE', name: 'Athletic / Rectangle', description: 'Even bust, waist & hips with athletic line', bestCuts: ['Belted Coats', 'Pleated Trousers', 'Cut-Out Tops', 'Voluminous Outerwear'] },
  { id: 'PEAR', name: 'Pear / Triangle', description: 'Curvaceous hips & thighs with defined upper torso', bestCuts: ['Statement Shoulders', 'Boatneck Tops', 'A-Line Skirts', 'Wide-Leg Pants'] },
  { id: 'INVERTED_TRIANGLE', name: 'Inverted Triangle', description: 'Broader shoulders & chest with streamlined hips', bestCuts: ['V-Neck Tops', 'Flared Trousers', 'Raglan Sleeves', 'Full-Skirt Dresses'] },
  { id: 'OVAL', name: 'Apple / Oval', description: 'Softer silhouette around midsection with lean legs', bestCuts: ['Empire Waist', 'Tunic Blouses', 'Open-Front Cardigans', 'Straight-Leg Cuts'] }
];

export const SKIN_TONES = [
  {
    id: 'FAIR_COOL',
    name: 'Fair Porcelain (Cool Rosy)',
    undertone: 'Cool Blue/Pink',
    swatch: '#FBE4D8',
    recommendedColors: [
      { name: 'Icy Lavender', hex: '#E6E6FA' },
      { name: 'Emerald Jewel', hex: '#004B23' },
      { name: 'Midnight Onyx', hex: '#1C1D21' },
      { name: 'Rosewater', hex: '#FAD2E1' }
    ]
  },
  {
    id: 'FAIR_WARM',
    name: 'Ivory Warm (Peachy Peach)',
    undertone: 'Warm Golden',
    swatch: '#F5D6BA',
    recommendedColors: [
      { name: 'Camel Dune', hex: '#C19A6B' },
      { name: 'Terracotta', hex: '#C86D51' },
      { name: 'Cream Cashmere', hex: '#FFFDD0' },
      { name: 'Sage Leaf', hex: '#87A96B' }
    ]
  },
  {
    id: 'MEDIUM_NEUTRAL',
    name: 'Sand Medium (Neutral Balance)',
    undertone: 'Balanced Neutral',
    swatch: '#DEB887',
    recommendedColors: [
      { name: 'Champagne Pearl', hex: '#F7E7CE' },
      { name: 'Rich Espresso', hex: '#362B28' },
      { name: 'Burgundy Wine', hex: '#6B1724' },
      { name: 'Olive Green', hex: '#556B2F' }
    ]
  },
  {
    id: 'OLIVE',
    name: 'Mediterranean Olive (Golden Green)',
    undertone: 'Warm Green/Yellow',
    swatch: '#C8A165',
    recommendedColors: [
      { name: 'Burnished Gold', hex: '#D4AF37' },
      { name: 'Forest Teal', hex: '#004F54' },
      { name: 'Warm Terracotta', hex: '#D97443' },
      { name: 'Warm Truffle', hex: '#4B3621' }
    ]
  },
  {
    id: 'TAN_GOLDEN',
    name: 'Golden Bronze (Warm Bronze)',
    undertone: 'Warm Copper',
    swatch: '#B87D4B',
    recommendedColors: [
      { name: 'Saffron Amber', hex: '#FF9933' },
      { name: 'Tuscan Sun', hex: '#F4C430' },
      { name: 'Cobalt Blue', hex: '#0047AB' },
      { name: 'Pure Chalk White', hex: '#FFFFFF' }
    ]
  },
  {
    id: 'DEEP_RICH',
    name: 'Deep Espresso (Rich Warm/Cool)',
    undertone: 'Rich Radiant Espresso',
    swatch: '#5A3825',
    recommendedColors: [
      { name: 'Royal Amethyst', hex: '#7851A9' },
      { name: 'Liquid Gold', hex: '#FFD700' },
      { name: 'Electric Fuchsia', hex: '#FF007F' },
      { name: 'Pure White Silk', hex: '#FAF9F6' }
    ]
  }
];

export const PRODUCTS = [
  {
    id: 'prod_1',
    name: 'Double-Breasted Wool-Blend Trench',
    brand: 'Saint Laurent Atelier',
    category: CATEGORIES.OUTERWEAR,
    originalPrice: 420.0,
    lowestPrice: 249.0,
    priceDropPercent: 41,
    rating: 4.9,
    reviewCount: 128,
    imageUrl: 'https://images.unsplash.com/photo-1544022613-e87ca75a784a?w=800&auto=format&fit=crop&q=80',
    description: 'A timeless architectural trench tailored from heavyweight Italian virgin wool blend. Features sharp notch lapels, structured epaulettes, and a waist-cinching horn buckle belt.',
    material: '75% Virgin Wool, 25% Polyamide (Cupro lining)',
    silhouette: 'Tailored Hourglass Overcoat',
    fitType: 'True to Size / Structured Shoulders',
    aestheticTags: ['Quiet Luxury', 'Old Money', 'Parisian Chic'],
    flatteringBodyTypes: ['HOURGLASS', 'RECTANGLE', 'PEAR', 'INVERTED_TRIANGLE'],
    storeOffers: [
      { storeName: 'Farfetch', price: 249.0, discountPercent: 41, inStock: true, shippingInfo: 'Express Delivery (2 days)', storeUrl: 'https://farfetch.com' },
      { storeName: 'Nordstrom', price: 310.0, discountPercent: 26, inStock: true, shippingInfo: 'Standard Free Shipping', storeUrl: 'https://nordstrom.com' },
      { storeName: 'Zara', price: 349.0, discountPercent: 17, inStock: true, shippingInfo: 'Standard (3-5 days)', storeUrl: 'https://zara.com' },
      { storeName: 'ASOS', price: 420.0, discountPercent: 0, inStock: false, shippingInfo: 'Backorder Available', storeUrl: 'https://asos.com' }
    ],
    availableColors: [
      { name: 'Camel Dune', hex: '#C19A6B' },
      { name: 'Midnight Onyx', hex: '#1C1D21' },
      { name: 'Chalk Ivory', hex: '#EDE8DF' }
    ],
    availableSizes: ['XS', 'S', 'M', 'L', 'XL']
  },
  {
    id: 'prod_2',
    name: 'Silk-Satin Draped Asymmetric Blouse',
    brand: 'Khaite Minimalist',
    category: CATEGORIES.TOPS,
    originalPrice: 280.0,
    lowestPrice: 159.0,
    priceDropPercent: 43,
    rating: 4.8,
    reviewCount: 94,
    imageUrl: 'https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=800&auto=format&fit=crop&q=80',
    description: 'Cut on the bias from lustrous 19mm mulberry silk. Features a cowl neckline that gathers fluidly across the collarbone, with elongated French cuffs and mother-of-pearl buttons.',
    material: '100% Mulberry Silk',
    silhouette: 'Fluid Bias-Cut Draping',
    fitType: 'Relaxed Fit with Tapered Waist',
    aestheticTags: ['Quiet Luxury', 'Date Night', 'Modern Minimalist'],
    flatteringBodyTypes: ['HOURGLASS', 'RECTANGLE', 'INVERTED_TRIANGLE', 'OVAL'],
    storeOffers: [
      { storeName: 'Nordstrom', price: 159.0, discountPercent: 43, inStock: true, shippingInfo: 'Free 2-Day Shipping', storeUrl: 'https://nordstrom.com' },
      { storeName: 'Farfetch', price: 195.0, discountPercent: 30, inStock: true, shippingInfo: 'Import Duties Included', storeUrl: 'https://farfetch.com' },
      { storeName: 'Zara', price: 220.0, discountPercent: 21, inStock: true, shippingInfo: 'Store Pickup Available', storeUrl: 'https://zara.com' },
      { storeName: 'ASOS', price: 280.0, discountPercent: 0, inStock: true, shippingInfo: 'Standard Delivery', storeUrl: 'https://asos.com' }
    ],
    availableColors: [
      { name: 'Champagne Pearl', hex: '#F7E7CE' },
      { name: 'Terracotta Rose', hex: '#C86D51' },
      { name: 'Emerald', hex: '#004B23' }
    ],
    availableSizes: ['XS', 'S', 'M', 'L']
  },
  {
    id: 'prod_3',
    name: 'High-Waisted Pleated Wide-Leg Trousers',
    brand: 'Totême Stockholm',
    category: CATEGORIES.BOTTOMS,
    originalPrice: 290.0,
    lowestPrice: 168.0,
    priceDropPercent: 42,
    rating: 4.9,
    reviewCount: 165,
    imageUrl: 'https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?w=800&auto=format&fit=crop&q=80',
    description: 'Impeccably tailored from fluid tropical wool with sharp front knife pleats. Sits high at the natural waist and pools elegantly over heels or loafers.',
    material: '88% Wool, 10% Mohair, 2% Elastane',
    silhouette: 'Architectural High-Rise Wide Leg',
    fitType: 'Tailored Waist, Fluid Thigh',
    aestheticTags: ['Old Money', 'Modern Minimalist', 'Quiet Luxury'],
    flatteringBodyTypes: ['HOURGLASS', 'RECTANGLE', 'PEAR', 'OVAL'],
    storeOffers: [
      { storeName: 'Zara', price: 168.0, discountPercent: 42, inStock: true, shippingInfo: 'Standard Free Shipping', storeUrl: 'https://zara.com' },
      { storeName: 'Farfetch', price: 210.0, discountPercent: 27, inStock: true, shippingInfo: 'Express Delivery (3 days)', storeUrl: 'https://farfetch.com' },
      { storeName: 'Nordstrom', price: 245.0, discountPercent: 15, inStock: true, shippingInfo: 'In-Store Pickup Ready', storeUrl: 'https://nordstrom.com' },
      { storeName: 'ASOS', price: 290.0, discountPercent: 0, inStock: true, shippingInfo: 'Standard Delivery', storeUrl: 'https://asos.com' }
    ],
    availableColors: [
      { name: 'Espresso Noir', hex: '#211F26' },
      { name: 'Alabaster Oat', hex: '#E3DAC9' },
      { name: 'Slate Charcoal', hex: '#3B3945' }
    ],
    availableSizes: ['24', '25', '26', '27', '28', '29', '30']
  },
  {
    id: 'prod_4',
    name: 'Draped Bias-Cut Silk Maxi Slip Dress',
    brand: 'Reformation Riviera',
    category: CATEGORIES.DRESSES,
    originalPrice: 350.0,
    lowestPrice: 189.0,
    priceDropPercent: 46,
    rating: 4.95,
    reviewCount: 210,
    imageUrl: 'https://images.unsplash.com/photo-1566174053879-31528523f8ae?w=800&auto=format&fit=crop&q=80',
    description: 'A 90s-inspired slip dress with a scooped cowl neck, delicate spaghetti straps, and an open back with tie detailing. Cascades diagonally across body contours.',
    material: '100% Heavy Silk Charmeuse',
    silhouette: 'Body-Skimming Bias Cut',
    fitType: 'Form-Skimming with Gentle Flare',
    aestheticTags: ['Date Night', 'Quiet Luxury', 'Parisian Chic'],
    flatteringBodyTypes: ['HOURGLASS', 'RECTANGLE', 'PEAR'],
    storeOffers: [
      { storeName: 'Farfetch', price: 189.0, discountPercent: 46, inStock: true, shippingInfo: 'Express Duties Paid', storeUrl: 'https://farfetch.com' },
      { storeName: 'Nordstrom', price: 225.0, discountPercent: 35, inStock: true, shippingInfo: 'Free Returns Included', storeUrl: 'https://nordstrom.com' },
      { storeName: 'Zara', price: 279.0, discountPercent: 20, inStock: true, shippingInfo: 'Express Delivery', storeUrl: 'https://zara.com' },
      { storeName: 'ASOS', price: 350.0, discountPercent: 0, inStock: true, shippingInfo: 'Standard Shipping', storeUrl: 'https://asos.com' }
    ],
    availableColors: [
      { name: 'Terracotta Bronze', hex: '#C86D51' },
      { name: 'Midnight Onyx', hex: '#1A1C1E' },
      { name: 'Champagne Silk', hex: '#F7E7CE' }
    ],
    availableSizes: ['0', '2', '4', '6', '8', '10']
  },
  {
    id: 'prod_5',
    name: 'Pointed Slingback Sculptural Stilettos',
    brand: 'The Row Minimal',
    category: CATEGORIES.SHOES,
    originalPrice: 380.0,
    lowestPrice: 225.0,
    priceDropPercent: 40,
    rating: 4.75,
    reviewCount: 88,
    imageUrl: 'https://images.unsplash.com/photo-1543163521-1bf539c55dd2?w=800&auto=format&fit=crop&q=80',
    description: 'Crafted in Italy from supple Nappa lambskin. Features an elongated square point toe, slim elasticized slingback strap, and an architectural 65mm flared kitten heel.',
    material: '100% Italian Lambskin Leather',
    silhouette: 'Architectural Slingback Stiletto',
    fitType: 'True to Size (Italian Sizing)',
    aestheticTags: ['Modern Minimalist', 'Date Night', 'Old Money'],
    flatteringBodyTypes: ['HOURGLASS', 'RECTANGLE', 'PEAR', 'INVERTED_TRIANGLE', 'OVAL'],
    storeOffers: [
      { storeName: 'Nordstrom', price: 225.0, discountPercent: 40, inStock: true, shippingInfo: 'Complimentary Styling Gift', storeUrl: 'https://nordstrom.com' },
      { storeName: 'Farfetch', price: 260.0, discountPercent: 31, inStock: true, shippingInfo: 'Duties Included', storeUrl: 'https://farfetch.com' },
      { storeName: 'Zara', price: 310.0, discountPercent: 18, inStock: true, shippingInfo: 'Standard 4 days', storeUrl: 'https://zara.com' },
      { storeName: 'ASOS', price: 380.0, discountPercent: 0, inStock: false, shippingInfo: 'Sold Out', storeUrl: 'https://asos.com' }
    ],
    availableColors: [
      { name: 'Pitch Black Nappa', hex: '#1A1C1E' },
      { name: 'Alabaster Bone', hex: '#EDE8DF' },
      { name: 'Wine Cherry', hex: '#6B1724' }
    ],
    availableSizes: ['36', '37', '38', '39', '40', '41']
  },
  {
    id: 'prod_6',
    name: 'Half-Moon Leather Crossbody Shoulder Bag',
    brand: 'Valenti Florence',
    category: CATEGORIES.ACCESSORIES,
    originalPrice: 310.0,
    lowestPrice: 175.0,
    priceDropPercent: 43,
    rating: 4.88,
    reviewCount: 142,
    imageUrl: 'https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=800&auto=format&fit=crop&q=80',
    description: 'Sculpted in Florence from vegetable-tanned Italian calf leather. Features clean geometric lines, suede lining, magnetic flap closure, and a polished gold-tone buckle.',
    material: 'Vegetable Tanned Full-Grain Leather',
    silhouette: 'Clean Sculptural Half-Moon',
    fitType: 'Adjustable Drop (38cm - 54cm)',
    aestheticTags: ['Quiet Luxury', 'Parisian Chic', 'Old Money'],
    flatteringBodyTypes: ['HOURGLASS', 'RECTANGLE', 'PEAR', 'INVERTED_TRIANGLE', 'OVAL'],
    storeOffers: [
      { storeName: 'Farfetch', price: 175.0, discountPercent: 43, inStock: true, shippingInfo: 'Free Global Returns', storeUrl: 'https://farfetch.com' },
      { storeName: 'Zara', price: 215.0, discountPercent: 30, inStock: true, shippingInfo: 'Next-Day Delivery', storeUrl: 'https://zara.com' },
      { storeName: 'Nordstrom', price: 260.0, discountPercent: 16, inStock: true, shippingInfo: 'Standard Shipping', storeUrl: 'https://nordstrom.com' },
      { storeName: 'ASOS', price: 310.0, discountPercent: 0, inStock: true, shippingInfo: 'Standard Shipping', storeUrl: 'https://asos.com' }
    ],
    availableColors: [
      { name: 'Warm Chestnut', hex: '#6F4E37' },
      { name: 'Onyx Black', hex: '#1C1D21' },
      { name: 'Butter Ivory', hex: '#FFFDD0' }
    ],
    availableSizes: ['One Size (24cm x 16cm x 7cm)']
  }
];

export const STYLISTS = [
  {
    id: 'stylist_1',
    name: 'Camille Devereaux',
    title: 'Senior Haute Couture Consultant',
    location: 'Paris & New York',
    hourlyRate: 85,
    rating: 4.98,
    reviewsCount: 312,
    imageUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&auto=format&fit=crop&q=80',
    bio: 'Former Vogue Paris editorial stylist specializing in capsule wardrobes, timeless French tailoring, and red carpet silhouette architecture.',
    specialties: ['Quiet Luxury', 'French Tailoring', 'Capsule Wardrobe', 'Event Gala']
  },
  {
    id: 'stylist_2',
    name: 'Marcus Vance',
    title: 'Executive Image & Color Strategist',
    location: 'London & Tokyo',
    hourlyRate: 65,
    rating: 4.94,
    reviewsCount: 198,
    imageUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500&auto=format&fit=crop&q=80',
    bio: 'Savile Row trained consultant focusing on proportion mastery, seasonal color science, and executive boardroom confidence.',
    specialties: ['Executive Wardrobe', 'Color Analysis', 'Minimalist Tech', 'Bespoke Suiting']
  },
  {
    id: 'stylist_3',
    name: 'Elena Rostova',
    title: 'Avant-Garde & Seasonal Trend Director',
    location: 'Milan & Digital Global',
    hourlyRate: 75,
    rating: 4.96,
    reviewsCount: 247,
    imageUrl: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=500&auto=format&fit=crop&q=80',
    bio: 'Milan Fashion Week consultant fusing modern architectural silhouettes with vintage archival luxury and sustainable styling.',
    specialties: ['Old Money Aesthetic', 'Vintage Archival', 'Color Undertone', 'Date Night']
  }
];

export const SUBSCRIPTION_TIERS = [
  {
    id: 'STARTER',
    title: 'Aura Starter',
    monthlyPrice: 0,
    description: 'Essential virtual fitting room & daily automated price comparisons.',
    features: [
      '3D Silhouette Mannequin & Draping',
      'Daily Price Drop Alerts across 4 Retailers',
      'Up to 3 AI Styling Prompts per month',
      'Standard Size Recommendations'
    ]
  },
  {
    id: 'STYLE_PRO',
    title: 'Style Pro',
    monthlyPrice: 14.99,
    description: 'Unlimited AI Stylist intelligence, personalized color harmonization & priority deals.',
    isPopular: true,
    features: [
      'Unlimited Gemini Flash Haute Couture Stylist',
      'Automated Multi-Store Price Tracker (Real-time)',
      'Seasonal Undertone Color Harmonizer Analysis',
      'Save Unlimited Custom 3D Lookbooks',
      '10% Discount on Certified Stylist Consultations'
    ]
  },
  {
    id: 'VIP_ATELIER',
    title: 'VIP Atelier',
    monthlyPrice: 39.99,
    description: 'The pinnacle of private luxury styling, monthly 1-on-1 human stylist sessions & bespoke concierge.',
    features: [
      'Everything in Style Pro Tier',
      '1 Complimentary Monthly 1-on-1 Stylist Video Call',
      'Dedicated Paris/Milan Stylist Direct Messaging',
      'Concierge Multi-Store Order Synchronization',
      'VIP Early Access to Archival Private Sales'
    ]
  }
];

export const DEFAULT_USER_PROFILE = {
  name: 'Eleanor Vance',
  heightCm: 172,
  weightKg: 60,
  bustCm: 88,
  waistCm: 68,
  hipsCm: 95,
  bodyTypeName: 'HOURGLASS',
  skinToneName: 'MEDIUM_NEUTRAL',
  preferredStylesCsv: 'Quiet Luxury, Parisian Chic, Modern Minimalist',
  subscriptionTierName: 'STYLE_PRO'
};
