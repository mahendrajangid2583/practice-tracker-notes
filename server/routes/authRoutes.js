import express from 'express';
import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import requireOwnerAuth from '../middleware/authMiddleware.js';

const router = express.Router();

// Hardcoded hash from env or fallback (safety check)
// Hardcoded hash from env or fallback (safety check)
// const PIN_HASH = process.env.OWNER_PIN_HASH; // REMOVED to force direct usage

// Debug Endpoint
router.get('/debug', (req, res) => {
    res.json({
        ownerPinHashExists: !!process.env.OWNER_PIN_HASH,
        ownerPinHashLength: process.env.OWNER_PIN_HASH?.length,
        jwtSecretExists: !!process.env.JWT_SECRET,
        bcryptVersion: "implied-bcryptjs"
    });
});

// Login: POST /auth/login
router.post('/login', async (req, res) => {
    // 1️⃣ PIN HANDLING (CRITICAL)
    console.log('[Auth] Login attempt. Body keys:', Object.keys(req.body));

    // Accept 'pin' OR 'password' to be flexible with different clients
    let cleanPin = req.body.pin || req.body.password;

    const pin = String(cleanPin);

    if (!cleanPin || pin === 'undefined') {
        console.log('[Auth] No PIN/Password provided in body');
        return res.status(400).json({ message: 'PIN/Password is required' });
    }

    if (!process.env.OWNER_PIN_HASH) {
        // console.error('SERVER ERROR: OWNER_PIN_HASH not set');
        console.error('SERVER ERROR: OWNER_PIN_HASH not set');
        return res.status(500).json({ message: 'Server configuration error' });
    }

    let isMatch = false;
    try {
        isMatch = await bcrypt.compare(pin, process.env.OWNER_PIN_HASH);
    } catch (err) {
        console.error("[AUTH] bcrypt.compare threw error:", err);
    }

    if (!isMatch) {
        console.log('[Auth] Incorrect PIN/Password');
        // We return 401 here, which matches the user's report.
        return res.status(401).json({ message: 'Incorrect PIN' });
    }

    // Generate JWT
    const token = jwt.sign(
        { sub: 'owner' },
        process.env.JWT_SECRET,
        { expiresIn: '1825d' } // 5 years (approx)
    );

    // Set secure cookie (for web)
    res.cookie('owner_auth_token', token, {
        httpOnly: true,
        secure: process.env.NODE_ENV === 'production',
        sameSite: process.env.NODE_ENV === 'production' ? 'None' : 'Lax',
        path: '/',
        maxAge: 5 * 365 * 24 * 60 * 60 * 1000 // 5 years
    });

    console.log('[Auth] Success. Token generated.');
    res.status(200).json({ message: 'Authenticated successfully', token });
});

// Check Auth: GET /auth/me
// Uses middleware to verify. If middleware passes, return 200.
router.get('/me', requireOwnerAuth, (req, res) => {
    res.status(200).json({ authenticated: true });
});

// Logout: POST /auth/logout
router.post('/logout', (req, res) => {
    res.clearCookie('owner_auth_token', {
        httpOnly: true,
        secure: process.env.NODE_ENV === 'production',
        sameSite: process.env.NODE_ENV === 'production' ? 'None' : 'Lax',
        path: '/'
    });
    res.status(200).json({ message: 'Logged out' });
});

export default router;
