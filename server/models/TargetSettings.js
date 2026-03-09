import mongoose from 'mongoose';

const targetSettingsSchema = new mongoose.Schema({
    userId: { type: String, default: 'default' }, // Single user for now
    slots: [{
        label: { type: String, default: 'Daily Target' },
        collectionIds: [{ type: mongoose.Schema.Types.ObjectId, ref: 'Collection' }]
    }]
}, { timestamps: true });

export default mongoose.model('TargetSettings', targetSettingsSchema);
