import { useState, useEffect, useMemo } from 'react';
import { getActivityLog } from '../services/api';

export const useStreak = () => {
    const [activityDates, setActivityDates] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchStreak = async () => {
        setLoading(true);
        try {
            const dates = await getActivityLog();
            setActivityDates(dates);
            setError(null);
        } catch (err) {
            console.error("Failed to fetch streak:", err);
            setError(err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchStreak();
    }, []);

    const streakData = useMemo(() => {
        if (!activityDates.length) return { currentStreak: 0, isActiveToday: false };

        const today = new Date();
        const yesterday = new Date(today);
        yesterday.setDate(yesterday.getDate() - 1);

        const formatDate = (date) => {
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            return `${year}-${month}-${day}`;
        };

        const todayStr = formatDate(today);
        const yesterdayStr = formatDate(yesterday);

        // Convert raw timestamps to local YYYY-MM-DD dates/set
        const localDateSet = new Set(
            activityDates.map(ts => formatDate(new Date(ts)))
        );

        const isActiveToday = localDateSet.has(todayStr);
        let currentStreak = 0;

        // Start checking from today if active, otherwise start from yesterday
        let checkDate = isActiveToday ? today : yesterday;

        // If not active today and not active yesterday, streak is 0.
        // But if active yesterday, we start counting from yesterday.
        if (!isActiveToday && !localDateSet.has(yesterdayStr)) {
            return { currentStreak: 0, isActiveToday };
        }

        while (true) {
            const dateStr = formatDate(checkDate);
            if (localDateSet.has(dateStr)) {
                currentStreak++;
                checkDate.setDate(checkDate.getDate() - 1);
            } else {
                break;
            }
        }

        return { currentStreak, isActiveToday };

    }, [activityDates]);

    return { ...streakData, activityDates, loading, refetch: fetchStreak }; // activityDates here contains raw timestamps now
};
