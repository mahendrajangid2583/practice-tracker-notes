import mongoose from 'mongoose';

const dailyTargetSchema = new mongoose.Schema({
    date: {
        type: String, // Format: YYYY-MM-DD
        required: true,
        unique: true
    },
    tasks: [{
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Task'
    }]
}, { timestamps: true });

export default mongoose.model('DailyTarget', dailyTargetSchema);
