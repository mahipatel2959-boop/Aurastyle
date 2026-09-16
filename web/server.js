import express from 'express';
import cors from 'cors';
import path from 'path';
import { fileURLToPath } from 'url';
import fs from 'fs';
import {
  PRODUCTS,
  CATEGORIES,
  CATEGORY_LABELS,
  BODY_TYPES,
  SKIN_TONES,
  STYLISTS,
  SUBSCRIPTION_TIERS,
  DEFAULT_USER_PROFILE
} from './public/js/data.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const PORT = process.env.WEB_PORT || (process.env.PORT && process.env.PORT !== '8080' ? process.env.PORT : 3000);

app.use(cors());
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

// Retrieve Gemini API Key securely server-side (Never exposed to client)
function getGeminiApiKey() {
  const envKey = process.env.GEMINI_API_KEY || '';
  if (envKey && envKey !== 'MY_GEMINI_API_KEY' && envKey.trim().length > 0) {
    return envKey.trim();
  }
  try {
    const rootEnvPath = path.resolve(__dirname, '../.env');
    if (fs.existsSync(rootEnvPath)) {
      const content = fs.readFileSync(rootEnvPath, 'utf8');
      const match = content.match(/GEMINI_API_KEY=(.+)/);
      if (match && match[1] && match[1].trim() !== 'MY_GEMINI_API_KEY') {
        return match[1].trim();
      }
    }
  } catch (e) {
    // Ignore error
  }
  return '';
}

// --------------------------------------------------------------------------
// 1. SizeAdvisorEngine Module (Faithful server-side replica of SizeAdvisorEngine.kt)
// --------------------------------------------------------------------------
function calculateFitAdvice(product, user) {
  const bust = user?.bustCm || 88;
  const waist = user?.waistCm || 68;
  const hips = user?.hipsCm || 95;
  const height = user?.heightCm || 172;
  const bodyType = user?.bodyTypeName || 'HOURGLASS';

  let sizeBasedOnBust = 'M';
  if (bust < 84) sizeBasedOnBust = 'XS';
  else if (bust <= 90) sizeBasedOnBust = 'S';
  else if (bust <= 96) sizeBasedOnBust = 'M';
  else if (bust <= 104) sizeBasedOnBust = 'L';
  else sizeBasedOnBust = 'XL';

  let sizeBasedOnWaistHips = 'M';
  if (waist < 66 || hips < 90) sizeBasedOnWaistHips = 'XS';
  else if (waist <= 72 && hips <= 96) sizeBasedOnWaistHips = 'S';
  else if (waist <= 78 && hips <= 103) sizeBasedOnWaistHips = 'M';
  else if (waist <= 86 && hips <= 111) sizeBasedOnWaistHips = 'L';
  else sizeBasedOnWaistHips = 'XL';

  let recommendedSize = 'M';
  switch (product.category) {
    case CATEGORIES.TOPS:
    case CATEGORIES.OUTERWEAR:
      recommendedSize = sizeBasedOnBust;
      break;
    case CATEGORIES.BOTTOMS:
      recommendedSize = sizeBasedOnWaistHips;
      break;
    case CATEGORIES.DRESSES:
      recommendedSize = (bodyType === 'PEAR') ? sizeBasedOnWaistHips : sizeBasedOnBust;
      break;
    case CATEGORIES.SHOES:
      recommendedSize = 'EU 38';
      break;
    default:
      recommendedSize = 'Standard';
  }

  let confidence = 92;
  if (product.category === CATEGORIES.ACCESSORIES) confidence = 99;
  else if ((product.fitType || '').toLowerCase().includes('relaxed')) confidence = 97;
  else if ((product.fitType || '').toLowerCase().includes('oversized')) confidence = 95;
  else if ((product.fitType || '').toLowerCase().includes('tailored')) confidence = 94;

  const flatteryNote = product.bodyTypeFlattery?.[bodyType] ||
    `The proportions of this garment enhance your natural silhouette.`;

  let stretchFactor = 'Standard Woven Texture';
  if ((product.fabricComposition || '').includes('Elastane') || (product.fabricComposition || '').includes('Spandex')) {
    stretchFactor = 'High Comfort Stretch (3-5% give)';
  } else if ((product.fabricComposition || '').includes('Wool') || (product.fabricComposition || '').includes('Silk')) {
    stretchFactor = 'Natural Breathable Drape (Structured with zero static)';
  }

  return {
    recommendedSize,
    confidencePercent: confidence,
    fitSummary: `Size ${recommendedSize} will flatter your ${bodyType.toLowerCase()} silhouette with a ${(product.fitType || 'modern').toLowerCase()} drape.`,
    bustFit: `Ideal contours around your ${bust} cm bustline without strain.`,
    waistFit: `Sits naturally at your ${waist} cm waistline with clean ease.`,
    lengthFit: `Proportioned gracefully for your ${height} cm stature.`,
    stretchFactor,
    stylingTip: flatteryNote
  };
}

// --------------------------------------------------------------------------
// 2. TryOnCompositor Module (Faithful server-side replica of TryOnCompositor.kt)
// --------------------------------------------------------------------------
function analyzeTryOnLook(selectedItems = [], user = {}, occasion = 'Casual Chic') {
  if (!selectedItems || selectedItems.length === 0) {
    return {
      harmonyScore: 0,
      silhouetteBalanceScore: 0,
      colorHarmonyScore: 0,
      occasionScore: 0,
      stylistVerdict: 'Select garments in the mannequin slots above to run live virtual try-on analysis.',
      keyStrengths: [],
      stylingSuggestions: ['Add a Top, Bottom, or Outerwear piece to begin styling.']
    };
  }

  const bodyType = user.bodyTypeName || 'HOURGLASS';
  const skinTone = user.skinToneName || 'MEDIUM_NEUTRAL';

  let baseScore = 88;
  const strengths = [];
  const tips = [];

  if (selectedItems.length >= 3) {
    baseScore += 6;
    strengths.push('Full layered ensemble with balanced focal points.');
  } else if (selectedItems.length === 2) {
    baseScore += 3;
    strengths.push('Clean minimalist 2-piece coordination.');
  }

  const hasFlattering = selectedItems.some(i => (i.name || '').includes('Trench') || (i.name || '').includes('Pleated'));
  if (hasFlattering) {
    baseScore += 3;
    strengths.push(`Structured lines accentuate your ${bodyType.toLowerCase()} silhouette.`);
  }

  const skinObj = SKIN_TONES.find(s => s.id === skinTone) || SKIN_TONES[2];
  strengths.push(`Color palette complements ${skinObj.name} (${skinObj.undertone}).`);
  tips.push('Roll up sleeves or open the top collar button for effortless French nonchalance.');
  tips.push('Add gold or brass metal jewelry to tie the warm tones together.');

  const finalHarmony = Math.min(99, Math.max(75, baseScore));
  const silhouetteScore = Math.min(98, Math.max(80, baseScore - 2));
  const colorScore = Math.min(100, Math.max(82, baseScore + 1));
  const occasionScore = Math.min(97, Math.max(78, baseScore - 1));

  let verdict = 'Chic everyday harmony. You can amplify the contrast with statement footwear or a structured shoulder bag.';
  if (finalHarmony >= 94) {
    verdict = 'Exceptional sartorial balance. The garment proportions lengthen your frame while the color tones elevate your skin undertone seamlessly.';
  } else if (finalHarmony >= 88) {
    verdict = 'Polished and cohesive look. The textures contrast gracefully, creating high-end quiet luxury appeal.';
  }

  return {
    harmonyScore: finalHarmony,
    silhouetteBalanceScore: silhouetteScore,
    colorHarmonyScore: colorScore,
    occasionScore,
    stylistVerdict: verdict,
    keyStrengths: strengths,
    stylingSuggestions: tips
  };
}

// --------------------------------------------------------------------------
// 3. Fallback Intelligent Advice Engine (Mirror of GeminiStylistService.kt)
// --------------------------------------------------------------------------
function getOfflineStylingAdvice(prompt, userProfile, relevantProducts = []) {
  const lowerPrompt = (prompt || '').toLowerCase();
  const bodyType = userProfile?.bodyTypeName || 'Hourglass';
  const skinTone = userProfile?.skinToneName || 'Sand Medium (Neutral Balance)';
  const waistCm = userProfile?.waistCm || 68;
  const heightCm = userProfile?.heightCm || 172;
  const bustCm = userProfile?.bustCm || 88;

  if (lowerPrompt.includes('date') || lowerPrompt.includes('evening') || lowerPrompt.includes('night') || lowerPrompt.includes('gala')) {
    return `✨ **Evening Allure for your ${bodyType} Silhouette**\n\n` +
      `• **The Foundation**: Pair the **Draped Bias-Cut Silk Maxi Slip Dress** in Terracotta or Midnight Onyx. The fluid diagonal bias skim highlights your ${waistCm} cm waistline with effortless grace.\n` +
      `• **Layering**: Drape the **Double-Breasted Wool Trench** over shoulders (cape-style) rather than wearing sleeves to keep the evening drape fluid.\n` +
      `• **Footwear & Accessories**: Elevate with **Pointed Slingback Sculptural Stilettos** and the **Half-Moon Leather Shoulder Bag** in Warm Chestnut.\n` +
      `• **Stylist Note**: Your ${skinTone} undertone shines against warm terracotta, champagne gold, and deep onyx satin. Add delicate gold huggie earrings to illuminate your collarbones.`;
  }

  if (lowerPrompt.includes('work') || lowerPrompt.includes('office') || lowerPrompt.includes('meeting') || lowerPrompt.includes('business')) {
    return `💼 **Executive Chic & Tailored Authority**\n\n` +
      `• **The Proportions**: Anchor your ensemble with **High-Waisted Pleated Wide-Leg Trousers** in Espresso Noir. The knife pleats elongate your ${heightCm} cm stature.\n` +
      `• **Upper Pairing**: Tuck in the **Silk-Satin Draped Asymmetric Blouse** in Champagne Pearl. The subtle lustre conveys quiet luxury in boardrooms or gallery previews.\n` +
      `• **Finishing Touch**: Add **Retro Chunky Sole Leather Loafers** to ground the wide-leg hem with modern architectural sharpness.\n` +
      `• **Fit Confidence**: 96% fit synergy with your ${bustCm} cm bust and ${waistCm} cm waistline.`;
  }

  if (lowerPrompt.includes('casual') || lowerPrompt.includes('weekend') || lowerPrompt.includes('sunday') || lowerPrompt.includes('street') || lowerPrompt.includes('airport')) {
    return `☕ **Effortless Parisian Sunday / Streetwear Luxe**\n\n` +
      `• **The Look**: Style the **Relaxed Oversized Knit Cashmere Sweater** in Oatmeal Heather with tailored high-rise trousers or straight-cut raw denim.\n` +
      `• **The French Half-Tuck**: Tuck only the front 3 inches of the sweater hem into your waistband to preserve your defined waist while enjoying relaxed comfort.\n` +
      `• **Accessories**: Slung the **Valenti Florence Half-Moon Leather Bag** across the body for clean diagonals.\n` +
      `• **Color Palette**: Neutral monochromatic creams, camel, and deep espresso create an instantly expensive aesthetic.`;
  }

  return `✨ **Personalized AuraStyle Atelier Recommendation**\n\n` +
    `• **Optimal Silhouette**: For your **${bodyType}** frame, we balance upper structure with clean, fluid lower drapery.\n` +
    `• **Recommended Ensemble**:\n` +
    `  1. *Outerwear*: Double-Breasted Wool Trench (Camel Dune)\n` +
    `  2. *Base*: Silk-Satin Asymmetric Blouse (Champagne Pearl)\n` +
    `  3. *Trouser*: High-Waisted Wide-Leg Pleated Pant (Espresso Noir)\n` +
    `  4. *Shoes*: Pointed Slingback Sculptural Stiletto\n` +
    `• **Color Harmony**: Soft champagne and rich camel accentuate your ${skinTone} undertone with a radiant glow.\n` +
    `• **Sizing Insight**: Size M in outerwear offers ideal shoulder room for knit layering; Size S in trousers fits your ${waistCm} cm waist without gaping.`;
}

// --------------------------------------------------------------------------
// REST API Endpoints
// --------------------------------------------------------------------------

// 1. Products Catalog API
app.get('/api/products', (req, res) => {
  const { category, aesthetic, search, minPriceDrop } = req.query;
  let results = [...PRODUCTS];

  if (category && category !== 'ALL') {
    results = results.filter(p => p.category === category);
  }
  if (aesthetic && aesthetic !== 'All') {
    results = results.filter(p => p.aestheticTags.some(t => t.toLowerCase().includes(aesthetic.toLowerCase())));
  }
  if (search) {
    const q = search.toLowerCase();
    results = results.filter(p => p.name.toLowerCase().includes(q) || p.brand.toLowerCase().includes(q));
  }
  if (minPriceDrop) {
    const minDrop = parseFloat(minPriceDrop);
    results = results.filter(p => p.priceDropPercent >= minDrop);
  }

  res.json({
    total: results.length,
    products: results
  });
});

app.get('/api/products/:id', (req, res) => {
  const product = PRODUCTS.find(p => p.id === req.params.id);
  if (!product) {
    return res.status(404).json({ error: 'Product not found' });
  }
  res.json(product);
});

// 2. Size Advisor Engine API (Replicates SizeAdvisorEngine.kt)
app.post('/api/size-advisor', (req, res) => {
  const { product, userProfile } = req.body;
  if (!product) {
    return res.status(400).json({ error: 'Product is required' });
  }
  const advice = calculateFitAdvice(product, userProfile || DEFAULT_USER_PROFILE);
  res.json(advice);
});

// 3. Try-On Compositor API (Replicates TryOnCompositor.kt)
app.post('/api/try-on/analyze', (req, res) => {
  const { selectedItems, userProfile, occasion } = req.body;
  const analysis = analyzeTryOnLook(selectedItems, userProfile || DEFAULT_USER_PROFILE, occasion);
  res.json(analysis);
});

// 4. Stylists Roster API
app.get('/api/stylists', (req, res) => {
  res.json({ stylists: STYLISTS });
});

// 5. Subscription Tiers API
app.get('/api/tiers', (req, res) => {
  res.json({ tiers: SUBSCRIPTION_TIERS });
});

// 6. Profile Options (Body Types and Skin Tones) API
app.get('/api/profile-options', (req, res) => {
  res.json({
    bodyTypes: BODY_TYPES,
    skinTones: SKIN_TONES,
    categories: CATEGORIES,
    categoryLabels: CATEGORY_LABELS
  });
});

// 7. Gemini Stylist Proxy API (Secure server-side LLM call)
app.post('/api/gemini/stylist', async (req, res) => {
  try {
    const { prompt, userProfile, relevantProducts } = req.body;
    if (!prompt || typeof prompt !== 'string') {
      return res.status(400).json({ error: 'Prompt is required' });
    }

    const apiKey = getGeminiApiKey();

    if (!apiKey) {
      const offlineResult = getOfflineStylingAdvice(prompt, userProfile, relevantProducts);
      return res.json({
        advice: offlineResult,
        source: 'AuraStyle Haute Couture Engine (Local Intelligence)'
      });
    }

    const profileText = userProfile ? `
User Profile:
- Name: ${userProfile.name || 'Client'}
- Height: ${userProfile.heightCm || 172} cm, Weight: ${userProfile.weightKg || 60} kg
- Body Type: ${userProfile.bodyTypeName || 'Hourglass'}
- Skin Undertone: ${userProfile.skinToneName || 'Sand Medium'}
- Bust: ${userProfile.bustCm || 88} cm, Waist: ${userProfile.waistCm || 68} cm, Hips: ${userProfile.hipsCm || 95} cm
- Preferred Aesthetics: ${userProfile.preferredStylesCsv || 'Quiet Luxury, Minimalist'}
- Subscription: ${userProfile.subscriptionTierName || 'Style Pro'}
    ` : '';

    const productsText = Array.isArray(relevantProducts) && relevantProducts.length > 0
      ? `Curated Catalog Available:\n` + relevantProducts.slice(0, 5).map(p => `- ${p.brand} ${p.name} ($${p.lowestPrice}, ${p.category}, ${p.fitType})`).join('\n')
      : '';

    const systemInstruction = `You are AuraStyle AI, a world-class luxury fashion stylist and haute couture consultant.
${profileText}
${productsText}

Provide elegant, specific, highly actionable styling advice:
1. Recommend exact outfit silhouettes, color palettes, and layering techniques.
2. Explain why specific cuts flatter their body type and skin undertone.
3. Highlight key footwear, accessories, and styling tricks (e.g. French tuck, cuff rolls, belt placement).
4. Keep the tone sophisticated, warm, encouraging, and luxurious.`;

    const fullPrompt = `${systemInstruction}\n\nClient Occasion / Styling Request:\n${prompt}`;
    const MODEL_NAME = 'gemini-3.5-flash';
    const geminiUrl = `https://generativelanguage.googleapis.com/v1beta/models/${MODEL_NAME}:generateContent?key=${apiKey}`;

    const geminiResponse = await fetch(geminiUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        contents: [{ parts: [{ text: fullPrompt }] }],
        generationConfig: {
          temperature: 0.7,
          topP: 0.95
        }
      })
    });

    if (!geminiResponse.ok) {
      const errText = await geminiResponse.text();
      console.warn('[Server] Gemini API error, falling back to local stylist engine:', errText);
      const fallbackResult = getOfflineStylingAdvice(prompt, userProfile, relevantProducts);
      return res.json({
        advice: fallbackResult,
        source: 'AuraStyle Haute Couture Engine (Local Fallback)'
      });
    }

    const data = await geminiResponse.json();
    const generatedText = data?.candidates?.[0]?.content?.parts?.[0]?.text;

    if (generatedText) {
      return res.json({
        advice: generatedText,
        source: 'Gemini 3.5 Flash Model'
      });
    } else {
      const fallbackResult = getOfflineStylingAdvice(prompt, userProfile, relevantProducts);
      return res.json({
        advice: fallbackResult,
        source: 'AuraStyle Haute Couture Engine'
      });
    }

  } catch (err) {
    console.error('[Server] Exception in /api/gemini/stylist:', err);
    const { prompt, userProfile, relevantProducts } = req.body;
    const fallbackResult = getOfflineStylingAdvice(prompt, userProfile, relevantProducts);
    return res.json({
      advice: fallbackResult,
      source: 'AuraStyle Haute Couture Engine'
    });
  }
});

// 8. Health Check API
app.get('/api/health', (req, res) => {
  res.json({
    status: 'ok',
    app: 'VogueAI Web App',
    hasGeminiKey: Boolean(getGeminiApiKey()),
    model: 'gemini-3.5-flash',
    modules: [
      'SizeAdvisorEngine',
      'TryOnCompositor',
      'GeminiStylistService',
      'ProductCatalog',
      'RetailerPriceComparison',
      'StylistConsultants',
      'SubscriptionTiers'
    ]
  });
});

// Wildcard SPA route
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

function startServer(portToUse) {
  const server = app.listen(portToUse, '0.0.0.0', () => {
    console.log(`VogueAI Web Server running on http://localhost:${portToUse}`);
  });

  server.on('error', (err) => {
    if (err.code === 'EADDRINUSE') {
      console.warn(`[Server] Port ${portToUse} in use, trying port ${Number(portToUse) + 1}...`);
      startServer(Number(portToUse) + 1);
    } else {
      console.error('[Server] Listen error:', err);
    }
  });
}

// In local and traditional Node environments, start server listener
if (!process.env.VERCEL) {
  startServer(PORT);
}

// Export default app for Vercel Serverless Function entrypoint
export default app;
