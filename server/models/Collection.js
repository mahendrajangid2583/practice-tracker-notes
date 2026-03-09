import mongoose from 'mongoose';

const collectionSchema = new mongoose.Schema({
    title: {
        type: String,
        required: true,
        unique: true,
    },
    type: {
        type: String,
        enum: ['DSA', 'PROJECT', 'LEARNING', 'NOTES'],
        required: true,
    },
    theme: {
        type: String,
        default: 'blue', // blue, purple, green, etc.
    },
    totalTasks: {
        type: Number,
        default: 0,
    },
    completedTasks: {
        type: Number,
        default: 0,
    },
    lastOpenedAt: {
        type: Date,
    },
}, { timestamps: true });

export default mongoose.model('Collection', collectionSchema);
