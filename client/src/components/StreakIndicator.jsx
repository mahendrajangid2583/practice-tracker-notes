import React from 'react';
import { Flame } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useStreak } from '../hooks/useStreak';
import { motion } from 'framer-motion';

const StreakIndicator = () => {
    const { currentStreak, loading } = useStreak();

    if (loading) return null;

    return (
        <Link to="/consistency">
            <motion.div 
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
                className="flex items-center gap-2 group cursor-pointer"
            >
                <div className={`p-2 rounded-lg border transition-colors ${currentStreak > 0 ? 'bg-amber-500/10 border-amber-500/20' : 'bg-neutral-900 border-neutral-800'}`}>
                    <Flame 
                        size={20} 
                        className={`transition-colors ${currentStreak > 0 ? 'text-amber-500 fill-amber-500/20' : 'text-neutral-600'}`} 
                    />
                </div>
                <div className="flex flex-col">
                    <span className="text-[10px] uppercase tracking-widest text-neutral-500 font-bold">Consistency</span>
                    <span className={`text-sm font-medium ${currentStreak > 0 ? 'text-amber-400' : 'text-neutral-400'}`}>
                        {currentStreak} Day{currentStreak !== 1 ? 's' : ''}
                    </span>
                </div>
            </motion.div>
        </Link>
    );
};

export default StreakIndicator;
