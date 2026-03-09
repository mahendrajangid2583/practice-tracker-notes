import React from 'react';
import { PieChart, Pie, Cell, ResponsiveContainer } from 'recharts';
import { Folder, Code, BookOpen, Briefcase, ArrowRight, Trash2 } from 'lucide-react';
import { motion } from 'framer-motion';
import clsx from 'clsx';
import { deleteCollection } from '../services/api';

const CollectionCard = ({ collection, refreshData }) => {
  const { _id, title, type, theme, totalTasks, completedTasks } = collection;

  const pending = totalTasks - completedTasks;
  const progress = totalTasks > 0 ? (completedTasks / totalTasks) * 100 : 0;

  const data = [
    { name: 'Completed', value: completedTasks },
    { name: 'Pending', value: pending > 0 ? pending : 0.01 },
  ];

  // Luxury Gold & Onyx Palette
  const COLORS = {
    gold: '#fbbf24', // Amber-400
    onyx: '#171717', // Neutral-900
    glass: 'rgba(23, 23, 23, 0.6)',
  };

  const handleDelete = async (e) => {
    e.preventDefault(); // Prevent navigation
    e.stopPropagation(); // Prevent bubbling to parent
    if (window.confirm('Are you sure you want to delete this collection?')) {
      try {
        await deleteCollection(_id);
        refreshData(); // Refresh the dashboard
      } catch (error) {
        console.error('Error deleting collection:', error);
      }
    }
  };

  const getIcon = () => {
    switch (type) {
      case 'DSA': return <Code size={20} />;
      case 'PROJECT': return <Briefcase size={20} />;
      case 'LEARNING': return <BookOpen size={20} />;
      default: return <Folder size={20} />;
    }
  };

  return (
    <motion.div
      whileHover={{ y: -4, scale: 1.02 }}
      className="h-full bg-neutral-900/50 backdrop-blur-xl rounded-2xl p-6 border border-white/10 hover:border-amber-400/50 transition-all duration-500 shadow-2xl shadow-black/50 group relative overflow-hidden"
    >
      {/* Delete Button (Top Right) */}
      <button
        onClick={handleDelete}
        className="absolute top-4 right-4 p-2 text-neutral-500 hover:text-red-500 opacity-0 group-hover:opacity-100 transition-all duration-300 z-20"
        title="Delete Collection"
      >
        <Trash2 size={18} />
      </button>

      {/* Subtle Gold Gradient Glow */}
      <div className="absolute inset-0 bg-gradient-to-br from-amber-500/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-700 pointer-events-none" />

      <div className="relative z-10 flex flex-col h-full justify-between">
        <div>
          {/* Header */}
          <div className="flex items-start justify-between mb-6">
            <div className="p-3 rounded-xl border border-white/5 bg-white/5 text-amber-400 shadow-inner">
              {getIcon()}
            </div>
            <div className="px-3 py-1 rounded-full text-xs font-medium border border-white/10 bg-white/5 text-neutral-400 tracking-widest uppercase">
              {type}
            </div>
          </div>

          {/* Title */}
          <h3 className="text-2xl font-serif font-medium text-white mb-2 group-hover:text-amber-400 transition-colors duration-300">
            {title}
          </h3>
          <p className="text-neutral-500 text-sm mb-8 font-sans">
            {totalTasks === 0 ? 'No tasks yet' : `${completedTasks} / ${totalTasks} Milestones`}
          </p>
        </div>

        {/* Visualization */}
        <div>
          {type === 'DSA' ? (
            <div className="flex items-end justify-between">
              <div className="flex flex-col">
                <span className="text-4xl font-serif text-white tracking-tight">
                  {Math.round(progress)}%
                </span>
                <span className="text-xs text-neutral-500 font-medium uppercase tracking-widest mt-1">Completion</span>
              </div>
              <div className="h-16 w-16 relative">
                <PieChart width={64} height={64}>
                  <Pie
                    data={data}
                    cx="50%"
                    cy="50%"
                    innerRadius={15}
                    outerRadius={25}
                    paddingAngle={5}
                    dataKey="value"
                    stroke="none"
                  >
                    <Cell fill={COLORS.gold} />
                    <Cell fill="#262626" />
                  </Pie>
                </PieChart>
              </div>
            </div>
          ) : (
            <div className="space-y-4">
              <div className="flex justify-between text-sm">
                <span className="text-neutral-500 font-sans">Progress</span>
                <span className="text-white font-serif">{Math.round(progress)}%</span>
              </div>
              <div className="h-1 w-full bg-neutral-800 rounded-full overflow-hidden">
                <motion.div 
                  initial={{ width: 0 }}
                  animate={{ width: `${progress}%` }}
                  className="h-full bg-gradient-to-r from-amber-200 to-amber-500 shadow-[0_0_10px_rgba(251,191,36,0.5)]"
                  transition={{ duration: 1, ease: "easeOut" }}
                />
              </div>
            </div>
          )}
        </div>
      </div>
    </motion.div>
  );
};

export default CollectionCard;
