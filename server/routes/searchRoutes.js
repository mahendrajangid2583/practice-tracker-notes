import express from 'express';
import Collection from '../models/Collection.js';
import Task from '../models/Task.js';

const router = express.Router();

// GET /api/search/omni?q=query
router.get('/omni', async (req, res) => {
    try {
        const { q } = req.query;
        let collections = [];
        let tasks = [];

        if (!q) {
            // Default: Show recent items (last 5 accessed/modified)
            collections = await Collection.find().sort({ updatedAt: -1 }).limit(5).select('_id title type');
            tasks = await Task.find().sort({ updatedAt: -1 }).limit(5).select('_id title collectionId status difficulty');
        } else {
            const regex = new RegExp(q, 'i'); // Case-insensitive regex

            // 1. Search Collections
            collections = await Collection.find({ title: regex })
                .select('_id title type')
                .limit(5);

            // 2. Search Tasks (Notes)
            tasks = await Task.find({ title: regex })
                .select('_id title collectionId status difficulty')
                .limit(5);
        }

        const collectionResults = collections.map(c => ({
            id: c._id,
            value: c.title,
            type: 'COLLECTION', // Frontend expects 'COLLECTION'
            subtype: c.type, // e.g. DSA, PROJECT
            link: `/collection/${c._id}`
        }));

        const taskResults = tasks.map(t => ({
            id: t._id,
            value: t.title,
            type: 'NOTE', // Default to NOTE
            subtype: t.difficulty ? 'QUESTION' : 'NOTE', // access difficulty to guess if it's a question
            link: `/collection/${t.collectionId}?focus=${t._id}`
        }));

        // Combine and return
        const results = [...collectionResults, ...taskResults];
        res.json(results);

    } catch (error) {
        console.error('Search Error:', error);
        res.status(500).json({ error: 'Search failed' });
    }
});

// GET /api/search/global?q=query
router.get('/global', async (req, res) => {
    try {
        const { q } = req.query;
        if (!q) return res.json([]);

        const regex = new RegExp(q, 'i');

        // 1. Search Collections
        const collections = await Collection.find({ title: regex })
            .select('_id title type')
            .limit(5);

        const collectionResults = collections.map(c => ({
            _id: c._id,
            title: c.title,
            type: 'COLLECTION',
            icon: c.type === 'DSA' ? 'Code' : 'Folder'
        }));

        // 2. Search Tasks (Title OR Notes)
        const tasks = await Task.find({
            $or: [
                { title: regex },
                { notes: regex }
            ]
        })
            .select('_id title notes collectionId')
            .limit(10);

        const taskResults = tasks.map(t => {
            let snippet = '';
            if (t.notes) {
                // simple snippet generation
                const matchIndex = t.notes.toLowerCase().indexOf(q.toLowerCase());
                if (matchIndex !== -1) {
                    const start = Math.max(0, matchIndex - 30);
                    const end = Math.min(t.notes.length, matchIndex + 70);
                    snippet = (start > 0 ? '...' : '') + t.notes.substring(start, end) + (end < t.notes.length ? '...' : '');
                } else if (t.notes.length > 0) {
                    snippet = t.notes.substring(0, 60) + '...';
                }
            }

            return {
                _id: t._id,
                title: t.title,
                type: 'TASK',
                collectionId: t.collectionId,
                snippet: snippet
            };
        });

        res.json([...collectionResults, ...taskResults]);

    } catch (error) {
        console.error('Global Search Error:', error);
        res.status(500).json({ error: 'Global search failed' });
    }
});

export default router;
