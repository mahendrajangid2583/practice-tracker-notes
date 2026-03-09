import React from 'react';
import { motion } from 'framer-motion';

const StreakCalendar = ({ activityDates }) => {
    // Hardcoded start date as per requirements
    const START_DATE = new Date('2026-02-01T00:00:00');
    const today = new Date();
    
    // Generate months from Feb 2026 to current date
    const months = [];
    let current = new Date(START_DATE);
    
    // Ensure we include the current month
    while (current <= today || (current.getMonth() === today.getMonth() && current.getFullYear() === today.getFullYear())) {
        months.push(new Date(current));
        current.setMonth(current.getMonth() + 1);
    }
    // Reverse to show newest first
    months.reverse();

    // Process raw timestamps into local date strings
    const dateSet = new Set(
        activityDates.map(ts => {
            const d = new Date(ts);
            const year = d.getFullYear();
            const month = String(d.getMonth() + 1).padStart(2, '0');
            const day = String(d.getDate()).padStart(2, '0');
            return `${year}-${month}-${day}`;
        })
    );

    const formatDate = (date) => {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    };

    const getDaysInMonth = (date) => {
        const year = date.getFullYear();
        const month = date.getMonth();
        const days = new Date(year, month + 1, 0).getDate();
        const result = [];
        for (let i = 1; i <= days; i++) {
            result.push(new Date(year, month, i));
        }
        return result;
    };

    return (
        <div className="space-y-12">
            {months.map((monthDate) => (
                <div key={monthDate.toISOString()} className="space-y-4">
                    <h3 className="text-xl font-serif text-neutral-400 border-b border-neutral-800 pb-2">
                        {monthDate.toLocaleDateString('en-US', { month: 'long', year: 'numeric' })}
                    </h3>
                    <div className="grid grid-cols-7 gap-2 sm:gap-3">
                        {['S', 'M', 'T', 'W', 'T', 'F', 'S'].map((d, i) => (
                            <div key={i} className="text-center text-xs text-neutral-600 font-bold mb-2">
                                {d}
                            </div>
                        ))}
                        
                        {/* Empty cells for start offset */}
                        {Array.from({ length: new Date(monthDate.getFullYear(), monthDate.getMonth(), 1).getDay() }).map((_, i) => (
                            <div key={`empty-${i}`} />
                        ))}

                        {getDaysInMonth(monthDate).map((date) => {
                            const dateStr = formatDate(date);
                            const isActive = dateSet.has(dateStr);
                            const isFuture = date > today;
                            const isBeforeStart = date < START_DATE;

                            if (isBeforeStart) {
                                 return <div key={dateStr} className="aspect-square rounded-md bg-transparent" />;
                            }

                            return (
                                <motion.div
                                    key={dateStr}
                                    initial={false}
                                    className={`aspect-square rounded-md flex items-center justify-center text-xs font-medium border transition-colors relative group
                                        ${isActive 
                                            ? 'bg-amber-500 border-amber-500 text-black shadow-[0_0_10px_rgba(245,158,11,0.3)]' 
                                            : isFuture 
                                                ? 'bg-neutral-900/10 border-transparent text-neutral-800' 
                                                : 'bg-neutral-900 border-neutral-800 text-neutral-500'
                                        }`}
                                >
                                    {date.getDate()}
                                    {isActive && (
                                        <div className="absolute inset-0 bg-white/20 rounded-md opacity-0 group-hover:opacity-100 transition-opacity" />
                                    )}
                                </motion.div>
                            );
                        })}
                    </div>
                </div>
            ))}
        </div>
    );
};

export default StreakCalendar;
