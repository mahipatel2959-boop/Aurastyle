# Deploying VogueAI to Vercel

The `web` directory is configured as a standalone application ready for 1-click deployment on **Vercel** with full serverless support for both the static frontend and the Express backend (including the secure Gemini API proxy).

---

## Architecture on Vercel

```
web/
├── vercel.json           # Vercel configuration (rewrites /api/* to serverless function)
├── api/
│   └── index.js          # Serverless entrypoint exporting the Express app
├── server.js             # Express app, REST API routes & Gemini proxy
├── package.json          # Node dependencies & configuration
├── public/               # Static frontend assets
│   ├── index.html        # Entry HTML
│   ├── css/
│   │   └── style.css     # Clean Minimalism design system
│   └── js/
│       ├── app.js        # Single Page App (SPA) state and UI logic
│       └── data.js       # Data models, product catalog, stylist roster
└── README.md
```

### How Vercel Routes Traffic
- **`/api/*`** → routed via `web/vercel.json` to `web/api/index.js` (runs as a secure Vercel Serverless Function).
- **`/*`** (everything else) → served statically from `web/public/` with client-side SPA routing.

---

## Deployment Steps

### Method 1: Via Vercel Web Dashboard (Recommended)

1. **Push your code to GitHub / GitLab / Bitbucket**:
   - Make sure your repo contains the `web` folder.

2. **Import Project in Vercel**:
   - Go to [vercel.com](https://vercel.com) and click **"Add New Project"**.
   - Select your repository.

3. **Configure the Project**:
   - **Framework Preset**: Select **Other** (or leave as auto-detected).
   - **Root Directory**: Click *Edit* and select **`web`** *(CRITICAL: must be set to `web` so Vercel uses the web app files)*.
   - **Build Command**: Leave empty or default (`npm run build`).
   - **Output Directory**: Leave empty or default.

4. **Add Environment Variables**:
   - Under **Environment Variables**, add:
     - **Key**: `GEMINI_API_KEY`
     - **Value**: Your Google Gemini API Key.
   - *(Note: The API key stays safe on Vercel's serverless environment and is never delivered to client browsers)*.

5. **Click "Deploy"**:
   - Vercel will install dependencies, package the serverless function, and deploy your site to a `https://*.vercel.app` URL.

---

### Method 2: Via Vercel CLI

If you have `vercel` installed locally:

```bash
# Navigate to the web folder
cd web

# Deploy preview
vercel

# Add your Gemini secret when prompted, or link project and run:
vercel env add GEMINI_API_KEY

# Deploy to production
vercel --prod
```

---

## Testing Your Vercel Deployment

Once deployed:
1. **Frontend**: Open `https://<your-project>.vercel.app/` — you will see the Clean Minimalism VogueAI storefront.
2. **Health Check**: Open `https://<your-project>.vercel.app/api/health` — should return:
   ```json
   {
     "status": "ok",
     "app": "VogueAI Web App",
     "hasGeminiKey": true,
     "model": "gemini-3.5-flash",
     "modules": ["SizeAdvisorEngine", "TryOnCompositor", "GeminiStylistService", ...]
   }
   ```
3. **AI Stylist**: Go to the **AI Stylist** tab, enter an event/occasion prompt, and click **Generate Haute Couture Ensemble** to test the serverless Gemini integration.
