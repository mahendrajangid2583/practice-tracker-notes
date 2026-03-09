import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, Command, Folder, FileText, ArrowRight, Code } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import clsx from 'clsx';
import DOMPurify from 'dompurify';
import { searchGlobal } from '../services/api';

const SearchPalette = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const [loading, setLoading] = useState(false);
  const inputRef = useRef(null);
  const navigate = useNavigate();

  // Toggle with Ctrl+K
  useEffect(() => {
    const handleKeyDown = (e) => {
      if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
        e.preventDefault();
        setIsOpen((prev) => !prev);
      }
      if (e.key === 'Escape') {
        setIsOpen(false);
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  // Custom Event Listener for UI Triggers
  useEffect(() => {
    const handleOpenSearch = () => setIsOpen(true);
    window.addEventListener('open-search-palette', handleOpenSearch);
    return () => window.removeEventListener('open-search-palette', handleOpenSearch);
  }, []);

  // Auto-focus input when opened
  useEffect(() => {
    if (isOpen) {
      setTimeout(() => inputRef.current?.focus(), 100);
      setQuery('');
      setResults([]);
    }
  }, [isOpen]);

  // Search Logic (Debounced)
  useEffect(() => {
    const timer = setTimeout(async () => {
      if (!query.trim()) {
        setResults([]);
        return;
      }

      setLoading(true);
      try {
        const data = await searchGlobal(query);
        setResults(data);
        setSelectedIndex(0);
      } catch (error) {
        console.error('Search failed:', error);
      } finally {
        setLoading(false);
      }
    }, 300);

    return () => clearTimeout(timer);
  }, [query]);

  // Keyboard Navigation
  useEffect(() => {
    const handleNavigation = (e) => {
      if (!isOpen) return;

      if (e.key === 'ArrowDown') {
        e.preventDefault();
        setSelectedIndex((prev) => (prev + 1) % results.length);
      } else if (e.key === 'ArrowUp') {
        e.preventDefault();
        setSelectedIndex((prev) => (prev - 1 + results.length) % results.length);
      } else if (e.key === 'Enter') {
        e.preventDefault();
        if (results[selectedIndex]) {
          handleSelect(results[selectedIndex]);
        }
      }
    };

    window.addEventListener('keydown', handleNavigation);
    return () => window.removeEventListener('keydown', handleNavigation);
  }, [isOpen, results, selectedIndex]);

  const handleSelect = (item) => {
    setIsOpen(false);
    if (item.type === 'COLLECTION') {
      navigate(`/collection/${item._id}`);
    } else if (item.type === 'TASK') {
      navigate(`/collection/${item.collectionId}?focus=${item._id}`);
    }
  };

  // Highlight matching text
  const HighlightedText = ({ text, highlight }) => {
    if (!highlight.trim()) return <span>{text}</span>;
    const parts = text.split(new RegExp(`(${highlight})`, 'gi'));
    return (
      <span>
        {parts.map((part, i) => 
          part.toLowerCase() === highlight.toLowerCase() ? (
            <span key={i} className="text-amber-400 font-bold">{part}</span>
          ) : (
            <span key={i}>{part}</span>
          )
        )}
      </span>
    );
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-[100] flex items-start justify-center pt-[20vh] px-4">
          {/* Backdrop */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={() => setIsOpen(false)}
            className="absolute inset-0 bg-black/60 backdrop-blur-sm"
          />

          {/* Palette */}
          <motion.div
            initial={{ opacity: 0, scale: 0.95, y: -20 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.95, y: -20 }}
            className="relative w-[95vw] md:w-full max-w-2xl bg-neutral-900 border border-neutral-700 shadow-2xl rounded-xl overflow-hidden flex flex-col max-h-[60vh]"
          >
            {/* Input */}
            <div className="flex items-center px-4 py-4 border-b border-neutral-800">
              <Search className="text-neutral-500 mr-3" size={24} />
              <input
                ref={inputRef}
                type="text"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="Search collections, tasks, notes..."
                className="flex-1 bg-transparent text-xl text-white placeholder-neutral-500 focus:outline-none"
              />
              <div className="hidden sm:flex items-center gap-1 text-xs text-neutral-500 border border-neutral-800 rounded px-1.5 py-0.5">
                <span className="text-[10px]">ESC</span>
              </div>
            </div>

            {/* Results */}
            <div className="overflow-y-auto custom-scrollbar p-2">
              {loading ? (
                <div className="p-8 text-center text-neutral-500 flex flex-col items-center gap-2">
                    <div className="w-5 h-5 border-2 border-neutral-600 border-t-amber-500 rounded-full animate-spin" />
                    <span>Searching...</span>
                </div>
              ) : results.length > 0 ? (
                <div className="space-y-1">
                  {results.map((item, index) => (
                    <button
                      key={item._id}
                      onClick={() => handleSelect(item)}
                      onMouseEnter={() => setSelectedIndex(index)}
                      className={clsx(
                        "w-full text-left px-2 md:px-4 py-3 rounded-lg flex items-start gap-4 transition-colors group",
                        index === selectedIndex ? "bg-neutral-800" : "hover:bg-neutral-800/50"
                      )}
                    >
                      <div className={clsx(
                        "p-2 rounded-lg mt-0.5",
                        item.type === 'COLLECTION' ? "bg-blue-500/10 text-blue-400" : "bg-amber-500/10 text-amber-400"
                      )}>
                        {item.type === 'COLLECTION' ? (
                            item.icon === 'Code' ? <Code size={20} /> : <Folder size={20} />
                        ) : (
                            <FileText size={20} />
                        )}
                      </div>
                      
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between mb-0.5">
                            <h4 className={clsx(
                                "font-medium truncate pr-4",
                                index === selectedIndex ? "text-white" : "text-neutral-300"
                            )}>
                                <HighlightedText text={item.title} highlight={query} />
                            </h4>
                            {index === selectedIndex && (
                                <ArrowRight size={16} className="text-neutral-500" />
                            )}
                        </div>
                        
                        {item.type === 'TASK' && item.snippet && (
                            <p className="text-sm text-neutral-500 line-clamp-1 font-serif italic">
                                "...<HighlightedText text={item.snippet} highlight={query} />..."
                            </p>
                        )}
                        
                        {item.type === 'COLLECTION' && (
                            <span className="text-[10px] font-bold text-neutral-600 uppercase tracking-wider">Collection</span>
                        )}
                      </div>
                    </button>
                  ))}
                </div>
              ) : query.trim() ? (
                <div className="p-12 text-center text-neutral-500">
                  <p>No results found for "{query}"</p>
                </div>
              ) : (
                <div className="p-12 text-center text-neutral-600">
                    <div className="flex justify-center mb-4 opacity-20">
                        <Command size={48} />
                    </div>
                    <p className="text-sm">Type to search across your entire second brain.</p>
                </div>
              )}
            </div>

            {/* Footer */}
            <div className="px-4 py-2 bg-neutral-900 border-t border-neutral-800 flex justify-between items-center text-[10px] text-neutral-500">
                <div className="flex gap-3">
                    <span className="flex items-center gap-1"><kbd className="bg-neutral-800 px-1 rounded">↑↓</kbd> to navigate</span>
                    <span className="flex items-center gap-1"><kbd className="bg-neutral-800 px-1 rounded">↵</kbd> to select</span>
                </div>
                <span>Global Search</span>
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
};

export default SearchPalette;
