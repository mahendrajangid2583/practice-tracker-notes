import React, { useEffect } from 'react';
import { ArrowLeft, Flame } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useStreak } from '../hooks/useStreak';
import StreakCalendar from '../components/StreakCalendar';
import { motion } from 'framer-motion';

const ConsistencyDetails = () => {
    const { activityDates, currentStreak, loading, refetch } = useStreak();

    useEffect(() => {
        refetch();
    }, []);

    if (loading) {
        return (
            <div className="min-h-screen bg-neutral-950 flex items-center justify-center">
                <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-amber-500"></div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-neutral-950 text-neutral-100 font-sans selection:bg-amber-500/30">
            <div className="max-w-3xl mx-auto px-4 sm:px-6 py-12">
                {/* Header */}
                <div className="mb-12">
                    <Link 
                        to="/" 
                        className="inline-flex items-center gap-2 text-neutral-500 hover:text-white transition-colors mb-8 group"
                    >
                        <ArrowLeft size={16} className="group-hover:-translate-x-1 transition-transform" />
                        <span>Back to Dashboard</span>
                    </Link>
                    
                    <div className="flex items-start justify-between">
                        <div>
                            <h1 className="text-4xl font-serif text-white mb-2">Consistency Log</h1>
                            <p className="text-neutral-400">Tracking daily progress since February 2026.</p>
                        </div>
                        <div className="text-right">
                             <div className="flex items-center justify-end gap-2 mb-1">
                                <Flame className="text-amber-500" size={24} fill="currentColor" fillOpacity={0.2} />
                                <span className="text-4xl font-bold text-amber-500">{currentStreak}</span>
                             </div>
                             <span className="text-xs uppercase tracking-widest text-neutral-500 font-bold">Current Streak</span>
                        </div>
                    </div>
                </div>

                {/* Main Content */}
                <motion.div 
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.5 }}
                >
                   <StreakCalendar activityDates={activityDates} />
                </motion.div>
            </div>
        </div>
    );
};

export default ConsistencyDetails;
