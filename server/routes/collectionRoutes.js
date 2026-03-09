import express from 'express';
import Collection from '../models/Collection.js';
import Task from '../models/Task.js';
import DeletedItem from '../models/DeletedItem.js';
import axios from 'axios';
import * as cheerio from 'cheerio';

const router = express.Router();

// GET /collections/activity: Fetch completion timestamps
router.get('/collections/activity', async (req, res) => {
    try {
        const tasks = await Task.find({
            status: 'Done',
            completedAt: { $gte: new Date('2026-02-01T00:00:00.000Z') }
        }).select('completedAt');

        const timestamps = tasks.map(t => t.completedAt);
        res.json(timestamps);
    } catch (err) {
        res.status(500).json({ message: err.message });
    }
});

// GET /collections: Fetch all collections with dynamic counts
router.get('/collections', async (req, res) => {
    try {
        const collections = await Collection.aggregate([
            {
                $addFields: {
                    sortDate: { $ifNull: ["$lastOpenedAt", "$createdAt"] }
                }
            },
            { $sort: { sortDate: -1 } },
            {
                $lookup: {
                    from: 'tasks',
                    localField: '_id',
                    foreignField: 'collectionId',
                    as: 'tasks'
                }
            },
            {
                $addFields: {
                    completedTasks: {
                        $size: {
                            $filter: {
                                input: '$tasks',
                                as: 'task',
                                cond: { $eq: ['$$task.status', 'Done'] }
                            }
                        }
                    },
                    totalTasks: { $size: '$tasks' },
                    solvedProblems: { // Alias for frontend compatibility if needed
                        $size: {
                            $filter: {
                                input: '$tasks',
                                as: 'task',
                                cond: { $eq: ['$$task.status', 'Done'] }
                            }
                        }
                    }
                }
            },
            { $project: { tasks: 0 } }
        ]);

        const totalCompletedTasks = await Task.countDocuments({ status: 'Done' });

        res.json({
            collections,
            globalStats: {
                totalCompletedTasks
            }
        });
    } catch (err) {
        console.error("CRITICAL BACKEND ERROR:", err);
        res.status(500).json({ message: err.message });
    }
});

// POST /collections: Create new
router.post('/collections', async (req, res) => {
    const { title, type, theme } = req.body;
    const collection = new Collection({
        title,
        type,
        theme,
    });

    try {
        const newCollection = await collection.save();
        res.status(201).json(newCollection);
    } catch (err) {
        res.status(400).json({ message: err.message });
    }
});

// DELETE /collections/:id: Delete a collection AND delete all Tasks associated with it (Cascade delete)
router.delete('/collections/:id', async (req, res) => {
    try {
        const collection = await Collection.findById(req.params.id);
        if (!collection) return res.status(404).json({ message: 'Collection not found' });

        await collection.deleteOne();
        await DeletedItem.create({ originalId: req.params.id, entityType: 'collection' });

        // We also need to log the tasks as deleted, or just rely on cascade delete?
        // Ideally, if a collection is deleted, all its tasks are deleted locally on Android too.
        // Android's SyncWorker should handle this: if collection is gone, remove tasks.
        // But for safety, let's log the deleted tasks too so other clients know.
        const tasks = await Task.find({ collectionId: req.params.id });
        const taskIds = tasks.map(t => t._id);

        await Task.deleteMany({ collectionId: req.params.id });
        if (taskIds.length > 0) {
            const deletedTasks = taskIds.map(id => ({ originalId: id, entityType: 'task' }));
            await DeletedItem.insertMany(deletedTasks);
        }

        res.json({ message: 'Collection and associated tasks deleted' });
    } catch (err) {
        res.status(500).json({ message: err.message });
    }
});

// GET /collections/list: Lightweight list for extensions/dropdowns
router.get('/collections/list', async (req, res) => {
    try {
        const collections = await Collection.find({}, 'title type').sort({ title: 1 });
        res.json(collections);
    } catch (err) {
        res.status(500).json({ message: err.message });
    }
});

// GET /collections/:id: Get a single collection with its Tasks inside
router.get('/collections/:id', async (req, res) => {
    try {
        const collection = await Collection.findById(req.params.id);
        if (!collection) return res.status(404).json({ message: 'Collection not found' });

        const tasks = await Task.find({ collectionId: req.params.id }).sort({ createdAt: -1 });

        // Recalculate counts to ensure consistency
        const totalTasks = await Task.countDocuments({ collectionId: req.params.id });
        const completedTasks = await Task.countDocuments({ collectionId: req.params.id, status: 'Done' });

        // Update lastOpenedAt AND counts
        collection.lastOpenedAt = new Date();
        collection.totalTasks = totalTasks;
        collection.completedTasks = completedTasks;
        await collection.save();

        res.json({ ...collection.toObject(), tasks });
    } catch (err) {
        res.status(500).json({ message: err.message });
    }
});

// PATCH /collections/:id: Update collection details
router.patch('/collections/:id', async (req, res) => {
    try {
        const collection = await Collection.findById(req.params.id);
        if (!collection) return res.status(404).json({ message: 'Collection not found' });

        if (req.body.title) collection.title = req.body.title;
        if (req.body.type) collection.type = req.body.type;
        if (req.body.theme) collection.theme = req.body.theme;

        const updatedCollection = await collection.save();
        res.json(updatedCollection);
    } catch (err) {
        res.status(400).json({ message: err.message });
    }
});

// POST /tasks/smart-add: Smartly add a task based on URL
router.post('/tasks/smart-add', async (req, res) => {
    try {
        const { url, title, difficulty, rating, platform: providedPlatform } = req.body;

        if (!url) return res.status(400).json({ message: 'URL is required' });

        // 1. Identify Platform
        let platform = providedPlatform;
        if (!platform) {
            if (url.includes('leetcode.com')) platform = 'LeetCode';
            else if (url.includes('codeforces.com')) platform = 'Codeforces';
            else if (url.includes('cses.fi')) platform = 'CSES';
            else platform = 'Collector';
        }

        // 2. Find or Create Collection
        let collection;
        if (req.body.collectionId) {
            collection = await Collection.findById(req.body.collectionId);
            if (!collection) return res.status(404).json({ message: 'Collection not found' });
        } else {
            collection = await Collection.findOne({ title: platform });
            if (!collection) {
                let theme = 'gray';
                if (platform === 'LeetCode') theme = 'orange';
                else if (platform === 'Codeforces') theme = 'rose';
                else if (platform === 'CSES') theme = 'blue';

                collection = new Collection({
                    title: platform,
                    type: 'DSA',
                    theme,
                });
                await collection.save();
            }
        }

        // 3. Scrape Title if missing
        let finalTitle = title;
        if (!finalTitle) {
            try {
                const { data } = await axios.get(url, {
                    headers: { 'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36' }
                });
                const $ = cheerio.load(data);
                finalTitle = $('title').text().trim() || url;
            } catch (error) {
                console.error("Error scraping title:", error.message);
                finalTitle = url;
            }
        }

        // 4. Create Task
        const task = new Task({
            collectionId: collection._id,
            title: finalTitle,
            link: url,
            difficulty: difficulty || 'Medium',
            rating: rating ? Number(rating) : undefined,
            platform,
            addedAt: new Date(),
        });

        const newTask = await task.save();

        // Increment totalTasks
        await Collection.findByIdAndUpdate(collection._id, { $inc: { totalTasks: 1 } });

        res.status(201).json({
            message: "Task added successfully",
            task: newTask,
            collectionName: collection.title
        });

    } catch (err) {
        console.error("Smart Add Error:", err);
        res.status(500).json({ message: err.message });
    }
});

// POST /tasks: Add a task. When added, increment the `totalTasks` counter in the parent Collection document.
router.post('/tasks', async (req, res) => {
    const { collectionId, title, link, difficulty, rating } = req.body;
    const task = new Task({
        collectionId,
        title,
        link,
        difficulty,
        rating: rating ? Number(rating) : undefined,
    });

    try {
        const newTask = await task.save();

        // Increment totalTasks
        await Collection.findByIdAndUpdate(collectionId, { $inc: { totalTasks: 1 } });

        res.status(201).json(newTask);
    } catch (err) {
        res.status(400).json({ message: err.message });
    }
});

// PATCH /tasks/:id: Update status and return dynamic counts
router.patch('/tasks/:id', async (req, res) => {
    try {
        const task = await Task.findById(req.params.id);
        if (!task) return res.status(404).json({ message: 'Task not found' });

        if (req.body.status) {
            task.status = req.body.status;
            task.completedAt = req.body.status === 'Done' ? new Date() : null;
        }
        if (req.body.title) task.title = req.body.title;
        if (req.body.link) task.link = req.body.link;
        if (req.body.difficulty) task.difficulty = req.body.difficulty;
        if (req.body.visualization !== undefined) task.visualization = req.body.visualization;

        const updatedTask = await task.save();

        // Calculate dynamic counts
        const collectionCompletedTasks = await Task.countDocuments({
            collectionId: task.collectionId,
            status: 'Done'
        });

        const globalCompletedTasks = await Task.countDocuments({ status: 'Done' });

        // Update parent collection cache (optional but good for other endpoints)
        await Collection.findByIdAndUpdate(task.collectionId, {
            completedTasks: collectionCompletedTasks
        });

        res.json({
            task: updatedTask,
            collectionCompletedTasks,
            globalCompletedTasks
        });
    } catch (err) {
        res.status(400).json({ message: err.message });
    }
});

// PATCH /tasks/:id/notes: Update task notes
router.patch('/tasks/:id/notes', async (req, res) => {
    try {
        const task = await Task.findById(req.params.id);
        if (!task) return res.status(404).json({ message: 'Task not found' });

        task.notes = req.body.notes;
        const updatedTask = await task.save();

        res.json(updatedTask);
    } catch (err) {
        res.status(400).json({ message: err.message });
    }
});

// DELETE /tasks/:id: Delete a task and update parent collection counters
router.delete('/tasks/:id', async (req, res) => {
    try {
        const task = await Task.findById(req.params.id);
        if (!task) return res.status(404).json({ message: 'Task not found' });

        const collectionId = task.collectionId;
        const isCompleted = task.status === 'Done';

        await task.deleteOne();
        await DeletedItem.create({ originalId: req.params.id, entityType: 'task' });

        // Update parent collection
        const update = { $inc: { totalTasks: -1 } };
        if (isCompleted) {
            update.$inc.completedTasks = -1;
        }

        await Collection.findByIdAndUpdate(collectionId, update);

        res.json({ message: 'Task deleted successfully' });
    } catch (err) {
        res.status(500).json({ message: err.message });
    }
});

export default router;
