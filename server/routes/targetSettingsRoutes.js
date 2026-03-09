import express from 'express';
import { getTargetSettings, updateTargetSettings } from '../controllers/dailyTargetController.js';

const router = express.Router();

router.get('/', getTargetSettings);
router.put('/', updateTargetSettings);

export default router;
