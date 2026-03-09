import React, { useState } from 'react';
import { X } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { createCollection } from '../services/api';

const AddCollectionModal = ({ isOpen, onClose, onCollectionAdded }) => {
  const [title, setTitle] = useState('');
  const [type, setType] = useState('DSA');
  const [theme, setTheme] = useState('blue');

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await createCollection({ title, type, theme });
      onCollectionAdded();
      onClose();
      setTitle('');
      setType('DSA');
      setTheme('blue');
    } catch (error) {
      console.error('Error creating collection:', error);
    }
  };

  const THEMES = [
    { id: 'blue', bg: 'bg-blue-500', label: 'Blue' },
    { id: 'purple', bg: 'bg-purple-500', label: 'Purple' },
    { id: 'emerald', bg: 'bg-emerald-500', label: 'Green' },
    { id: 'rose', bg: 'bg-rose-500', label: 'Red' },
    { id: 'amber', bg: 'bg-amber-500', label: 'Gold' },
    { id: 'cyan', bg: 'bg-cyan-500', label: 'Cyan' },
    { id: 'slate', bg: 'bg-slate-500', label: 'Grey' },
  ];
  const types = ['DSA', 'PROJECT', 'LEARNING', 'NOTES'];

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-md p-4">
          <motion.div
            initial={{ opacity: 0, scale: 0.95, y: 20 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.95, y: 20 }}
            className="bg-neutral-900 rounded-2xl p-8 w-full max-w-md border border-white/10 shadow-2xl shadow-black/50 relative overflow-hidden"
          >
            {/* Ambient Glow */}
            <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-amber-200 via-amber-500 to-amber-700" />
            
            <div className="flex justify-between items-center mb-8">
              <div>
                <h2 className="text-2xl font-serif text-white tracking-wide">New Collection</h2>
                <p className="text-neutral-500 text-sm mt-1 font-light">Define your next milestone.</p>
              </div>
              <button 
                onClick={onClose} 
                className="text-neutral-500 hover:text-white transition-colors p-2 hover:bg-white/5 rounded-full"
              >
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleSubmit} className="space-y-6">
              <div>
                <label className="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-2">Title</label>
                <input
                  type="text"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  required
                  className="w-full bg-neutral-950 border border-neutral-800 rounded-xl px-4 py-3 text-white placeholder-neutral-700 focus:outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/20 transition-all font-serif"
                  placeholder="e.g., LeetCode Top 75"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-2">Type</label>
                <div className="grid grid-cols-4 gap-2">
                  {types.map(t => (
                    <button
                      key={t}
                      type="button"
                      onClick={() => setType(t)}
                      className={`px-2 py-2 rounded-lg text-[10px] font-medium transition-all border ${
                        type === t 
                          ? 'bg-white text-black border-white' 
                          : 'bg-neutral-950 text-neutral-500 border-neutral-800 hover:border-neutral-700'
                      }`}
                    >
                      {t}
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-3">Theme Accent</label>
                <div className="flex gap-3 justify-center flex-wrap bg-white/5 p-4 rounded-xl border border-white/10">
                  {THEMES.map((t) => (
                    <button
                      key={t.id}
                      type="button"
                      onClick={() => setTheme(t.id)}
                      title={t.label}
                      className={`w-8 h-8 rounded-full transition-all duration-300 relative border-2 ${t.bg} ${
                        theme === t.id 
                          ? 'scale-110 border-white ring-2 ring-white/20 shadow-[0_0_15px_rgba(255,255,255,0.3)]' 
                          : 'border-transparent opacity-80 hover:opacity-100 hover:scale-105 hover:border-white/50'
                      }`}
                    />
                  ))}
                </div>
              </div>

              <button
                type="submit"
                className="w-full bg-white hover:bg-neutral-200 text-black font-bold py-4 rounded-xl transition-all shadow-lg hover:shadow-xl transform hover:-translate-y-0.5 mt-2"
              >
                Create Collection
              </button>
            </form>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
};

export default AddCollectionModal;
