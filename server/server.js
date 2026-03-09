import express from 'express';
import mongoose from 'mongoose';
import cors from 'cors';
import dotenv from 'dotenv';
import cookieParser from 'cookie-parser';
import collectionRoutes from './routes/collectionRoutes.js';
import searchRoutes from './routes/searchRoutes.js';
import dailyTargetRoutes from './routes/dailyTargetRoutes.js';
import targetSettingsRoutes from './routes/targetSettingsRoutes.js';
import authRoutes from './routes/authRoutes.js';
import syncRoutes from './routes/syncRoutes.js';
import requireOwnerAuth from './middleware/authMiddleware.js';


dotenv.config();



if (!process.env.OWNER_PIN_HASH || !process.env.JWT_SECRET) {
    throw new Error("Auth env vars missing. Refusing to start. Ensure OWNER_PIN_HASH and JWT_SECRET are set.");
}

const app = express();
const PORT = process.env.PORT;

// Trust Proxy (Required for Render)
app.set('trust proxy', 1);



// Ping Endpoint
app.get('/__ping', (req, res) => {

    res.json({ ok: true, env: process.env.NODE_ENV });
});

// Middleware
const allowedOrigins = [
    process.env.CLIENT_URL,
    "http://localhost:5173",
    "chrome-extension://cfgdpghfcdfpbehhjipkgbacdhchmipc"
];

// Middleware
app.use(cors({
    origin: function (origin, callback) {
        if (!origin) return callback(null, true);
        if (allowedOrigins.includes(origin)) {
            return callback(null, origin);
        }
        return callback(new Error("CORS blocked"));
    },
    credentials: true
}));

app.use(express.json());

app.use(cookieParser());

// Database Connection
mongoose.connect(process.env.MONGODB_URI)
    .then(() => console.log('MongoDB Connected'))
    .catch(err => console.log(err));

// Public Routes
app.get('/ping', (req, res) => {
    res.status(200).send('pong');
});

// Mount Auth at /api/auth to match production expectation
app.use('/api/auth', authRoutes);

// Protected Routes Middleware
// All /api requests require authentication
app.use('/api', requireOwnerAuth);

// API Routes (Protected)
app.use('/api', collectionRoutes); // Note: check if collectionRoutes expects /api prefix or is mounted relative. Original was app.use('/api', collectionRoutes).
app.use('/api/search', searchRoutes);
app.use('/api/daily-targets', dailyTargetRoutes);
app.use('/api/target-settings', targetSettingsRoutes);
app.use('/api/sync', syncRoutes);


app.get('/', (req, res) => {
    res.send('Nexus Tracker API');
});

app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});
