# VogueAI Web App

A standalone, mobile-first responsive web application that faithfully mirrors the Android app's **Clean Minimalism** design system, haute couture virtual try-on studio, multi-store price intelligence, and AI fashion styling.

## Features Replicated from Android
1. **Design System & Aesthetics**:
   - **Color Palette**: Clean Minimal Background (`#F7F9FC`), Pure White Surface (`#FFFFFF`), Primary Minimalist Violet (`#6750A4`), Violet Light (`#EADDFF`), Dark Atelier Card (`#211F26`), Sage Savings Green (`#2E7D32`), Deal Red (`#BA1A1A`).
   - **Typography**: Editorial Serif (`Playfair Display`, `Georgia`) for headlines & branding paired with clean Sans-serif (`Inter`) for body, labels, and prices.
   - **Corner Radii & Spacing**: 8px, 12px, 16px, 20px, 28px hero card, and 9999px pill buttons.

2. **5 Core Navigation Tabs**:
   - **Shop (`HomeScreen`)**: Curated luxury catalog, trending price drops (≥40% off), aesthetic filters (Quiet Luxury, Old Money, Parisian Chic, etc.), category filters, and live search.
   - **3D Try-On Studio (`VirtualTryOnScreen`)**: Virtual mannequin canvas, body silhouette selection (Hourglass, Rectangle, Pear, etc.), skin undertone selector, 5 garment layer slots (Outerwear, Top, Bottom, Shoes, Bag), live Harmony Score (96%), and AI Fit & Styling critique.
   - **AI Stylist (`OutfitStudioScreen`)**: Generative capsule styling powered by server-side Gemini API (`gemini-3.5-flash`), seasonal color harmonizer, and occasion presets.
   - **VIP Atelier (`StylistSubscriptionScreen`)**: Membership tiers (Aura Starter, Style Pro, VIP Atelier) and certified 1-on-1 human stylist booking.
   - **Fit & Bag (`MyClosetAndProfileScreen`)**: Measurement silhouette editor (bust, waist, hips), multi-store shopping cart with retailer synchronization (Zara, Farfetch, Nordstrom, ASOS), active price drop alerts, and saved lookbooks.

3. **Server-Side Gemini API Proxy**:
   - **Security**: The Gemini API key is accessed exclusively server-side via `process.env.GEMINI_API_KEY`. It is **never** exposed to client-side code.
   - **Endpoint**: `POST /api/gemini/stylist` proxies requests to `gemini-3.5-flash` with graceful fallback to the local styling intelligence engine when offline.

## Running the Web App

From the project root:
```bash
npm --prefix web install
npm --prefix web start
```

Or inside the `web` folder:
```bash
cd web
npm install
npm start
```
The app will be accessible at `http://localhost:3000`.
