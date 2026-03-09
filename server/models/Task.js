import mongoose from 'mongoose';

const taskSchema = new mongoose.Schema({
    collectionId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Collection',
        required: true,
    },
    title: {
        type: String,
        required: true,
    },
    link: {
        type: String,
    },
    status: {
        type: String,
        enum: ['Pending', 'Done', 'Skipped'],
        default: 'Pending',
    },
    difficulty: {
        type: String,
        enum: ['Easy', 'Medium', 'Hard'],
        default: 'Medium',
    },
    completedAt: {
        type: Date,
    },
    rating: {
        type: Number,
        default: null,
    },
    platform: {
        type: String,
        default: 'Other',
    },
    addedAt: {
        type: Date,
        default: Date.now,
    },
    notes: {
        type: String,
        default: "",
    },
    visualization: {
        type: String,
        default: "",
    },
}, { timestamps: true });

export default mongoose.model('Task', taskSchema);
