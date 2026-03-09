import React, { createContext, useState, useEffect, useContext } from 'react';
import { getCollections } from '../services/api';

const DataContext = createContext();

export const DataProvider = ({ children }) => {
  const [collections, setCollections] = useState([]);
  const [globalStats, setGlobalStats] = useState({ totalCompletedTasks: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchData = async (silent = false) => {
    if (!silent) setLoading(true);
    try {
      const response = await getCollections();
      // Handle both old (array) and new (object) API response formats for safety
      if (Array.isArray(response)) {
          setCollections(response);
          // Calculate manually if backend doesn't provide it yet
          const total = response.reduce((acc, col) => acc + (col.completedTasks || 0), 0);
          setGlobalStats({ totalCompletedTasks: total });
      } else {
          setCollections(response.collections || []);
          setGlobalStats(response.globalStats || { totalCompletedTasks: 0 });
      }
      setError(null);
    } catch (err) {
      console.error('Error fetching data:', err);
      setError('Failed to load collections');
    } finally {
      if (!silent) setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const refreshData = (silent = false) => {
    fetchData(silent);
  };

  return (
    <DataContext.Provider value={{ collections, globalStats, loading, error, refreshData }}>
      {children}
    </DataContext.Provider>
  );
};

export const useData = () => {
  const context = useContext(DataContext);
  if (context === undefined) {
    throw new Error('useData must be used within a DataProvider');
  }
  return context;
};
