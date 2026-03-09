import React, { useState, useMemo } from "react";
import { SandpackProvider, SandpackLayout, SandpackCodeEditor, SandpackPreview, useSandpack } from "@codesandbox/sandpack-react";
import { X, Code, Columns, Play, Save, Box } from "lucide-react";

// --- Save Button ---
const SaveButton = ({ onSave }) => {
  const { sandpack } = useSandpack();
  return (
    <button 
      onClick={() => onSave(sandpack.files["/App.js"].code)}
      className="flex items-center gap-2 px-4 py-1.5 bg-gradient-to-r from-emerald-600 to-emerald-500 hover:from-emerald-500 hover:to-emerald-400 text-white font-bold rounded-lg shadow-lg text-sm transition-all active:scale-95"
    >
      <Save size={16} />
      <span>Save Visualization</span>
    </button>
  );
};

const SandpackModal = ({ isOpen, onClose, initialCode, taskId, onSave }) => {
  const [viewMode, setViewMode] = useState('split'); 

  // Default Code
  const defaultCode = `import React from "react";\nexport default function App() {\n  return (\n    <div className="h-full w-full flex items-center justify-center bg-slate-900 text-white">\n      <h1>Algorithm Playground</h1>\n    </div>\n  );\n}`;

  // 1. MEMOIZE FILES (Prevents Editor Reset bug)
  const files = useMemo(() => ({
    "/App.js": { code: initialCode || defaultCode, active: true },
    "/index.js": {
        code: `import React, { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./styles.css";

import App from "./App";

const root = createRoot(document.getElementById("root"));
root.render(
  <StrictMode>
    <App />
  </StrictMode>
);`,
        hidden: true
    },
    "/styles.css": {
        code: `@tailwind base;
@tailwind components;
@tailwind utilities;

body {
  background-color: #020617; /* slate-950 */
  color: white;
}`,
        hidden: true
    }
  }), [taskId, initialCode]); 

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/90 backdrop-blur-sm">
      <div className="h-[100dvh] md:h-[90vh] w-full max-w-7xl bg-slate-950 border-0 md:border border-slate-800 rounded-none md:rounded-xl overflow-hidden flex flex-col shadow-2xl">
         
         <SandpackProvider 
            key={taskId} 
            template="react" 
            theme="dark"
            files={files}
            options={{
              externalResources: ["https://cdn.tailwindcss.com"],
              // FORCE HEIGHT CSS
              classes: { 
                "sp-wrapper": "h-full flex flex-col", 
                "sp-layout": "h-full flex-1 min-h-0", // min-h-0 is crucial for flex children
                "sp-stack": "h-full",
                "sp-editor": "h-full", 
                "sp-preview": "h-full" 
              }
            }}
            customSetup={{ 
              dependencies: { "recharts": "latest", "framer-motion": "latest", "lucide-react": "latest", "lodash": "latest", "clsx": "latest", "tailwind-merge": "latest" } 
            }}
            style={{ height: '100%', display: 'flex', flexDirection: 'column' }} // Inline style to force provider height
         >
            {/* HEADER (Fixed Height 60px) */}
            <div className="h-[60px] flex justify-between items-center p-3 border-b border-slate-800 bg-slate-900 shrink-0">
               <div className="flex items-center gap-3">
                 <div className="p-2 bg-amber-500/10 rounded-lg"><Box className="text-amber-500" size={20} /></div>
                 <h3 className="text-slate-200 font-serif font-bold text-lg hidden sm:block">Algorithm Lab</h3>
               </div>
               
               {/* VIEW TOGGLE */}
               <div className="flex bg-slate-950 p-1 rounded-lg border border-slate-800">
                  <button onClick={() => setViewMode('editor')} className={`p-2 rounded-md transition-colors ${viewMode === 'editor' ? 'bg-slate-800 text-white' : 'text-slate-500 hover:text-white'}`} title="Editor Only"><Code size={18} /></button>
                  <button onClick={() => setViewMode('split')} className={`p-2 rounded-md transition-colors hidden sm:block ${viewMode === 'split' ? 'bg-slate-800 text-white' : 'text-slate-500 hover:text-white'}`} title="Split View"><Columns size={18} /></button>
                  <button onClick={() => setViewMode('preview')} className={`p-2 rounded-md transition-colors ${viewMode === 'preview' ? 'bg-emerald-900/30 text-emerald-400 border border-emerald-500/30' : 'text-slate-500 hover:text-white'}`} title="Run Fullscreen"><Play size={18} /></button>
               </div>

               <button onClick={onClose} className="p-2 hover:bg-slate-800 rounded-full text-slate-400 hover:text-white transition-colors">
                  <X size={20} />
               </button>
            </div>

            {/* MAIN CONTENT (Calculated Height: 100% - Header - Footer) */}
            <div className="flex-1 min-h-0 relative">
               <SandpackLayout className="h-full absolute inset-0">
                  
                  {/* EDITOR */}
                  <div 
                    className={`h-full transition-all duration-300 ${
                      viewMode === 'editor' ? 'w-full' : 
                      viewMode === 'split' ? 'w-1/2' : 'hidden'
                    }`}
                  >
                    <SandpackCodeEditor 
                      showLineNumbers 
                      showTabs={false} 
                      closableTabs={false} 
                      style={{ height: '100%' }}
                    />
                  </div>

                  {/* PREVIEW */}
                  <div 
                    className={`h-full transition-all duration-300 ${
                      viewMode === 'preview' ? 'w-full' : 
                      viewMode === 'split' ? 'w-1/2 border-l border-slate-800' : 'hidden'
                    }`}
                  >
                    <SandpackPreview 
                      showNavigator={false} 
                      showOpenInCodeSandbox={false}
                      style={{ height: '100%' }}
                    />
                  </div>

               </SandpackLayout>
            </div>
            
            {/* FOOTER (Fixed Height) */}
            <div className="p-3 bg-slate-900 flex justify-end items-center gap-4 shrink-0 border-t border-slate-800">
               <span className="text-xs text-slate-500 mr-auto flex items-center gap-2">
                  <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
                  Ready
               </span>
               <SaveButton onSave={onSave} />
            </div>
         </SandpackProvider>
      </div>
    </div>
  );
};

export default SandpackModal;
