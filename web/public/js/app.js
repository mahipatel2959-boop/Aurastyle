import {
  PRODUCTS,
  CATEGORIES,
  CATEGORY_LABELS,
  BODY_TYPES,
  SKIN_TONES,
  STYLISTS,
  SUBSCRIPTION_TIERS,
  DEFAULT_USER_PROFILE
} from './data.js';

// Application State Management (Mirrors FashionViewModel in Android)
const state = {
  currentTab: 'shop', // 'shop', 'try-on', 'ai-stylist', 'vip', 'closet'
  selectedCategory: CATEGORIES.ALL,
  selectedAesthetic: 'All',
  searchQuery: '',
  selectedProduct: null,
  
  userProfile: { ...DEFAULT_USER_PROFILE },
  cartItems: [],
  priceAlerts: [],
  savedOutfits: [],
  stylistBookings: [],
  
  tryOnState: {
    outerwear: PRODUCTS.find(p => p.id === 'prod_1') || null,
    top: PRODUCTS.find(p => p.id === 'prod_2') || null,
    bottom: PRODUCTS.find(p => p.id === 'prod_3') || null,
    shoes: PRODUCTS.find(p => p.id === 'prod_5') || null,
    accessory: PRODUCTS.find(p => p.id === 'prod_6') || null,
    bodyType: 'HOURGLASS',
    skinTone: 'MEDIUM_NEUTRAL',
    gender: 'FEMALE'
  },
  
  googleAuth: {
    isSignedIn: false,
    name: 'Mahi Patel',
    email: 'mahipatel2959@gmail.com',
    isAuthenticating: false
  },

  aiStylistResult: null,
  isAiGenerating: false,
  
  // Modals
  activeModal: null, // 'fit-profile', 'price-alert', 'book-stylist', 'slot-picker', 'save-outfit', 'tier-upgrade', 'google-signin', 'google-account'
  modalContext: null
};

// Initialize from LocalStorage
function loadPersistentState() {
  try {
    const savedProfile = localStorage.getItem('vogue_profile');
    if (savedProfile) state.userProfile = JSON.parse(savedProfile);

    const savedCart = localStorage.getItem('vogue_cart');
    if (savedCart) state.cartItems = JSON.parse(savedCart);

    const savedAlerts = localStorage.getItem('vogue_alerts');
    if (savedAlerts) state.priceAlerts = JSON.parse(savedAlerts);

    const savedOutfits = localStorage.getItem('vogue_saved_outfits');
    if (savedOutfits) state.savedOutfits = JSON.parse(savedOutfits);

    const savedBookings = localStorage.getItem('vogue_bookings');
    if (savedBookings) state.stylistBookings = JSON.parse(savedBookings);

    const savedGoogleAuth = localStorage.getItem('vogue_google_auth');
    if (savedGoogleAuth) {
      state.googleAuth = JSON.parse(savedGoogleAuth);
    } else if (!sessionStorage.getItem('vogue_dismissed_auth')) {
      // Show Google sign in dialog on initial visit for mockup test
      state.activeModal = 'google-signin';
    }
  } catch (e) {
    console.warn('Could not read from localStorage', e);
  }
}

function saveState(key, data) {
  try {
    localStorage.setItem(key, JSON.stringify(data));
  } catch (e) {
    console.warn('Could not write to localStorage', e);
  }
}

// Toast Notification
function showToast(msg) {
  const existing = document.querySelector('.toast-msg');
  if (existing) existing.remove();

  const toast = document.createElement('div');
  toast.className = 'toast-msg';
  toast.textContent = msg;
  document.body.appendChild(toast);

  setTimeout(() => {
    toast.remove();
  }, 3000);
}

// Fit Advice Engine (Exact replica of SizeAdvisorEngine.kt)
function calculateFitAdvice(product, profile) {
  const bust = profile.bustCm || 88;
  const waist = profile.waistCm || 68;
  const hips = profile.hipsCm || 95;
  const height = profile.heightCm || 172;
  const bodyType = profile.bodyTypeName || 'HOURGLASS';

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

// Try-On Harmony Calculation (Exact replica of TryOnCompositor.kt)
function calculateTryOnAnalysis() {
  const items = [
    state.tryOnState.outerwear,
    state.tryOnState.top,
    state.tryOnState.bottom,
    state.tryOnState.shoes,
    state.tryOnState.accessory
  ].filter(Boolean);

  if (items.length === 0) {
    return {
      harmonyScore: 0,
      silhouetteBalanceScore: 0,
      colorHarmonyScore: 0,
      occasionScore: 0,
      stylistVerdict: 'Select garments in the mannequin slots above to run live virtual try-on analysis.',
      keyStrengths: [],
      stylingSuggestions: ['Add a Top, Bottom, or Outerwear piece to begin styling.'],
      verdict: 'Select garments in the mannequin slots above to run live virtual try-on analysis.',
      strengths: [],
      tips: ['Add a Top, Bottom, or Outerwear piece to begin styling.'],
      silhouetteBalance: 0,
      colorUndertone: 0,
      occasionSynergy: 0
    };
  }

  const bodyType = state.tryOnState.bodyType || 'HOURGLASS';
  const skinTone = state.tryOnState.skinTone || 'MEDIUM_NEUTRAL';

  let baseScore = 88;
  const strengths = [];
  const tips = [];

  if (items.length >= 3) {
    baseScore += 6;
    strengths.push('Full layered ensemble with balanced focal points.');
  } else if (items.length === 2) {
    baseScore += 3;
    strengths.push('Clean minimalist 2-piece coordination.');
  }

  const hasFlattering = items.some(i => (i.name || '').includes('Trench') || (i.name || '').includes('Pleated'));
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
    stylingSuggestions: tips,
    verdict: verdict,
    strengths: strengths,
    tips: tips,
    silhouetteBalance: silhouetteScore,
    colorUndertone: colorScore,
    occasionSynergy: occasionScore
  };
}

// Navigation Tab Switcher
export function switchTab(tabId) {
  state.currentTab = tabId;
  state.selectedProduct = null; // dismiss product detail if active
  render();
}

// Product Selector
export function viewProduct(productId) {
  const prod = PRODUCTS.find(p => p.id === productId);
  if (prod) {
    state.selectedProduct = prod;
    render();
  }
}

export function closeProductDetail() {
  state.selectedProduct = null;
  render();
}

// Cart Management
export function addToCart(product, size = 'M', color = '', store = null) {
  const chosenStore = store || product.storeOffers[0];
  const chosenColor = color || (product.availableColors[0] ? product.availableColors[0].name : 'Default');
  
  const existingIdx = state.cartItems.findIndex(
    item => item.productId === product.id && item.selectedSize === size && item.storeName === chosenStore.storeName
  );

  if (existingIdx >= 0) {
    state.cartItems[existingIdx].quantity += 1;
  } else {
    state.cartItems.push({
      id: `cart_${Date.now()}`,
      productId: product.id,
      productName: product.name,
      brand: product.brand,
      category: product.category,
      price: chosenStore.price,
      originalPrice: product.originalPrice,
      selectedSize: size,
      selectedColor: chosenColor,
      storeName: chosenStore.storeName,
      imageUrl: product.imageUrl,
      quantity: 1
    });
  }

  saveState('vogue_cart', state.cartItems);
  showToast(`Added ${product.name} to bag`);
  render();
}

export function removeFromCart(cartId) {
  state.cartItems = state.cartItems.filter(item => item.id !== cartId);
  saveState('vogue_cart', state.cartItems);
  render();
}

export function updateCartQuantity(cartId, delta) {
  const item = state.cartItems.find(i => i.id === cartId);
  if (item) {
    item.quantity += delta;
    if (item.quantity <= 0) {
      removeFromCart(cartId);
      return;
    }
    saveState('vogue_cart', state.cartItems);
    render();
  }
}

// Price Alert Tracker
export function addPriceAlert(product, targetPrice, storeName) {
  state.priceAlerts.push({
    id: `alert_${Date.now()}`,
    productId: product.id,
    productName: product.name,
    brand: product.brand,
    targetPrice: parseFloat(targetPrice),
    currentLowest: product.lowestPrice,
    storeName: storeName || 'Any Store',
    createdAt: new Date().toLocaleDateString()
  });

  saveState('vogue_alerts', state.priceAlerts);
  showToast(`Price tracker set for $${targetPrice}`);
  closeModal();
  render();
}

export function removePriceAlert(alertId) {
  state.priceAlerts = state.priceAlerts.filter(a => a.id !== alertId);
  saveState('vogue_alerts', state.priceAlerts);
  render();
}

// Try-On Actions
export function sendToTryOn(product) {
  if (product.category === CATEGORIES.OUTERWEAR) state.tryOnState.outerwear = product;
  else if (product.category === CATEGORIES.TOPS) state.tryOnState.top = product;
  else if (product.category === CATEGORIES.BOTTOMS) state.tryOnState.bottom = product;
  else if (product.category === CATEGORIES.SHOES) state.tryOnState.shoes = product;
  else if (product.category === CATEGORIES.ACCESSORIES) state.tryOnState.accessory = product;
  else if (product.category === CATEGORIES.DRESSES) {
    state.tryOnState.top = product;
    state.tryOnState.bottom = null;
  }

  state.currentTab = 'try-on';
  state.selectedProduct = null;
  showToast(`Draped ${product.name} in Try-On Studio`);
  render();
}

export function removeTryOnSlot(category) {
  if (category === CATEGORIES.OUTERWEAR) state.tryOnState.outerwear = null;
  if (category === CATEGORIES.TOPS) state.tryOnState.top = null;
  if (category === CATEGORIES.BOTTOMS) state.tryOnState.bottom = null;
  if (category === CATEGORIES.SHOES) state.tryOnState.shoes = null;
  if (category === CATEGORIES.ACCESSORIES) state.tryOnState.accessory = null;
  render();
}

export function saveCurrentOutfit(name) {
  const activeItems = [
    state.tryOnState.outerwear,
    state.tryOnState.top,
    state.tryOnState.bottom,
    state.tryOnState.shoes,
    state.tryOnState.accessory
  ].filter(Boolean);

  const outfit = {
    id: `outfit_${Date.now()}`,
    name: name || 'Autumn Gala Ensemble',
    date: new Date().toLocaleDateString(),
    itemCount: activeItems.length,
    harmonyScore: calculateTryOnAnalysis().harmonyScore,
    items: activeItems.map(i => ({ id: i.id, name: i.name, brand: i.brand, price: i.lowestPrice, imageUrl: i.imageUrl }))
  };

  state.savedOutfits.unshift(outfit);
  saveState('vogue_saved_outfits', state.savedOutfits);
  showToast(`Saved lookbook: "${outfit.name}"`);
  closeModal();
  render();
}

export function addAllTryOnToCart() {
  const activeItems = [
    state.tryOnState.outerwear,
    state.tryOnState.top,
    state.tryOnState.bottom,
    state.tryOnState.shoes,
    state.tryOnState.accessory
  ].filter(Boolean);

  activeItems.forEach(item => {
    addToCart(item);
  });
  showToast(`Added ${activeItems.length} outfit pieces to bag`);
}

// Serverless Backend Gemini AI Stylist Call
export async function generateAiEnsemble(promptText) {
  if (!promptText || promptText.trim().length === 0) {
    showToast('Please describe your occasion or event');
    return;
  }

  state.isAiGenerating = true;
  render();

  try {
    // Call server-side API proxy endpoint (Gemini API key is NEVER exposed on client!)
    const response = await fetch('/api/gemini/stylist', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        prompt: promptText,
        userProfile: state.userProfile,
        relevantProducts: PRODUCTS
      })
    });

    if (!response.ok) {
      throw new Error(`Server returned status: ${response.status}`);
    }

    const data = await response.json();
    state.aiStylistResult = {
      text: data.advice,
      source: data.source || 'Gemini 3.5 Flash Model'
    };
  } catch (err) {
    console.warn('AI request failed, fallback triggered', err);
    state.aiStylistResult = {
      text: `✨ **Personalized Haute Couture Ensemble**\n\n` +
        `• **Foundation**: Draped Bias-Cut Silk Maxi Slip Dress paired with Double-Breasted Wool Trench.\n` +
        `• **Silhouette Balance**: Cut skims your ${state.userProfile.bodyTypeName.toLowerCase()} silhouette gracefully.\n` +
        `• **Accessories**: Half-Moon Leather Crossbody in Warm Chestnut with Pointed Slingback Stilettos.`,
      source: 'AuraStyle Haute Couture Engine (Local)'
    };
  } finally {
    state.isAiGenerating = false;
    render();
  }
}

// Modal Controllers
export function openModal(type, context = null) {
  state.activeModal = type;
  state.modalContext = context;
  render();
}

export function closeModal() {
  state.activeModal = null;
  state.modalContext = null;
  render();
}

// UI Rendering Functions
function render() {
  const root = document.getElementById('app-root');
  if (!root) return;

  const totalCartCount = state.cartItems.reduce((acc, i) => acc + i.quantity, 0);

  root.innerHTML = `
    <div class="app-shell">
      <!-- App Header -->
      <header class="app-header">
        <div>
          <h1 class="brand-title">VogueAI</h1>
          <p class="brand-subtitle">Smart Styling & Price Intelligence</p>
        </div>
        <div class="header-actions">
          ${state.googleAuth.isSignedIn ? `
            <button class="header-user-btn" onclick="window.vogueApp.openModal('google-account')" title="Google Account: ${state.googleAuth.name}">
              <span class="google-user-avatar">M</span>
              <span class="google-user-name">${state.googleAuth.name.split(' ')[0]}</span>
            </button>
          ` : `
            <button class="header-google-signin-pill" onclick="window.vogueApp.openGoogleSignIn()" title="Sign in with Google">
              <svg width="14" height="14" viewBox="0 0 24 24">
                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"/>
                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"/>
              </svg>
              <span>Sign In</span>
            </button>
          `}
          <button class="header-icon-btn" onclick="window.vogueApp.openModal('fit-profile')" title="Fit Profile">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
              <circle cx="12" cy="7" r="4"></circle>
            </svg>
          </button>
          <button class="header-icon-btn" onclick="window.vogueApp.switchTab('closet')" title="Shopping Bag">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z"></path>
              <path d="M3 6h18"></path>
              <path d="M16 10a4 4 0 0 1-8 0"></path>
            </svg>
            ${totalCartCount > 0 ? `<span class="badge-counter">${totalCartCount}</span>` : ''}
          </button>
        </div>
      </header>

      <!-- Main Tab Content Area -->
      <main class="app-content">
        ${renderCurrentTab()}
      </main>

      <!-- Bottom Navigation Bar -->
      <nav class="bottom-nav">
        <button class="nav-item ${state.currentTab === 'shop' ? 'active' : ''}" onclick="window.vogueApp.switchTab('shop')">
          <div class="nav-icon-wrapper">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="m2 7 4.41-4.41A2 2 0 0 1 7.83 2h8.34a2 2 0 0 1 1.42.59L22 7"></path>
              <path d="M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8"></path>
              <path d="M15 22v-4a2 2 0 0 0-2-2h-2a2 2 0 0 0-2 2v4"></path>
              <path d="M2 7h20"></path>
            </svg>
          </div>
          <span>Shop</span>
        </button>

        <button class="nav-item ${state.currentTab === 'try-on' ? 'active' : ''}" onclick="window.vogueApp.switchTab('try-on')">
          <div class="nav-icon-wrapper">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20.38 3.46 16 2a4 4 0 0 1-8 0L3.62 3.46a2 2 0 0 0-1.34 2.23l.58 3.47a1 1 0 0 0 .99.84H6v10c0 1.1.9 2 2 2h8a2 2 0 0 0 2-2V10h2.15a1 1 0 0 0 .99-.84l.58-3.47a2 2 0 0 0-1.34-2.23z"></path>
            </svg>
          </div>
          <span>3D Try-On</span>
        </button>

        <button class="nav-item ${state.currentTab === 'ai-stylist' ? 'active' : ''}" onclick="window.vogueApp.switchTab('ai-stylist')">
          <div class="nav-icon-wrapper">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="m12 3-1.912 5.813a2 2 0 0 1-1.275 1.275L3 12l5.813 1.912a2 2 0 0 1 1.275 1.275L12 21l1.912-5.813a2 2 0 0 1 1.275-1.275L21 12l-5.813-1.912a2 2 0 0 1-1.275-1.275L12 3Z"></path>
              <path d="M5 3v4"></path>
              <path d="M19 17v4"></path>
              <path d="M3 5h4"></path>
              <path d="M17 19h4"></path>
            </svg>
          </div>
          <span>AI Stylist</span>
        </button>

        <button class="nav-item ${state.currentTab === 'vip' ? 'active' : ''}" onclick="window.vogueApp.switchTab('vip')">
          <div class="nav-icon-wrapper">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M6 3h12l4 6-10 13L2 9Z"></path>
              <path d="M11 3 8 9l4 13 4-13-3-6"></path>
              <path d="M2 9h20"></path>
            </svg>
          </div>
          <span>VIP Atelier</span>
        </button>

        <button class="nav-item ${state.currentTab === 'closet' ? 'active' : ''}" onclick="window.vogueApp.switchTab('closet')">
          <div class="nav-icon-wrapper">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect width="18" height="18" x="3" y="3" rx="2"></rect>
              <path d="M3 9h18"></path>
              <path d="M3 15h18"></path>
              <path d="M9 3v18"></path>
              <path d="M15 3v18"></path>
            </svg>
          </div>
          <span>Fit & Bag</span>
        </button>
      </nav>

      <!-- Product Detail Overlay -->
      ${state.selectedProduct ? renderProductDetailOverlay() : ''}

      <!-- Modals -->
      ${renderModal()}
    </div>
  `;
}

function renderCurrentTab() {
  switch (state.currentTab) {
    case 'shop':
      return renderShopTab();
    case 'try-on':
      return renderTryOnTab();
    case 'ai-stylist':
      return renderAiStylistTab();
    case 'vip':
      return renderVipTab();
    case 'closet':
      return renderClosetTab();
    default:
      return renderShopTab();
  }
}

// 1. SHOP TAB
function renderShopTab() {
  const aesthetics = ['All', 'Quiet Luxury', 'Old Money', 'Modern Minimalist', 'Parisian Chic', 'Date Night', 'Streetwear'];

  const filtered = PRODUCTS.filter(p => {
    const matchCat = state.selectedCategory === CATEGORIES.ALL || p.category === state.selectedCategory;
    const matchAesthetic = state.selectedAesthetic === 'All' || p.aestheticTags.some(t => t.toLowerCase().includes(state.selectedAesthetic.toLowerCase()));
    const matchSearch = !state.searchQuery || p.name.toLowerCase().includes(state.searchQuery.toLowerCase()) || p.brand.toLowerCase().includes(state.searchQuery.toLowerCase());
    return matchCat && matchAesthetic && matchSearch;
  });

  const trending = PRODUCTS.filter(p => p.priceDropPercent >= 40);

  return `
    <!-- Hero Minimalist Fitting Room Banner -->
    <div class="hero-card">
      <img src="https://images.unsplash.com/photo-1490481651871-ab68de25d43d?w=800&auto=format&fit=crop&q=80" alt="Fashion Fitting Room" class="hero-img">
      <div class="hero-overlay">
        <div class="hero-pill">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
            <path d="m12 3-1.912 5.813a2 2 0 0 1-1.275 1.275L3 12l5.813 1.912a2 2 0 0 1 1.275 1.275L12 21l1.912-5.813a2 2 0 0 1 1.275-1.275L21 12l-5.813-1.912a2 2 0 0 1-1.275-1.275L12 3Z"></path>
          </svg>
          AI FITTING ROOM
        </div>
        <h2 class="hero-title">Virtual Try-On<br>Ready to View</h2>
        <p class="hero-subtitle">Compare prices across Zara, Farfetch, Nordstrom & ASOS</p>
        <div class="hero-actions">
          <button class="btn-hero-primary" onclick="window.vogueApp.switchTab('try-on')">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20.38 3.46 16 2a4 4 0 0 1-8 0L3.62 3.46a2 2 0 0 0-1.34 2.23l.58 3.47a1 1 0 0 0 .99.84H6v10c0 1.1.9 2 2 2h8a2 2 0 0 0 2-2V10h2.15a1 1 0 0 0 .99-.84l.58-3.47a2 2 0 0 0-1.34-2.23z"></path>
            </svg>
            Start 3D Try-On
          </button>
          <button class="btn-hero-outlined" onclick="window.vogueApp.switchTab('ai-stylist')">
            AI Stylist
          </button>
        </div>
      </div>
    </div>

    <!-- Search Bar -->
    <div class="search-container">
      <div class="search-bar">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="var(--text-muted)" stroke-width="2">
          <circle cx="11" cy="11" r="8"></circle>
          <path d="m21 21-4.3-4.3"></path>
        </svg>
        <input type="text" placeholder="Search trench coats, silk blouses, loafers..." 
               value="${state.searchQuery}" 
               oninput="window.vogueApp.setSearch(this.value)">
        ${state.searchQuery ? `
          <button style="background:none;border:none;cursor:pointer;color:var(--text-muted);" onclick="window.vogueApp.setSearch('')">✕</button>
        ` : ''}
      </div>
    </div>

    <!-- Aesthetic Filter Chips -->
    <h3 class="section-heading">Aesthetic Vibes</h3>
    <div class="chips-scroll">
      ${aesthetics.map(a => `
        <button class="chip ${state.selectedAesthetic === a ? 'active' : ''}" 
                onclick="window.vogueApp.setAesthetic('${a}')">
          ${a}
        </button>
      `).join('')}
    </div>

    <!-- Category Filter Bar -->
    <div class="category-bar">
      ${Object.keys(CATEGORIES).map(catKey => `
        <button class="cat-btn ${state.selectedCategory === catKey ? 'active' : ''}" 
                onclick="window.vogueApp.setCategory('${catKey}')">
          ${CATEGORY_LABELS[catKey]}
        </button>
      `).join('')}
    </div>

    <!-- Trending Price Drops Strip -->
    <div class="trending-strip">
      <div class="trending-header">
        <div class="trending-title">
          <span class="fire-icon">🔥</span>
          <span>Major Multi-Store Price Drops</span>
        </div>
        <span class="trending-discount-label">Up to 50% Off</span>
      </div>
      <div class="trending-cards">
        ${trending.map(t => `
          <div class="trending-mini-card" onclick="window.vogueApp.viewProduct('${t.id}')">
            <div class="card-thumb-container">
              <span class="badge-discount-tag">-${t.priceDropPercent}% OFF</span>
              <span class="badge-stores-tag">
                <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8"></path><path d="M15 22v-4a2 2 0 0 0-2-2h-2a2 2 0 0 0-2 2v4"></path><path d="M2 7h20"></path></svg>
                ${t.storeOffers ? t.storeOffers.length : 4} Stores
              </span>
              <img src="${t.imageUrl}" alt="${t.name}" class="trending-mini-img">
              <button class="try-on-overlay-btn" title="3D Try-On"
                      onclick="event.stopPropagation(); window.vogueApp.sendToTryOn(PRODUCTS.find(x => x.id === '${t.id}'))">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <path d="M20.38 3.46 16 2a4 4 0 0 1-8 0L3.62 3.46a2 2 0 0 0-1.34 2.23l.58 3.47a1 1 0 0 0 .99.84H6v10c0 1.1.9 2 2 2h8a2 2 0 0 0 2-2V10h2.15a1 1 0 0 0 .99-.84l.58-3.47a2 2 0 0 0-1.34-2.23z"></path>
                </svg>
                <span>Try On</span>
              </button>
            </div>
            <div class="trending-mini-info">
              <div class="trending-brand">${t.brand}</div>
              <div class="trending-name">${t.name}</div>
              <div class="trending-prices">
                <span class="trending-curr-price">$${Math.round(t.lowestPrice)}</span>
                <span class="trending-orig-price">$${Math.round(t.originalPrice)}</span>
              </div>
            </div>
          </div>
        `).join('')}
      </div>
    </div>

    <!-- Curated Catalog Grid -->
    <div class="products-grid">
      ${filtered.map(p => `
        <div class="product-card" onclick="window.vogueApp.viewProduct('${p.id}')">
          <div class="product-thumb-box">
            <span class="badge-discount-tag">-${p.priceDropPercent}% OFF</span>
            <span class="badge-stores-tag">
              <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8"></path><path d="M15 22v-4a2 2 0 0 0-2-2h-2a2 2 0 0 0-2 2v4"></path><path d="M2 7h20"></path></svg>
              ${p.storeOffers ? p.storeOffers.length : 4} Stores
            </span>
            <img src="${p.imageUrl}" alt="${p.name}" class="product-thumb">
            <button class="try-on-overlay-btn" title="3D Try-On" 
                    onclick="event.stopPropagation(); window.vogueApp.sendToTryOn(PRODUCTS.find(x => x.id === '${p.id}'))">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <path d="M20.38 3.46 16 2a4 4 0 0 1-8 0L3.62 3.46a2 2 0 0 0-1.34 2.23l.58 3.47a1 1 0 0 0 .99.84H6v10c0 1.1.9 2 2 2h8a2 2 0 0 0 2-2V10h2.15a1 1 0 0 0 .99-.84l.58-3.47a2 2 0 0 0-1.34-2.23z"></path>
              </svg>
              <span>Try On</span>
            </button>
          </div>
          <div class="product-details">
            <div class="product-brand">${p.brand}</div>
            <div class="product-title">${p.name}</div>
            <div class="price-row">
              <span class="price-lowest">$${Math.round(p.lowestPrice)}</span>
              <span class="price-orig">$${Math.round(p.originalPrice)}</span>
            </div>
            <div class="store-lead-tag">Lowest on ${p.storeOffers[0].storeName}</div>
          </div>
        </div>
      `).join('')}
    </div>
  `;
}

// 2. VIRTUAL TRY-ON TAB
function renderTryOnTab() {
  const analysis = calculateTryOnAnalysis();
  const tryOn = state.tryOnState;
  const activeItems = [tryOn.outerwear, tryOn.top, tryOn.bottom, tryOn.shoes, tryOn.accessory].filter(Boolean);
  const totalCost = activeItems.reduce((acc, i) => acc + i.lowestPrice, 0);
  const totalOriginal = activeItems.reduce((acc, i) => acc + i.originalPrice, 0);
  const totalSavings = Math.max(0, totalOriginal - totalCost);

  const mannequinImg = (tryOn.gender === 'MALE') 
    ? '/images/img_avatar_male_1787220800239.jpg' 
    : '/images/img_avatar_female_1787220782196.jpg';

  const strengthsList = analysis.strengths || analysis.keyStrengths || [];
  const tipsList = analysis.tips || analysis.stylingSuggestions || [];
  const verdictText = analysis.verdict || analysis.stylistVerdict || 'Add garments to the 3D mannequin to analyze silhouette harmony.';
  const silhouetteScore = analysis.silhouetteBalance || analysis.silhouetteBalanceScore || 92;
  const undertoneScore = analysis.colorUndertone || analysis.colorHarmonyScore || 94;
  const occasionScore = analysis.occasionSynergy || analysis.occasionScore || 90;

  return `
    <div style="padding: 16px 20px 8px; display:flex; justify-content:space-between; align-items:center;">
      <div>
        <h2 style="font-size:18px; font-weight:700; color:var(--text-primary); letter-spacing:0.5px;">VIRTUAL TRY-ON STUDIO</h2>
        <p style="font-size:11px; color:var(--text-muted);">Real-Time 3D Silhouette & Flattery Engine</p>
      </div>
      <span style="background:rgba(164,137,250,0.18); color:var(--primary-glow); font-size:10px; font-weight:700; padding:4px 8px; border-radius:6px; border:1px solid rgba(164,137,250,0.3);">
        AI Drape Active
      </span>
    </div>

    <!-- Model Mannequin Settings -->
    <div class="settings-card">
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
        <div style="font-size:12px; font-weight:700;">Model Silhouette & Undertone</div>
        <div class="gender-toggle-row" style="margin-bottom:0;">
          <button class="gender-btn ${tryOn.gender !== 'MALE' ? 'active' : ''}" 
                  onclick="window.vogueApp.updateTryOnAvatar(null, null, 'FEMALE')">Female</button>
          <button class="gender-btn ${tryOn.gender === 'MALE' ? 'active' : ''}" 
                  onclick="window.vogueApp.updateTryOnAvatar(null, null, 'MALE')">Male</button>
        </div>
      </div>
      <div class="chips-scroll" style="padding:0; margin-bottom:8px;">
        ${BODY_TYPES.map(b => `
          <button class="chip ${tryOn.bodyType === b.id ? 'active' : ''}" 
                  onclick="window.vogueApp.updateTryOnAvatar('${b.id}', null)">
            ${b.name}
          </button>
        `).join('')}
      </div>
      <div style="display:flex; align-items:center; gap:8px;">
        <span style="font-size:11px; color:var(--text-muted);">Skin Tone:</span>
        <div class="swatch-group">
          ${SKIN_TONES.map(s => `
            <button class="swatch-btn ${tryOn.skinTone === s.id ? 'active' : ''}" 
                    style="background:${s.swatch};" 
                    title="${s.name}"
                    onclick="window.vogueApp.updateTryOnAvatar(null, '${s.id}')">
            </button>
          `).join('')}
        </div>
      </div>
    </div>

    <!-- Dark Luxury Studio Card -->
    <div class="dark-studio-card">
      <div class="mannequin-viewport">
        <!-- 3D Angle Switcher -->
        <div class="tryon-angle-controls">
          <button class="angle-btn active">Front 3D</button>
          <button class="angle-btn">45° Angle</button>
          <button class="angle-btn">Back Fit</button>
        </div>

        <!-- Overlay Mannequin Graphic -->
        <img src="${mannequinImg}" 
             onerror="this.src='https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=600&auto=format&fit=crop&q=80'"
             alt="3D Mannequin" class="mannequin-silhouette">
        
        <!-- Live Harmony Score Pill -->
        <div class="harmony-pill">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
            <path d="m12 3-1.912 5.813a2 2 0 0 1-1.275 1.275L3 12l5.813 1.912a2 2 0 0 1 1.275 1.275L12 21l1.912-5.813a2 2 0 0 1 1.275-1.275L21 12l-5.813-1.912a2 2 0 0 1-1.275-1.275L12 3Z"></path>
          </svg>
          ${analysis.harmonyScore || 94}% Harmony
        </div>

        <!-- Visual Draped Garment Tags -->
        <div class="draped-tags-container">
          ${activeItems.map(item => `
            <span class="draped-item-tag">
              ${item.category === CATEGORIES.OUTERWEAR ? '🧥' : item.category === CATEGORIES.TOPS ? '👕' : item.category === CATEGORIES.BOTTOMS ? '👖' : item.category === CATEGORIES.SHOES ? '👠' : '👜'}
              ${item.name.split(' ').slice(0, 3).join(' ')}
            </span>
          `).join('')}
        </div>

        <div class="viewport-footer">
          <span>${activeItems.length} Pieces Layered</span>
          <span style="color:var(--sage-green); font-weight:700;">Total: $${Math.round(totalCost)} (Save $${Math.round(totalSavings)})</span>
        </div>
      </div>

      <div style="margin-top:14px; font-size:12px; font-weight:700; color:white; display:flex; justify-content:space-between; align-items:center;">
        <span>Garment Layer Slots (Tap to Swap)</span>
        <span style="font-size:11px; color:var(--text-muted); font-weight:normal;">Tap ✕ to remove</span>
      </div>

      <!-- 5 Slot Cards -->
      <div class="slots-grid">
        ${renderSlotCard('Outerwear', tryOn.outerwear, CATEGORIES.OUTERWEAR)}
        ${renderSlotCard('Top', tryOn.top, CATEGORIES.TOPS)}
        ${renderSlotCard('Bottom', tryOn.bottom, CATEGORIES.BOTTOMS)}
        ${renderSlotCard('Shoes', tryOn.shoes, CATEGORIES.SHOES)}
        ${renderSlotCard('Bag', tryOn.accessory, CATEGORIES.ACCESSORIES)}
      </div>

      <div style="display:flex; gap:10px; margin-top:16px;">
        <button style="flex:1; background:transparent; border:1px solid var(--primary-glow); color:var(--primary-glow); padding:10px; border-radius:8px; font-weight:600; cursor:pointer;" 
                onclick="window.vogueApp.openModal('save-outfit')">
          Save Lookbook
        </button>
        <button style="flex:1.4; background:linear-gradient(135deg, #7C4DFF, #6750A4); border:none; color:white; padding:10px; border-radius:8px; font-weight:700; cursor:pointer; box-shadow:0 4px 12px rgba(124,77,255,0.3);" 
                onclick="window.vogueApp.addAllTryOnToCart()">
          Add All to Cart ($${Math.round(totalCost)})
        </button>
      </div>
    </div>

    <!-- AI Fit & Styling Critique Card -->
    <div class="critique-card">
      <div class="critique-title">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"></circle>
          <path d="M12 16v-4"></path>
          <path d="M12 8h.01"></path>
        </svg>
        AI Fit & Styling Critique
      </div>
      <div class="critique-body">
        ${verdictText}
      </div>
      <div class="critique-scores">
        <div class="score-pill">
          <div class="score-label">Silhouette</div>
          <div class="score-val">${silhouetteScore}%</div>
        </div>
        <div class="score-pill">
          <div class="score-label">Undertone</div>
          <div class="score-val">${undertoneScore}%</div>
        </div>
        <div class="score-pill">
          <div class="score-label">Occasion</div>
          <div class="score-val">${occasionScore}%</div>
        </div>
      </div>
      
      <div style="font-size:11px; font-weight:700; color:var(--primary-glow); margin-top:10px;">Key Styling Strengths:</div>
      ${strengthsList.map(s => `
        <div style="font-size:12px; margin:3px 0; color:var(--text-secondary);">• ${s}</div>
      `).join('')}

      <div style="font-size:11px; font-weight:700; color:var(--text-muted); margin-top:8px;">Atelier Tips:</div>
      ${tipsList.map(t => `
        <div style="font-size:12px; margin:3px 0; color:var(--text-secondary);">→ ${t}</div>
      `).join('')}
    </div>
  `;
}

function renderSlotCard(slotName, item, category) {
  if (!item) {
    return `
      <div class="slot-card" onclick="window.vogueApp.openModal('slot-picker', '${category}')">
        <span class="slot-label">${slotName}</span>
        <div style="font-size:18px; color:var(--primary-glow); margin: 6px 0;">＋</div>
        <span style="font-size:9px; color:var(--text-muted);">Add</span>
      </div>
    `;
  }

  return `
    <div class="slot-card filled" onclick="window.vogueApp.openModal('slot-picker', '${category}')">
      <span class="slot-label">${slotName}</span>
      <img src="${item.imageUrl}" alt="${item.name}" class="slot-thumb">
      <span class="slot-price">$${Math.round(item.lowestPrice)}</span>
      <button style="position:absolute; top:2px; right:2px; background:rgba(0,0,0,0.6); border:none; color:white; font-size:10px; border-radius:50%; width:16px; height:16px; display:flex; align-items:center; justify-content:center; cursor:pointer;" 
              onclick="event.stopPropagation(); window.vogueApp.removeTryOnSlot('${category}')">✕</button>
    </div>
  `;
}

// 3. AI STYLIST TAB (Direct Android Parity with OutfitStudioScreen.kt)
function renderAiStylistTab() {
  const currentSkin = SKIN_TONES.find(s => s.id === state.userProfile.skinToneName) || SKIN_TONES[2];

  const presets = [
    "Date Night at a Rooftop Bistro",
    "Executive Boardroom & Client Dinner",
    "Parisian Weekend Gallery Stroll",
    "Autumn Wedding Guest",
    "Minimalist Airport Travel Look"
  ];

  return `
    <div style="padding: 16px 20px 8px; display:flex; justify-content:space-between; align-items:center;">
      <div style="display:flex; align-items:center; gap:8px;">
        <div style="width:32px; height:32px; border-radius:8px; background:rgba(164,137,250,0.15); display:flex; align-items:center; justify-content:center; color:var(--primary-glow);">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
            <path d="m12 3-1.912 5.813a2 2 0 0 1-1.275 1.275L3 12l5.813 1.912a2 2 0 0 1 1.275 1.275L12 21l1.912-5.813a2 2 0 0 1 1.275-1.275L21 12l-5.813-1.912a2 2 0 0 1-1.275-1.275L12 3Z"></path>
          </svg>
        </div>
        <div>
          <h2 style="font-size:17px; font-weight:800; color:var(--text-primary); letter-spacing:0.5px;">AI OUTFIT ARCHITECT</h2>
          <p style="font-size:11px; color:var(--text-muted);">Generative Capsule & Occasion Styling Engine</p>
        </div>
      </div>
      <span style="background:linear-gradient(135deg, rgba(124,77,255,0.2), rgba(103,80,164,0.3)); color:var(--primary-glow); font-size:10px; font-weight:700; padding:4px 8px; border-radius:6px; border:1px solid rgba(164,137,250,0.4);">
        Gemini Flash AI
      </span>
    </div>

    <!-- Seasonal Color Harmonizer Card -->
    <div class="settings-card" style="border:1px solid rgba(164,137,250,0.3);">
      <div style="display:flex; justify-content:space-between; align-items:center;">
        <div style="display:flex; align-items:center; gap:6px; font-size:13px; font-weight:700; color:var(--text-primary);">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--primary-glow)" stroke-width="2">
            <circle cx="13.5" cy="6.5" r=".5" fill="currentColor"></circle>
            <circle cx="17.5" cy="10.5" r=".5" fill="currentColor"></circle>
            <circle cx="8.5" cy="7.5" r=".5" fill="currentColor"></circle>
            <circle cx="6.5" cy="12.5" r=".5" fill="currentColor"></circle>
            <path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10c.926 0 1.648-.746 1.648-1.688 0-.437-.18-.835-.437-1.125-.29-.289-.438-.652-.438-1.125a1.64 1.64 0 0 1 1.668-1.668h1.996c3.051 0 5.555-2.503 5.555-5.554C21.965 6.012 17.461 2 12 2z"></path>
          </svg>
          Your Seasonal Color Harmonizer
        </div>
        <button style="background:rgba(164,137,250,0.15); border:1px solid rgba(164,137,250,0.3); color:var(--primary-glow); font-size:11px; font-weight:600; padding:3px 8px; border-radius:6px; cursor:pointer;" 
                onclick="window.vogueApp.openModal('fit-profile')">Change Tone</button>
      </div>
      <div style="font-size:11px; color:var(--text-muted); margin-top:4px;">
        Skin Profile: <b style="color:var(--text-secondary);">${currentSkin.name}</b> (${currentSkin.undertone})
      </div>
      <div style="font-size:10px; color:var(--text-muted); margin-top:8px; font-weight:600; text-transform:uppercase; letter-spacing:0.5px;">Best Flattering Shades:</div>
      <div style="display:flex; gap:8px; margin-top:6px; overflow-x:auto; padding-bottom:4px;">
        ${currentSkin.recommendedColors.map(c => `
          <div style="flex:1; min-width:65px; text-align:center; background:var(--surface-variant); padding:6px 4px; border-radius:8px; border:1px solid var(--border);">
            <div style="height:22px; width:22px; margin:0 auto; border-radius:50%; background:${c.hex}; border:1.5px solid rgba(255,255,255,0.2);"></div>
            <div style="font-size:9px; color:var(--text-secondary); font-weight:600; margin-top:4px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">${c.name}</div>
          </div>
        `).join('')}
      </div>
    </div>

    <!-- Occasion Presets Horizontal Scroll Strip -->
    <div style="padding: 6px 14px 2px;">
      <div style="font-size:11px; font-weight:700; color:var(--text-muted); margin-bottom:6px; text-transform:uppercase; letter-spacing:0.5px;">
        Occasion Presets (Tap to Style):
      </div>
      <div class="chips-scroll" style="padding:0; margin-bottom:4px;">
        ${presets.map(p => `
          <button class="occasion-chip" onclick="document.getElementById('ai-user-prompt').value = '${p}'; window.vogueApp.generateAiEnsemble('${p}');">
            ${p}
          </button>
        `).join('')}
      </div>
    </div>

    <!-- AI Prompt Box (Android Parity) -->
    <div class="ai-prompt-box">
      <div style="font-size:13px; font-weight:700; margin-bottom:8px; color:var(--text-primary); display:flex; align-items:center; gap:6px;">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="var(--primary-glow)" stroke-width="2">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
        </svg>
        Ask AuraStyle AI Haute Couture Advisor
      </div>
      <textarea id="ai-user-prompt" class="ai-prompt-input" 
                placeholder="e.g., 'Style me for an outdoor spring rooftop dinner', 'What coat suits 165cm hourglass frame?'"></textarea>

      <button class="ai-generate-btn" ${state.isAiGenerating ? 'disabled' : ''} 
              onclick="window.vogueApp.generateAiEnsemble(document.getElementById('ai-user-prompt').value)">
        ${state.isAiGenerating ? `
          <svg class="spin" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="2" x2="12" y2="6"></line>
            <line x1="12" y1="18" x2="12" y2="22"></line>
            <line x1="4.93" y1="4.93" x2="7.76" y2="7.76"></line>
            <line x1="16.24" y1="16.24" x2="19.07" y2="19.07"></line>
            <line x1="2" y1="12" x2="6" y2="12"></line>
            <line x1="18" y1="12" x2="22" y2="12"></line>
            <line x1="4.93" y1="19.07" x2="7.76" y2="16.24"></line>
            <line x1="16.24" y1="7.76" x2="19.07" y2="4.93"></line>
          </svg>
          <span>Designing Look...</span>
        ` : `
          <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
            <path d="m12 3-1.912 5.813a2 2 0 0 1-1.275 1.275L3 12l5.813 1.912a2 2 0 0 1 1.275 1.275L12 21l1.912-5.813a2 2 0 0 1 1.275-1.275L21 12l-5.813-1.912a2 2 0 0 1-1.275-1.275L12 3Z"></path>
          </svg>
          <span>Generate Haute Couture Ensemble</span>
        `}
      </button>
    </div>

    <!-- AI Output Box (Android Bespoke Plan Card) -->
    ${state.aiStylistResult ? `
      <div class="bespoke-plan-card">
        <div class="bespoke-plan-header">
          <div class="bespoke-plan-title">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <path d="m12 3-1.912 5.813a2 2 0 0 1-1.275 1.275L3 12l5.813 1.912a2 2 0 0 1 1.275 1.275L12 21l1.912-5.813a2 2 0 0 1 1.275-1.275L21 12l-5.813-1.912a2 2 0 0 1-1.275-1.275L12 3Z"></path>
            </svg>
            Bespoke AI Styling Plan
          </div>
          <button class="load-tryon-btn" onclick="window.vogueApp.switchTab('try-on')">
            Load in 3D Try-On →
          </button>
        </div>
        <div class="ai-result-content">${formatMarkdown(state.aiStylistResult.text)}</div>
      </div>
    ` : ''}

    <!-- Saved Wardrobe Lookbooks Gallery (Android Parity) -->
    <div class="saved-lookbooks-section">
      <div style="font-size:14px; font-weight:700; color:var(--text-primary); margin-bottom:4px;">
        Saved Wardrobe Lookbooks (${state.savedOutfits.length})
      </div>
      
      ${state.savedOutfits.length === 0 ? `
        <div style="background:var(--surface); border:1px dashed var(--border); border-radius:var(--radius-lg); padding:24px 16px; text-align:center; margin-top:8px;">
          <div style="font-size:28px; margin-bottom:8px;">👗</div>
          <div style="font-size:13px; font-weight:700; color:var(--text-primary);">No saved outfits yet</div>
          <div style="font-size:11px; color:var(--text-muted); margin-top:4px; max-width:280px; margin-left:auto; margin-right:auto;">
            Craft ensembles in Virtual Try-On and tap 'Save Lookbook' to curate your wardrobe.
          </div>
        </div>
      ` : `
        <div style="display:flex; flex-direction:column; gap:8px;">
          ${state.savedOutfits.map(o => `
            <div class="saved-look-card">
              <div>
                <div style="font-size:13px; font-weight:700; color:white;">${o.name}</div>
                <div style="font-size:11px; color:var(--text-muted); margin-top:2px;">
                  ${o.itemCount || 3} Pieces • Saved ${o.date || 'Today'}
                </div>
                <div style="font-size:11px; color:var(--sage-green); font-weight:700; margin-top:4px;">
                  Harmony Score: ${o.harmonyScore || 94}%
                </div>
              </div>
              <div style="display:flex; gap:6px; align-items:center;">
                <button style="background:rgba(164,137,250,0.15); border:1px solid rgba(164,137,250,0.4); color:var(--primary-glow); padding:6px 10px; border-radius:6px; font-size:11px; font-weight:600; cursor:pointer;" 
                        onclick="window.vogueApp.switchTab('try-on')">
                  View in 3D
                </button>
                <button style="background:none; border:none; color:var(--text-muted); font-size:16px; padding:4px 6px; cursor:pointer;" 
                        title="Delete Outfit"
                        onclick="window.vogueApp.deleteSavedOutfit('${o.id}')">
                  ✕
                </button>
              </div>
            </div>
          `).join('')}
        </div>
      `}
    </div>
  `;
}

// 4. VIP ATELIER TAB
function renderVipTab() {
  const currentTier = SUBSCRIPTION_TIERS.find(t => t.id === state.userProfile.subscriptionTierName) || SUBSCRIPTION_TIERS[1];

  return `
    <div style="padding: 16px 20px 8px; display:flex; justify-content:space-between; align-items:center;">
      <div>
        <h2 style="font-size:18px; font-weight:700; color:var(--text-primary); letter-spacing:0.5px;">VIP ATELIER & STYLISTS</h2>
        <p style="font-size:11px; color:var(--text-muted);">Certified Human Stylists & Premium Subscriptions</p>
      </div>
      <span style="background:var(--primary); color:white; font-size:10px; font-weight:800; padding:4px 8px; border-radius:6px;">
        ${currentTier.title}
      </span>
    </div>

    <!-- Active Membership Status Banner -->
    <div class="vip-banner">
      <div>
        <div style="font-size:10px; color:var(--primary-light);">Current Membership Plan</div>
        <div style="font-size:18px; font-weight:700; color:white; font-family:var(--font-serif);">${currentTier.title}</div>
        <div style="font-size:11px; color:rgba(255,255,255,0.7); margin-top:2px;">
          ${currentTier.monthlyPrice === 0 ? 'Free Essential Tier' : `$${currentTier.monthlyPrice}/month`}
        </div>
      </div>
      <button style="background:var(--primary-light); color:var(--primary-dark); border:none; padding:8px 14px; border-radius:8px; font-size:12px; font-weight:700; cursor:pointer;" 
              onclick="window.vogueApp.openModal('tier-upgrade')">
        Change Plan
      </button>
    </div>

    <!-- Stylists List -->
    <h3 class="section-heading" style="margin-top:10px;">Private Human Consultants</h3>
    ${STYLISTS.map(s => `
      <div class="stylist-card">
        <img src="${s.imageUrl}" alt="${s.name}" class="stylist-avatar">
        <div class="stylist-info">
          <div class="stylist-name">${s.name}</div>
          <div class="stylist-title">${s.title} • ${s.location}</div>
          <div style="font-size:11px; color:var(--text-primary); margin-top:4px;">${s.bio}</div>
          <div class="stylist-specialties">
            ${s.specialties.map(sp => `<span class="spec-badge">${sp}</span>`).join('')}
          </div>
          <div style="display:flex; justify-content:space-between; align-items:center; margin-top:10px;">
            <div>
              <span style="font-size:14px; font-weight:700; color:var(--primary);">$${s.hourlyRate}</span>
              <span style="font-size:10px; color:var(--text-muted);">/hr session</span>
            </div>
            <button style="background:var(--primary); color:white; border:none; padding:6px 14px; border-radius:6px; font-size:11px; font-weight:600; cursor:pointer;" 
                    onclick="window.vogueApp.openModal('book-stylist', '${s.id}')">
              Book Consultation
            </button>
          </div>
        </div>
      </div>
    `).join('')}

    <!-- Bookings List -->
    ${state.stylistBookings.length > 0 ? `
      <h3 class="section-heading" style="margin-top:10px;">Your Scheduled Consultations</h3>
      <div style="padding:0 16px;">
        ${state.stylistBookings.map(b => `
          <div style="background:var(--surface); border:1px solid var(--border); border-radius:8px; padding:12px; margin-bottom:8px; display:flex; justify-content:space-between; align-items:center;">
            <div>
              <div style="font-size:12px; font-weight:700;">${b.stylistName}</div>
              <div style="font-size:11px; color:var(--text-muted);">${b.sessionType} • ${b.dateTime}</div>
            </div>
            <span style="background:var(--sage-light); color:var(--sage-green); font-size:10px; font-weight:700; padding:2px 6px; border-radius:4px;">Confirmed</span>
          </div>
        `).join('')}
      </div>
    ` : ''}
  `;
}

// 5. CLOSET & FIT PROFILE TAB
function renderClosetTab() {
  const profile = state.userProfile;
  const cartTotal = state.cartItems.reduce((acc, i) => acc + (i.price * i.quantity), 0);
  const cartOriginal = state.cartItems.reduce((acc, i) => acc + (i.originalPrice * i.quantity), 0);
  const cartSavings = Math.max(0, cartOriginal - cartTotal);

  return `
    <div style="padding: 16px 20px 8px; display:flex; justify-content:space-between; align-items:center;">
      <div>
        <h2 style="font-size:18px; font-weight:700; color:var(--text-primary); letter-spacing:0.5px;">MY ATELIER & FIT PROFILE</h2>
        <p style="font-size:11px; color:var(--text-muted);">Body Silhouette, Multi-Store Cart & Price Alerts</p>
      </div>
      <button class="header-icon-btn" onclick="window.vogueApp.openModal('fit-profile')">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M12 20h9"></path>
          <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path>
        </svg>
      </button>
    </div>

    <!-- Fit Profile Overview Card -->
    <div class="settings-card" style="border:1.5px solid var(--primary);">
      <div style="display:flex; align-items:center; gap:12px;">
        <div style="width:44px; height:44px; border-radius:50%; background:var(--primary-light); color:var(--primary); display:flex; align-items:center; justify-content:center;">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
            <circle cx="12" cy="7" r="4"></circle>
          </svg>
        </div>
        <div>
          <div style="font-size:15px; font-weight:700;">${profile.name}</div>
          <div style="font-size:11px; color:var(--text-muted);">${profile.heightCm} cm • ${profile.weightKg} kg • ${profile.bodyTypeName}</div>
        </div>
      </div>

      <div style="display:grid; grid-template-columns:repeat(3, 1fr); gap:8px; margin-top:12px; background:var(--bg-primary); padding:10px; border-radius:8px;">
        <div style="text-align:center;">
          <div style="font-size:10px; color:var(--text-muted);">Bust</div>
          <div style="font-size:14px; font-weight:700; color:var(--primary);">${profile.bustCm} cm</div>
        </div>
        <div style="text-align:center;">
          <div style="font-size:10px; color:var(--text-muted);">Waist</div>
          <div style="font-size:14px; font-weight:700; color:var(--primary);">${profile.waistCm} cm</div>
        </div>
        <div style="text-align:center;">
          <div style="font-size:10px; color:var(--text-muted);">Hips</div>
          <div style="font-size:14px; font-weight:700; color:var(--primary);">${profile.hipsCm} cm</div>
        </div>
      </div>
    </div>

    <!-- Multi-Store Shopping Cart Section -->
    <h3 class="section-heading" style="margin-top:10px;">
      Multi-Store Bag (${state.cartItems.length} items)
    </h3>
    <div style="padding:0 16px;">
      ${state.cartItems.length === 0 ? `
        <div style="text-align:center; padding:24px; background:var(--surface); border-radius:12px; border:1px dashed var(--border); color:var(--text-muted); font-size:13px;">
          Your bag is currently empty. Explore our catalog or Try-On studio to add garments.
        </div>
      ` : `
        <div style="background:var(--surface); border:1px solid var(--border); border-radius:12px; overflow:hidden;">
          ${state.cartItems.map(item => `
            <div style="display:flex; gap:12px; padding:12px; border-bottom:1px solid var(--border-subtle); align-items:center;">
              <img src="${item.imageUrl}" alt="${item.productName}" style="width:60px; height:60px; object-fit:cover; border-radius:6px;">
              <div style="flex:1;">
                <div style="font-size:10px; color:var(--primary); font-weight:700;">${item.storeName.toUpperCase()}</div>
                <div style="font-size:12px; font-weight:600; line-height:1.2;">${item.productName}</div>
                <div style="font-size:11px; color:var(--text-muted); margin-top:2px;">Size ${item.selectedSize} • ${item.selectedColor}</div>
                <div style="font-size:13px; font-weight:700; color:var(--sage-green); margin-top:4px;">$${Math.round(item.price)}</div>
              </div>
              <div style="display:flex; align-items:center; gap:6px;">
                <button style="width:24px; height:24px; border-radius:4px; border:1px solid var(--border); background:var(--bg-primary); cursor:pointer;" 
                        onclick="window.vogueApp.updateCartQuantity('${item.id}', -1)">-</button>
                <span style="font-size:12px; font-weight:700; width:16px; text-align:center;">${item.quantity}</span>
                <button style="width:24px; height:24px; border-radius:4px; border:1px solid var(--border); background:var(--bg-primary); cursor:pointer;" 
                        onclick="window.vogueApp.updateCartQuantity('${item.id}', 1)">+</button>
              </div>
            </div>
          `).join('')}
          <div style="padding:14px; background:var(--surface-variant);">
            <div style="display:flex; justify-content:space-between; font-size:12px;">
              <span style="color:var(--text-muted);">Estimated Savings:</span>
              <span style="color:var(--sage-green); font-weight:700;">-$${Math.round(cartSavings)}</span>
            </div>
            <div style="display:flex; justify-content:space-between; font-size:15px; font-weight:700; margin-top:4px;">
              <span>Total Across Retailers:</span>
              <span>$${Math.round(cartTotal)}</span>
            </div>
            <button style="width:100%; margin-top:10px; background:var(--primary); color:white; border:none; padding:10px; border-radius:8px; font-weight:700; cursor:pointer;" 
                    onclick="window.vogueApp.simulateCheckout()">
              Proceed to Synchronized Checkout
            </button>
          </div>
        </div>
      `}
    </div>

    <!-- Active Price Alerts -->
    <h3 class="section-heading" style="margin-top:16px;">
      Tracked Price Drop Alerts (${state.priceAlerts.length})
    </h3>
    <div style="padding:0 16px;">
      ${state.priceAlerts.length === 0 ? `
        <div style="font-size:12px; color:var(--text-muted); padding:10px; text-align:center;">No active price alerts set yet.</div>
      ` : `
        ${state.priceAlerts.map(a => `
          <div style="background:var(--surface); border:1px solid var(--border); border-radius:8px; padding:10px 14px; margin-bottom:8px; display:flex; justify-content:space-between; align-items:center;">
            <div>
              <div style="font-size:12px; font-weight:700;">${a.productName}</div>
              <div style="font-size:11px; color:var(--text-muted);">Target: <b style="color:var(--sage-green);">$${a.targetPrice}</b> (Current Lowest: $${Math.round(a.currentLowest)})</div>
            </div>
            <button style="background:none; border:none; color:var(--deal-red); font-size:14px; cursor:pointer;" 
                    onclick="window.vogueApp.removePriceAlert('${a.id}')">✕</button>
          </div>
        `).join('')}
      `}
    </div>

    <!-- Saved Outfits / Lookbooks -->
    <h3 class="section-heading" style="margin-top:16px;">
      Saved Lookbooks (${state.savedOutfits.length})
    </h3>
    <div style="padding:0 16px;">
      ${state.savedOutfits.length === 0 ? `
        <div style="font-size:12px; color:var(--text-muted); padding:10px; text-align:center;">No lookbooks saved yet. Use the Try-On studio to save ensembles.</div>
      ` : `
        ${state.savedOutfits.map(o => `
          <div style="background:var(--surface); border:1px solid var(--border); border-radius:8px; padding:12px; margin-bottom:8px;">
            <div style="display:flex; justify-content:space-between; align-items:center;">
              <div style="font-size:13px; font-weight:700;">${o.name}</div>
              <span style="font-size:11px; color:var(--primary); font-weight:700;">${o.harmonyScore}% Harmony</span>
            </div>
            <div style="font-size:11px; color:var(--text-muted); margin-top:2px;">${o.itemCount} layered items • Saved on ${o.date}</div>
            <div style="display:flex; gap:6px; margin-top:8px;">
              ${o.items.map(item => `
                <img src="${item.imageUrl}" alt="${item.name}" style="width:36px; height:36px; border-radius:4px; object-fit:cover;">
              `).join('')}
            </div>
          </div>
        `).join('')}
      `}
    </div>
  `;
}

// 6. PRODUCT DETAIL SCREEN OVERLAY
function renderProductDetailOverlay() {
  const p = state.selectedProduct;
  const fitAdvice = calculateFitAdvice(p, state.userProfile);

  return `
    <div class="product-detail-view">
      <div class="detail-header">
        <button style="background:none; border:none; cursor:pointer;" onclick="window.vogueApp.closeProductDetail()">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="m15 18-6-6 6-6"></path>
          </svg>
        </button>
        <span style="font-size:12px; font-weight:700; letter-spacing:1px; text-transform:uppercase; color:var(--primary);">${p.brand}</span>
        <button style="background:none; border:none; cursor:pointer; color:var(--primary);" 
                onclick="window.vogueApp.openModal('price-alert', '${p.id}')">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"></path>
            <path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"></path>
          </svg>
        </button>
      </div>

      <div class="detail-img-box">
        <img src="${p.imageUrl}" alt="${p.name}" class="detail-img">
      </div>

      <div class="detail-content">
        <div style="display:flex; justify-content:space-between; align-items:baseline;">
          <div>
            <span class="deal-badge" style="position:static;">-${p.priceDropPercent}% OFF</span>
            <h2 style="font-size:18px; font-weight:700; margin-top:6px; color:var(--text-primary); font-family:var(--font-serif);">${p.name}</h2>
          </div>
          <div style="text-align:right;">
            <div style="font-size:22px; font-weight:700; color:var(--sage-green);">$${Math.round(p.lowestPrice)}</div>
            <div style="font-size:12px; color:var(--text-muted); text-decoration:line-through;">$${Math.round(p.originalPrice)}</div>
          </div>
        </div>

        <p style="font-size:13px; color:var(--text-secondary); margin-top:8px; line-height:1.5;">${p.description}</p>

        <!-- Multi-Store Price Comparison Table -->
        <h4 style="font-size:13px; font-weight:700; margin-top:16px;">Multi-Store Price Comparison</h4>
        <table class="comparison-table">
          <thead>
            <tr>
              <th>Retailer</th>
              <th>Price</th>
              <th>Shipping</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            ${p.storeOffers.map((offer, idx) => `
              <tr>
                <td><b>${offer.storeName}</b> ${idx === 0 ? '<span class="best-price-badge">Best</span>' : ''}</td>
                <td style="color:${idx === 0 ? 'var(--sage-green)' : 'inherit'}; font-weight:700;">$${Math.round(offer.price)}</td>
                <td style="font-size:10px; color:var(--text-muted);">${offer.shippingInfo}</td>
                <td><span style="color:${offer.inStock ? 'var(--sage-green)' : 'var(--deal-red)'}; font-weight:600;">${offer.inStock ? 'In Stock' : 'Sold Out'}</span></td>
              </tr>
            `).join('')}
          </tbody>
        </table>

        <!-- AI Sizing & Fit Advisor Card -->
        <div class="settings-card" style="margin:16px 0; border:1px solid var(--primary-container);">
          <div style="display:flex; align-items:center; gap:6px; font-size:13px; font-weight:700; color:var(--primary);">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21.21 15.89A10 10 0 1 1 8 2.83"></path>
              <path d="M22 12A10 10 0 0 0 12 2v10z"></path>
            </svg>
            AI Silhouette & Size Advisor
          </div>
          <div style="font-size:14px; font-weight:700; margin-top:6px;">
            Recommended Size: <span style="color:var(--primary);">${fitAdvice.recommendedSize}</span> 
            <span style="font-size:11px; font-weight:500; color:var(--sage-green); font-family:var(--font-sans);">(${fitAdvice.confidence}% Fit Match)</span>
          </div>
          <div style="font-size:12px; color:var(--text-secondary); margin-top:4px;">${fitAdvice.summary}</div>
          <div style="display:flex; gap:8px; font-size:11px; color:var(--text-muted); margin-top:8px;">
            <div>• Bust: <b>${fitAdvice.bustFit}</b></div>
            <div>• Waist: <b>${fitAdvice.waistFit}</b></div>
          </div>
        </div>

        <!-- Available Colors -->
        <div style="margin-top:12px;">
          <div style="font-size:12px; font-weight:700;">Available Tones:</div>
          <div style="display:flex; gap:8px; margin-top:6px;">
            ${p.availableColors.map(c => `
              <div style="display:flex; align-items:center; gap:6px; background:var(--surface); border:1px solid var(--border); padding:4px 10px; border-radius:var(--radius-pill); font-size:11px;">
                <span style="width:12px; height:12px; border-radius:50%; background:${c.hex}; display:inline-block; border:1px solid rgba(0,0,0,0.1);"></span>
                <span>${c.name}</span>
              </div>
            `).join('')}
          </div>
        </div>
      </div>

      <!-- Sticky Action Bottom Bar -->
      <div class="detail-sticky-bar">
        <button style="flex:1; background:transparent; border:1px solid var(--primary); color:var(--primary); padding:12px; border-radius:8px; font-weight:700; cursor:pointer;" 
                onclick="window.vogueApp.sendToTryOn(PRODUCTS.find(x => x.id === '${p.id}'))">
          3D Try-On
        </button>
        <button style="flex:1.4; background:var(--primary); color:white; border:none; padding:12px; border-radius:8px; font-weight:700; cursor:pointer;" 
                onclick="window.vogueApp.addToCart(PRODUCTS.find(x => x.id === '${p.id}'), '${fitAdvice.recommendedSize}')">
          Buy on ${p.storeOffers[0].storeName} ($${Math.round(p.lowestPrice)})
        </button>
      </div>
    </div>
  `;
}

// 7. MODALS
function renderModal() {
  if (!state.activeModal) return '';

  return `
    <div class="modal-overlay" onclick="window.vogueApp.closeModal()">
      <div class="modal-sheet" onclick="event.stopPropagation()">
        ${renderModalContent()}
      </div>
    </div>
  `;
}

function renderModalContent() {
  switch (state.activeModal) {
    case 'fit-profile': {
      const p = state.userProfile;
      return `
        <div class="modal-header">
          <div class="modal-title">Edit Fit & Silhouette Profile</div>
          <button class="close-btn" onclick="window.vogueApp.closeModal()">✕</button>
        </div>
        <div style="display:flex; flex-direction:column; gap:12px;">
          <div>
            <label style="font-size:12px; font-weight:600;">Full Name</label>
            <input type="text" id="prof-name" value="${p.name}" style="width:100%; padding:8px; border-radius:6px; border:1px solid var(--border); font-size:13px; margin-top:4px;">
          </div>
          <div style="display:flex; gap:10px;">
            <div style="flex:1;">
              <label style="font-size:12px; font-weight:600;">Height (cm)</label>
              <input type="number" id="prof-height" value="${p.heightCm}" style="width:100%; padding:8px; border-radius:6px; border:1px solid var(--border); font-size:13px; margin-top:4px;">
            </div>
            <div style="flex:1;">
              <label style="font-size:12px; font-weight:600;">Weight (kg)</label>
              <input type="number" id="prof-weight" value="${p.weightKg}" style="width:100%; padding:8px; border-radius:6px; border:1px solid var(--border); font-size:13px; margin-top:4px;">
            </div>
          </div>
          <div style="display:flex; gap:8px;">
            <div style="flex:1;">
              <label style="font-size:11px; font-weight:600;">Bust (cm)</label>
              <input type="number" id="prof-bust" value="${p.bustCm}" style="width:100%; padding:8px; border-radius:6px; border:1px solid var(--border); font-size:13px; margin-top:4px;">
            </div>
            <div style="flex:1;">
              <label style="font-size:11px; font-weight:600;">Waist (cm)</label>
              <input type="number" id="prof-waist" value="${p.waistCm}" style="width:100%; padding:8px; border-radius:6px; border:1px solid var(--border); font-size:13px; margin-top:4px;">
            </div>
            <div style="flex:1;">
              <label style="font-size:11px; font-weight:600;">Hips (cm)</label>
              <input type="number" id="prof-hips" value="${p.hipsCm}" style="width:100%; padding:8px; border-radius:6px; border:1px solid var(--border); font-size:13px; margin-top:4px;">
            </div>
          </div>
          <div>
            <label style="font-size:12px; font-weight:600;">Body Type</label>
            <select id="prof-body" style="width:100%; padding:8px; border-radius:6px; border:1px solid var(--border); font-size:13px; margin-top:4px;">
              ${BODY_TYPES.map(b => `
                <option value="${b.id}" ${p.bodyTypeName === b.id ? 'selected' : ''}>${b.name}</option>
              `).join('')}
            </select>
          </div>
          <div>
            <label style="font-size:12px; font-weight:600;">Skin Undertone</label>
            <select id="prof-skin" style="width:100%; padding:8px; border-radius:6px; border:1px solid var(--border); font-size:13px; margin-top:4px;">
              ${SKIN_TONES.map(s => `
                <option value="${s.id}" ${p.skinToneName === s.id ? 'selected' : ''}>${s.name}</option>
              `).join('')}
            </select>
          </div>
          <button style="margin-top:10px; background:var(--primary); color:white; border:none; padding:10px; border-radius:8px; font-weight:700; cursor:pointer;" 
                  onclick="window.vogueApp.saveProfileForm()">
            Save Silhouette Measurements
          </button>
        </div>
      `;
    }

    case 'price-alert': {
      const prod = PRODUCTS.find(p => p.id === state.modalContext) || PRODUCTS[0];
      return `
        <div class="modal-header">
          <div class="modal-title">Track Price Drop</div>
          <button class="close-btn" onclick="window.vogueApp.closeModal()">✕</button>
        </div>
        <div style="display:flex; flex-direction:column; gap:12px;">
          <p style="font-size:12px; color:var(--text-secondary);">
            We will notify you the moment <b>${prod.name}</b> drops below your target threshold across partner retailers (Zara, Farfetch, Nordstrom, ASOS).
          </p>
          <div style="font-size:12px; font-weight:700; color:var(--sage-green);">Current Lowest: $${Math.round(prod.lowestPrice)}</div>
          <div>
            <label style="font-size:12px; font-weight:600;">Your Target Price ($)</label>
            <input type="number" id="alert-price" value="${Math.round(prod.lowestPrice * 0.85)}" style="width:100%; padding:8px; border-radius:6px; border:1px solid var(--border); font-size:14px; margin-top:4px;">
          </div>
          <button style="margin-top:10px; background:var(--primary); color:white; border:none; padding:10px; border-radius:8px; font-weight:700; cursor:pointer;" 
                  onclick="window.vogueApp.addPriceAlert(PRODUCTS.find(p => p.id === '${prod.id}'), document.getElementById('alert-price').value)">
            Set Real-Time Price Alert
          </button>
        </div>
      `;
    }

    case 'book-stylist': {
      const stylist = STYLISTS.find(s => s.id === state.modalContext) || STYLISTS[0];
      return `
        <div class="modal-header">
          <div class="modal-title">Book 1-on-1 Consultation</div>
          <button class="close-btn" onclick="window.vogueApp.closeModal()">✕</button>
        </div>
        <div style="display:flex; flex-direction:column; gap:12px;">
          <div style="display:flex; align-items:center; gap:10px;">
            <img src="${stylist.imageUrl}" alt="${stylist.name}" style="width:48px; height:48px; border-radius:50%; object-fit:cover;">
            <div>
              <div style="font-size:14px; font-weight:700;">${stylist.name}</div>
              <div style="font-size:11px; color:var(--text-muted);">$${stylist.hourlyRate}/hr • ${stylist.location}</div>
            </div>
          </div>
          <div>
            <label style="font-size:12px; font-weight:600;">Session Type</label>
            <select id="book-type" style="width:100%; padding:8px; border-radius:6px; border:1px solid var(--border); font-size:13px; margin-top:4px;">
              <option>1-on-1 Capsule Wardrobe Architecture (60m)</option>
              <option>Event Gala & Red Carpet Styling (45m)</option>
              <option>Seasonal Undertone Color Science Audit (30m)</option>
            </select>
          </div>
          <div>
            <label style="font-size:12px; font-weight:600;">Preferred Date & Time</label>
            <input type="text" id="book-time" value="Tomorrow at 3:00 PM" style="width:100%; padding:8px; border-radius:6px; border:1px solid var(--border); font-size:13px; margin-top:4px;">
          </div>
          <button style="margin-top:10px; background:var(--primary); color:white; border:none; padding:10px; border-radius:8px; font-weight:700; cursor:pointer;" 
                  onclick="window.vogueApp.confirmStylistBooking('${stylist.name}')">
            Confirm Booking ($${stylist.hourlyRate})
          </button>
        </div>
      `;
    }

    case 'slot-picker': {
      const category = state.modalContext;
      const slotProducts = PRODUCTS.filter(p => p.category === category);
      return `
        <div class="modal-header">
          <div class="modal-title">Select ${CATEGORY_LABELS[category] || 'Garment'}</div>
          <button class="close-btn" onclick="window.vogueApp.closeModal()">✕</button>
        </div>
        <div style="display:flex; flex-direction:column; gap:8px;">
          ${slotProducts.map(p => `
            <div style="display:flex; gap:10px; padding:8px; border:1px solid var(--border); border-radius:8px; align-items:center; cursor:pointer;" 
                 onclick="window.vogueApp.sendToTryOn(PRODUCTS.find(x => x.id === '${p.id}')); window.vogueApp.closeModal();">
              <img src="${p.imageUrl}" alt="${p.name}" style="width:48px; height:48px; object-fit:cover; border-radius:4px;">
              <div style="flex:1;">
                <div style="font-size:11px; font-weight:600; color:var(--primary);">${p.brand}</div>
                <div style="font-size:12px; font-weight:700;">${p.name}</div>
                <div style="font-size:12px; color:var(--sage-green); font-weight:700;">$${Math.round(p.lowestPrice)}</div>
              </div>
              <button style="background:var(--primary); color:white; border:none; padding:6px 12px; border-radius:6px; font-size:11px; font-weight:600;">Drape</button>
            </div>
          `).join('')}
        </div>
      `;
    }

    case 'save-outfit': {
      return `
        <div class="modal-header">
          <div class="modal-title">Save Current Ensemble</div>
          <button class="close-btn" onclick="window.vogueApp.closeModal()">✕</button>
        </div>
        <div style="display:flex; flex-direction:column; gap:12px;">
          <div>
            <label style="font-size:12px; font-weight:600;">Lookbook Name</label>
            <input type="text" id="look-name" value="Autumn Gala Chic" style="width:100%; padding:8px; border-radius:6px; border:1px solid var(--border); font-size:13px; margin-top:4px;">
          </div>
          <button style="background:var(--primary); color:white; border:none; padding:10px; border-radius:8px; font-weight:700; cursor:pointer;" 
                  onclick="window.vogueApp.saveCurrentOutfit(document.getElementById('look-name').value)">
            Save to My Wardrobe
          </button>
        </div>
      `;
    }

    case 'tier-upgrade': {
      return `
        <div class="modal-header">
          <div class="modal-title">VIP Atelier Membership</div>
          <button class="close-btn" onclick="window.vogueApp.closeModal()">✕</button>
        </div>
        <div style="display:flex; flex-direction:column; gap:12px;">
          ${SUBSCRIPTION_TIERS.map(tier => `
            <div style="border:1.5px solid ${state.userProfile.subscriptionTierName === tier.id ? 'var(--primary)' : 'var(--border)'}; border-radius:10px; padding:12px; background:${state.userProfile.subscriptionTierName === tier.id ? 'var(--primary-subtle)' : 'var(--surface)'};">
              <div style="display:flex; justify-content:space-between; align-items:center;">
                <div style="font-size:14px; font-weight:700; font-family:var(--font-serif);">${tier.title}</div>
                <div style="font-size:13px; font-weight:700; color:var(--primary);">${tier.monthlyPrice === 0 ? 'Free' : `$${tier.monthlyPrice}/mo`}</div>
              </div>
              <p style="font-size:11px; color:var(--text-secondary); margin:4px 0 8px;">${tier.description}</p>
              <div style="font-size:10px; color:var(--text-muted);">
                ${tier.features.map(f => `<div>✓ ${f}</div>`).join('')}
              </div>
              <button style="margin-top:8px; width:100%; background:${state.userProfile.subscriptionTierName === tier.id ? 'var(--surface)' : 'var(--primary)'}; color:${state.userProfile.subscriptionTierName === tier.id ? 'var(--text-primary)' : 'white'}; border:1px solid var(--primary); padding:6px; border-radius:6px; font-size:11px; font-weight:600; cursor:pointer;" 
                      onclick="window.vogueApp.selectTier('${tier.id}')">
                ${state.userProfile.subscriptionTierName === tier.id ? 'Current Active Tier' : 'Upgrade to Plan'}
              </button>
            </div>
          `).join('')}
        </div>
      `;
    }

    case 'google-signin': {
      return `
        <div class="modal-header">
          <div style="display:flex; align-items:center; gap:8px;">
            <svg width="20" height="20" viewBox="0 0 24 24">
              <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
              <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
              <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"/>
              <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"/>
            </svg>
            <div class="modal-title">Sign in with Google</div>
          </div>
          <button class="close-btn" onclick="window.vogueApp.continueAsGuest()">✕</button>
        </div>
        
        <div style="display:flex; flex-direction:column; gap:14px; text-align:center;">
          <p style="font-size:12px; color:var(--text-secondary); line-height:1.5;">
            Connect your Google account to sync your 3D wardrobe lookbooks, tailored fit silhouettes, and price alerts across devices.
          </p>

          <!-- Mockup Google Account Selector Card -->
          <div class="google-account-card" onclick="window.vogueApp.signInWithGoogle()">
            <div class="google-user-avatar large">M</div>
            <div style="flex:1; text-align:left;">
              <div style="font-size:13px; font-weight:700; color:var(--text-primary);">Mahi Patel</div>
              <div style="font-size:11px; color:var(--text-muted);">mahipatel2959@gmail.com</div>
            </div>
            <span class="google-sync-badge">Mock Google Test</span>
          </div>

          <!-- Main Sign In Button Requested by User -->
          <button class="google-btn-full" ${state.googleAuth.isAuthenticating ? 'disabled' : ''} 
                  onclick="window.vogueApp.signInWithGoogle()">
            <svg width="18" height="18" viewBox="0 0 24 24">
              <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
              <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
              <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"/>
              <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"/>
            </svg>
            <span>${state.googleAuth.isAuthenticating ? 'Connecting to Google...' : 'Sign in as Google & Continue'}</span>
          </button>

          <button class="google-guest-btn" onclick="window.vogueApp.continueAsGuest()">
            Continue as Guest (Explore app)
          </button>
        </div>
      `;
    }

    case 'google-account': {
      return `
        <div class="modal-header">
          <div class="modal-title">Google Account</div>
          <button class="close-btn" onclick="window.vogueApp.closeModal()">✕</button>
        </div>
        <div style="display:flex; flex-direction:column; gap:14px; text-align:center;">
          <div style="display:flex; align-items:center; gap:12px; background:var(--surface-variant); padding:14px; border-radius:12px; text-align:left;">
            <div class="google-user-avatar large">M</div>
            <div>
              <div style="font-size:14px; font-weight:700; color:var(--text-primary);">${state.googleAuth.name}</div>
              <div style="font-size:12px; color:var(--text-muted);">${state.googleAuth.email}</div>
              <div style="font-size:11px; color:var(--sage-green); font-weight:600; margin-top:2px;">✓ Connected via Google Auth Mockup</div>
            </div>
          </div>

          <div style="background:var(--surface); border:1px solid var(--border); border-radius:8px; padding:10px; font-size:11px; color:var(--text-secondary); text-align:left;">
            Synced Wardrobe Looks: <b>${state.savedOutfits.length} items</b><br>
            Cart Saved Items: <b>${state.cartItems.length} items</b><br>
            Price Drop Trackers: <b>${state.priceAlerts.length} items</b>
          </div>

          <button class="google-signout-btn" onclick="window.vogueApp.signOutGoogle()">
            Sign Out from Google
          </button>
        </div>
      `;
    }

    default:
      return '';
  }
}

// Helpers
function formatMarkdown(text) {
  if (!text) return '';
  return text
    .replace(/\*\*(.*?)\*\*/g, '<b>$1</b>')
    .replace(/\*(.*?)\*/g, '<i>$1</i>');
}

// Global App Bindings for DOM Event Handlers
window.vogueApp = {
  switchTab,
  setCategory: (cat) => { state.selectedCategory = cat; render(); },
  setAesthetic: (aes) => { state.selectedAesthetic = aes; render(); },
  setSearch: (q) => { state.searchQuery = q; render(); },
  viewProduct,
  closeProductDetail,
  addToCart,
  removeFromCart,
  updateCartQuantity,
  addPriceAlert,
  removePriceAlert,
  sendToTryOn,
  removeTryOnSlot,
  saveCurrentOutfit,
  deleteSavedOutfit: (id) => {
    state.savedOutfits = state.savedOutfits.filter(o => o.id !== id);
    saveState('vogue_saved_outfits', state.savedOutfits);
    showToast('Lookbook removed from wardrobe');
    render();
  },
  addAllTryOnToCart,
  generateAiEnsemble,
  openModal,
  closeModal,
  openGoogleSignIn: () => {
    openModal('google-signin');
  },
  signInWithGoogle: () => {
    state.googleAuth.isAuthenticating = true;
    render();
    setTimeout(() => {
      state.googleAuth.isAuthenticating = false;
      state.googleAuth.isSignedIn = true;
      saveState('vogue_google_auth', state.googleAuth);
      sessionStorage.setItem('vogue_dismissed_auth', 'true');
      closeModal();
      showToast('✓ Signed in with Google as Mahi Patel');
      render();
    }, 600);
  },
  signOutGoogle: () => {
    state.googleAuth.isSignedIn = false;
    saveState('vogue_google_auth', state.googleAuth);
    closeModal();
    showToast('Signed out of Google account');
    render();
  },
  continueAsGuest: () => {
    sessionStorage.setItem('vogue_dismissed_auth', 'true');
    closeModal();
    showToast('Continuing as Guest');
    render();
  },
  updateTryOnAvatar: (bodyType, skinTone, gender) => {
    if (bodyType) state.tryOnState.bodyType = bodyType;
    if (skinTone) state.tryOnState.skinTone = skinTone;
    if (gender) state.tryOnState.gender = gender;
    render();
  },
  saveProfileForm: () => {
    state.userProfile.name = document.getElementById('prof-name').value;
    state.userProfile.heightCm = parseInt(document.getElementById('prof-height').value) || 172;
    state.userProfile.weightKg = parseInt(document.getElementById('prof-weight').value) || 60;
    state.userProfile.bustCm = parseInt(document.getElementById('prof-bust').value) || 88;
    state.userProfile.waistCm = parseInt(document.getElementById('prof-waist').value) || 68;
    state.userProfile.hipsCm = parseInt(document.getElementById('prof-hips').value) || 95;
    state.userProfile.bodyTypeName = document.getElementById('prof-body').value;
    state.userProfile.skinToneName = document.getElementById('prof-skin').value;
    saveState('vogue_profile', state.userProfile);
    showToast('Fit measurements updated');
    closeModal();
    render();
  },
  confirmStylistBooking: (stylistName) => {
    const sessionType = document.getElementById('book-type').value;
    const dateTime = document.getElementById('book-time').value;
    state.stylistBookings.unshift({
      id: `book_${Date.now()}`,
      stylistName,
      sessionType,
      dateTime
    });
    saveState('vogue_bookings', state.stylistBookings);
    showToast(`Session booked with ${stylistName}`);
    closeModal();
    render();
  },
  selectTier: (tierId) => {
    state.userProfile.subscriptionTierName = tierId;
    saveState('vogue_profile', state.userProfile);
    showToast(`Updated to ${tierId} tier`);
    closeModal();
    render();
  },
  simulateCheckout: () => {
    showToast('Multi-Store Order Simulation initiated across Zara, Farfetch & Nordstrom!');
  }
};

// Bootstrap
document.addEventListener('DOMContentLoaded', () => {
  loadPersistentState();
  render();
});
