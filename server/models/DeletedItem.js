import mongoose from 'mongoose';

const deletedItemSchema = new mongoose.Schema({
    originalId: {
        type: String, // Store as String to match Android UUIDs if needed, or ObjectId
        required: true,
    },
    entityType: {
        type: String,
        required: true,
        enum: ['task', 'collection', 'daily_target'],
    },
    deletedAt: {
        type: Date,
        default: Date.now,
        index: true, // Crucial for sync queries
    }
});

export default mongoose.model('DeletedItem', deletedItemSchema);
