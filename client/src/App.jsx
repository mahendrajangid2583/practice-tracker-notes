import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { DataProvider } from './context/DataContext';
import CollectionDetails from './pages/CollectionDetails';
import Dashboard from './pages/Dashboard';
import ConsistencyDetails from './pages/ConsistencyDetails';
import SearchPalette from './components/SearchPalette';
import PinScreen from './components/PinScreen';
import useKeepAlive from './hooks/useKeepAlive';
import { checkAuth } from './services/auth';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(null); // null = loading
  useKeepAlive();

  useEffect(() => {
    const verifyAuth = async () => {
      const isAuth = await checkAuth();
      setIsAuthenticated(isAuth);
    };
    verifyAuth();

    const handleUnauthorized = () => setIsAuthenticated(false);
    window.addEventListener('auth:unauthorized', handleUnauthorized);

    return () => window.removeEventListener('auth:unauthorized', handleUnauthorized);
  }, []);

  if (isAuthenticated === null) {
      return <div className="min-h-screen bg-slate-950 flex items-center justify-center text-slate-500">Loading...</div>;
  }

  if (!isAuthenticated) {
      return <PinScreen onAuthenticated={() => setIsAuthenticated(true)} />;
  }
  
  return (
    <DataProvider>
      <Router>
        <div className="min-h-screen bg-slate-900 text-slate-100 font-sans">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/collection/:id" element={<CollectionDetails />} />
            <Route path="/consistency" element={<ConsistencyDetails />} />
          </Routes>
          <SearchPalette />
        </div>
      </Router>
    </DataProvider>
  );
}

export default App;
