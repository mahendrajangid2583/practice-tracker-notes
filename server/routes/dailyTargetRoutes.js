import express from 'express';
import { getDailyTargets } from '../controllers/dailyTargetController.js';

const router = express.Router();

// GET /api/daily-targets?date=YYYY-MM-DD
router.get('/', getDailyTargets);

export default router;
