import React from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X } from 'lucide-react';
import DOMPurify from 'dompurify';

const ReadNoteModal = ({ isOpen, onClose, note, title }) => {
  if (!isOpen) return null;

  return (
    <AnimatePresence>
      {isOpen && (
        <>
          {/* Backdrop */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
            className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50"
          />

          {/* Modal */}
          <motion.div
            initial={{ opacity: 0, scale: 0.95, y: 20 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.95, y: 20 }}
            className="fixed inset-0 z-50 flex items-center justify-center p-4 pointer-events-none"
          >
            <div className="bg-neutral-950 border border-neutral-800 w-full h-full max-w-[95vw] max-h-[95vh] rounded-2xl shadow-2xl flex flex-col pointer-events-auto overflow-hidden">
              
              {/* Header */}
              <div className="flex items-center justify-between p-6 border-b border-neutral-800 bg-neutral-950">
                <h2 className="text-xl font-serif text-white truncate pr-4">
                  {title || 'Note Details'}
                </h2>
                <button
                  onClick={onClose}
                  className="p-2 text-neutral-500 hover:text-white hover:bg-neutral-800 rounded-lg transition-colors"
                >
                  <X size={20} />
                </button>
              </div>

              {/* Content */}
              <div className="flex-1 overflow-y-auto p-6 sm:p-12 custom-scrollbar">
                <div 
                  className="prose-invert-custom max-w-none text-lg leading-relaxed"
                  dangerouslySetInnerHTML={{ __html: DOMPurify.sanitize(note) }} 
                />
              </div>
            </div>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  );
};

export default ReadNoteModal;
