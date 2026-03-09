import React, { useState, useRef, useEffect } from 'react';
import { NodeViewWrapper } from '@tiptap/react';
import { Image as ImageIcon, AlertCircle } from 'lucide-react';
import clsx from 'clsx';

const ImageBlockView = (props) => {
  const { node, selected, updateAttributes } = props;
  const { src, alt } = node.attrs;
  
  const [inputValue, setInputValue] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(false);
  const inputRef = useRef(null);

  // Focus input on mount if no src
  useEffect(() => {
    if (!src && inputRef.current) {
        setTimeout(() => inputRef.current?.focus(), 50);
    }
  }, [src]);

  const handleInsert = () => {
    if (inputValue.trim()) {
        updateAttributes({ src: inputValue.trim() });
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
        e.preventDefault();
        handleInsert();
    }
  };

  const handleLoad = () => {
    setIsLoading(false);
    setError(false);
  };

  const handleError = () => {
    setIsLoading(false);
    setError(true);
  };

  // --- INPUT STATE ---
  if (!src) {
    return (
        <NodeViewWrapper className="my-4">
            <div className={clsx(
                "flex items-center gap-2 p-2 rounded-lg bg-neutral-900 border transition-all duration-200",
                selected ? "border-amber-500 ring-1 ring-amber-500/50" : "border-neutral-800"
            )}>
                <div className="flex-shrink-0 text-neutral-500 p-1">
                    <ImageIcon size={20} />
                </div>
                <input
                    ref={inputRef}
                    type="text"
                    value={inputValue}
                    onChange={(e) => setInputValue(e.target.value)}
                    onKeyDown={handleKeyDown}
                    placeholder="Paste image URL here..."
                    className="flex-1 bg-transparent border-none text-neutral-200 placeholder-neutral-500 focus:outline-none text-sm font-medium"
                />
                <button
                    onClick={handleInsert}
                    disabled={!inputValue.trim()}
                    className="flex items-center gap-1.5 px-3 py-1.5 bg-amber-600 hover:bg-amber-500 text-white text-xs font-bold rounded transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                    Add Image
                </button>
            </div>
            <div className="text-xs text-neutral-500 mt-1.5 ml-1">
                Press Enter to insert
            </div>
        </NodeViewWrapper>
    );
  }

  // --- DISPLAY STATE ---
  return (
    <NodeViewWrapper className="image-block-component my-4 group">
      <div
        className={clsx(
          "relative rounded-xl overflow-hidden transition-all duration-300",
          "bg-neutral-900/30 border border-neutral-800/50", // Subtle container
          selected ? "ring-2 ring-amber-500/50 shadow-lg shadow-amber-900/10" : "hover:border-neutral-700",
        )}
      >
        {/* Loading State */}
        {isLoading && !error && (
          <div className="absolute inset-0 flex items-center justify-center bg-neutral-900 z-10 min-h-[200px]">
            <div className="w-8 h-8 border-2 border-neutral-700 border-t-amber-500 rounded-full animate-spin" />
          </div>
        )}

        {/* Error State */}
        {error ? (
            <div className="flex flex-col items-center justify-center p-8 bg-neutral-900 text-neutral-500 min-h-[150px]">
                <AlertCircle size={32} className="mb-2 text-rose-500/50" />
                <p className="text-sm font-medium">Failed to load image</p>
                <p className="text-xs text-neutral-600 mt-1 max-w-[300px] truncate">{src}</p>
                <button 
                    onClick={() => updateAttributes({ src: null })} 
                    className="mt-3 text-xs text-amber-500 hover:underline"
                >
                    Edit URL
                </button>
            </div>
        ) : (
             <img
                src={src}
                alt={alt}
                onLoad={handleLoad}
                onError={handleError}
                className={clsx(
                    "w-full h-auto max-h-[600px] object-contain block mx-auto",
                    isLoading ? "opacity-0" : "opacity-100 transition-opacity duration-300"
                )}
            />
        )}
      </div>
    </NodeViewWrapper>
  );
};

export default ImageBlockView;
