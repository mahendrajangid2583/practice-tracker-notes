import React, { useState, useEffect } from 'react';
import { Plus, LayoutGrid, Activity, Flame, Search, LogOut } from 'lucide-react';
import { useData } from '../context/DataContext';
import { logout } from '../services/auth';
import CollectionCard from '../components/CollectionCard';
import AddCollectionModal from '../components/AddCollectionModal';
import DailyTargets from '../components/DailyTargets';
import StreakIndicator from '../components/StreakIndicator';
import { motion } from 'framer-motion';
import { Link } from 'react-router-dom';

const Dashboard = () => {
  const { collections, globalStats, loading, error, refreshData } = useData();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [activeTab, setActiveTab] = useState('ALL');

  useEffect(() => {
    refreshData(true); // Silent refresh to update sort order
  }, []);

  const filteredCollections = collections.filter(c => 
    activeTab === 'ALL' ? true : c.type === activeTab
  );

  const handleLogout = async () => {
      try {
          await logout();
      } catch (err) {
          console.error("Logout failed", err);
      } finally {
          // Force UI update regardless of server response
          window.dispatchEvent(new Event('auth:unauthorized'));
      }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-neutral-950 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-amber-500"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-neutral-950 flex items-center justify-center text-red-400 font-serif">
        <div className="text-center">
          <p className="text-2xl mb-2">System Error</p>
          <p className="text-neutral-500 font-sans text-sm">{error}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-neutral-950 text-neutral-100 font-sans selection:bg-amber-500/30">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        {/* Header Section */}
        <header className="flex flex-col md:flex-row justify-between items-start md:items-end mb-20 gap-8 border-b border-white/5 pb-8">
          <div>
            <div className="flex items-center gap-3 mb-4">
              <div className="p-2 bg-amber-500/10 rounded-lg border border-amber-500/20">
                <LayoutGrid className="text-amber-400" size={20} />
              </div>
              <span className="text-xs font-bold tracking-[0.2em] text-amber-500 uppercase">The Private Suite</span>
            </div>
            <h1 className="text-5xl md:text-6xl font-serif font-medium text-white tracking-tight mb-4">
              Mahendra <span className="text-neutral-600 italic">Jangid</span>
            </h1>
            <div className="flex items-center gap-6">
                <p className="text-neutral-400 text-lg max-w-md font-light leading-relaxed">
                Design your life. Track your progress.
                </p>
                <div className="h-8 w-px bg-neutral-800 hidden sm:block"></div>
                <StreakIndicator />
                <button 
                    onClick={handleLogout}
                    className="ml-4 p-2 text-neutral-500 hover:text-red-400 hover:bg-red-400/10 rounded-lg transition-colors"
                    title="Log Out"
                >
                    <LogOut size={20} />
                </button>
            </div>
          </div>
          
          <div className="flex flex-col md:flex-row items-center gap-4 w-full md:w-auto">
            {/* Search Bar Trigger */}
            <button 
                onClick={() => window.dispatchEvent(new CustomEvent('open-search-palette'))}
                className="flex items-center gap-3 px-4 py-3 bg-neutral-900 border border-neutral-800 rounded-full text-neutral-400 hover:border-amber-500/30 hover:text-white transition-all w-full md:w-64 group shadow-lg"
            >
                <Search size={18} className="group-hover:text-amber-400 transition-colors" />
                <span className="text-sm font-medium">Search...</span>
                <div className="ml-auto flex items-center gap-1 opacity-50 group-hover:opacity-100 transition-opacity">
                    <kbd className="hidden sm:inline-block px-1.5 py-0.5 text-[10px] font-bold text-neutral-500 bg-neutral-800 border border-neutral-700 rounded-md">Ctrl K</kbd>
                </div>
            </button>

            {/* Add Collection (Primary) */}
            <motion.button 
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              onClick={() => setIsModalOpen(true)}
              className="group flex justify-center items-center gap-3 bg-white text-neutral-950 px-8 py-4 rounded-full font-medium transition-all shadow-[0_0_20px_rgba(255,255,255,0.1)] hover:shadow-[0_0_30px_rgba(255,255,255,0.2)] w-full md:w-auto"
            >
              <span>New Collection</span>
              <div className="bg-neutral-950 rounded-full p-1 group-hover:rotate-90 transition-transform duration-300">
                <Plus size={14} className="text-white" />
              </div>
            </motion.button>
          </div>
        </header>

        {/* Grid Section */}
        <main>
          <DailyTargets />
          
          {/* Tab Bar */}
          <div className="flex gap-3 mb-10 overflow-x-auto pb-2 scrollbar-hide">
            {['ALL', 'DSA', 'PROJECT', 'LEARNING', 'NOTES'].map((tab) => (
              <button
                key={tab}
                onClick={() => setActiveTab(tab)}
                className={`px-6 py-2.5 rounded-full text-xs font-bold tracking-widest transition-all duration-300 whitespace-nowrap border ${
                  activeTab === tab
                    ? 'bg-amber-500 text-black border-amber-500 shadow-[0_0_20px_rgba(245,158,11,0.3)]'
                    : 'bg-neutral-900/50 text-neutral-500 border-neutral-800 hover:border-neutral-600 hover:text-neutral-300'
                }`}
              >
                {tab === 'ALL' ? 'ALL COLLECTIONS' : tab}
              </button>
            ))}
          </div>

          {filteredCollections.length === 0 ? (
            <motion.div 
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              className="text-center py-32 border border-dashed border-neutral-800 rounded-3xl bg-neutral-900/20 backdrop-blur-sm"
            >
              <div className="inline-flex p-4 rounded-full bg-neutral-900 mb-6 border border-neutral-800">
                <Activity className="text-neutral-600" size={32} />
              </div>
              <h3 className="text-2xl font-serif text-white mb-2">A Clean Slate</h3>
              <p className="text-neutral-500 max-w-sm mx-auto mb-8 font-light">
                Begin your legacy by creating your first collection.
              </p>
              <button 
                onClick={() => setIsModalOpen(true)}
                className="text-amber-400 hover:text-amber-300 font-medium transition-colors tracking-wide text-sm uppercase border-b border-amber-400/30 pb-1 hover:border-amber-400"
              >
                Initialize Collection
              </button>
            </motion.div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
              {filteredCollections.map((collection, index) => (
                <motion.div
                  key={collection._id}
                  initial={{ opacity: 0, y: 30 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: index * 0.1, duration: 0.6, ease: [0.22, 1, 0.36, 1] }}
                >
                  <Link to={`/collection/${collection._id}`} className="block h-full">
                    <CollectionCard collection={collection} refreshData={refreshData} />
                  </Link>
                </motion.div>
              ))}
            </div>
          )}
        </main>

        <AddCollectionModal 
          isOpen={isModalOpen} 
          onClose={() => setIsModalOpen(false)} 
          onCollectionAdded={refreshData}
        />
      </div>
    </div>
  );
};

export default Dashboard;
