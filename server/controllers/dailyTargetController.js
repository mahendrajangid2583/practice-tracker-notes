import DailyTarget from '../models/DailyTarget.js';
import Task from '../models/Task.js';
import Collection from '../models/Collection.js';
import TargetSettings from '../models/TargetSettings.js';

export const getDailyTargets = async (req, res) => {
    try {
        const clientDate = req.query.date;
        const today = clientDate || new Date().toISOString().split('T')[0];

        // 1. Check if targets exist for today
        let dailyTarget = await DailyTarget.findOne({ date: today }).populate('tasks');

        // If exists, simply return
        if (dailyTarget) {
            return res.status(200).json(dailyTarget.tasks);
        }

        // 2. Fetch Settings
        let settings = await TargetSettings.findOne();
        if (!settings || settings.slots.length === 0) {
            // Default Fallback if no settings: 1 random target
            return res.status(200).json([]); // Or return empty if not configured
        }

        console.log(`Generating Daily Targets for ${today}...`);
        const selectedTaskIds = new Set();
        const selectedTasks = [];

        // 3. Generate Targets per Slot
        for (const slot of settings.slots) {
            if (!slot.collectionIds || slot.collectionIds.length === 0) continue;

            let candidateTask = null;

            // CASE 1: Single Collection -> First Unsolved (FIFO)
            if (slot.collectionIds.length === 1) {
                const collectionId = slot.collectionIds[0];

                // Find oldest task (createdAt asc) that is Pending
                candidateTask = await Task.findOne({
                    collectionId: collectionId,
                    status: { $ne: 'Done' },
                    _id: { $nin: Array.from(selectedTaskIds) }
                }).sort({ createdAt: 1 }); // Oldest first (Linear progression)
            }

            // CASE 2: Multiple Collections -> Random Collection -> Last Unsolved (LIFO)
            else {
                // Pick one random collection from the list
                const randomCollectionId = slot.collectionIds[Math.floor(Math.random() * slot.collectionIds.length)];

                // Find newest task (createdAt desc) that is Pending
                // Use the random collection
                candidateTask = await Task.findOne({
                    collectionId: randomCollectionId,
                    status: { $ne: 'Done' },
                    _id: { $nin: Array.from(selectedTaskIds) }
                }).sort({ createdAt: -1 }); // Newest first (Stack/LIFO)
            }

            if (candidateTask) {
                selectedTaskIds.add(candidateTask._id.toString());
                selectedTasks.push(candidateTask._id);
            }
        }

        // 4. Save to Database
        dailyTarget = await DailyTarget.create({
            date: today,
            tasks: selectedTasks
        });

        // 5. Return Populated
        const populated = await DailyTarget.findById(dailyTarget._id).populate('tasks');

        res.status(200).json(populated.tasks);

    } catch (error) {
        console.error("Error in getDailyTargets:", error);
        res.status(500).json({ message: "Server Error generating daily targets" });
    }
};

export const getTargetSettings = async (req, res) => {
    try {
        let settings = await TargetSettings.findOne();
        if (!settings) {
            settings = await TargetSettings.create({ slots: [] });
        }
        res.status(200).json(settings);
    } catch (error) {
        res.status(500).json({ message: "Error fetching settings" });
    }
};

export const updateTargetSettings = async (req, res) => {
    try {
        const { slots } = req.body;
        // Upsert
        let settings = await TargetSettings.findOne();
        if (!settings) {
            settings = new TargetSettings({ slots });
        } else {
            settings.slots = slots;
        }
        await settings.save();

        // REGENERATION RULE:
        // When settings change, invalidates today's targets so they regenerate on next fetch.
        // This ensures the dashboard reflects the new configuration immediately.
        const today = new Date().toISOString().split('T')[0];
        await DailyTarget.findOneAndDelete({ date: today });
        console.log(`Settings updated. Cleared daily targets for ${today} to force regeneration.`);

        res.status(200).json(settings);
    } catch (error) {
        console.error("Error updating settings:", error);
        res.status(500).json({ message: "Error updating settings" });
    }
};
