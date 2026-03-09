import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Plus, Trash2, Save, GripVertical } from 'lucide-react';
import { useData } from '../context/DataContext';
import { getTargetSettings, updateTargetSettings } from '../services/api';

const DailyTargetSettingsModal = ({ isOpen, onClose, onSettingsSaved }) => {
    const { collections } = useData();
    const [slots, setSlots] = useState([]);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);

    useEffect(() => {
        if (isOpen) {
            fetchSettings();
        }
    }, [isOpen]);

    const fetchSettings = async () => {
        setLoading(true);
        try {
            const settings = await getTargetSettings();
            setSlots(settings.slots || []);
        } catch (error) {
            console.error("Failed to load settings", error);
        } finally {
            setLoading(false);
        }
    };

    const handleSave = async () => {
        setSaving(true);
        try {
            // Filter out empty slots
            const validSlots = slots.filter(s => s.collectionIds.length > 0);
            await updateTargetSettings({ slots: validSlots });
            onSettingsSaved();
            onClose();
        } catch (error) {
            console.error("Failed to save settings", error);
        } finally {
            setSaving(false);
        }
    };

    const addSlot = () => {
        console.log("Adding slot, collections available:", collections.length);
        if (collections.length === 0) return;
        setSlots([...slots, { 
            // Default to first collection
            collectionIds: [collections[0]._id], 
            label: `Slot ${slots.length + 1}` 
        }]);
    };

    const removeSlot = (index) => {
        setSlots(slots.filter((_, i) => i !== index));
    };

    const toggleCollectionInSlot = (slotIndex, collectionId) => {
        setSlots(slots.map((slot, i) => {
            if (i !== slotIndex) return slot;

            const exists = slot.collectionIds.includes(collectionId);
            let newIds;
            if (exists) {
                // Prevent removing the last one? Or allow empty (invalid)?
                // Let's allow removing, but handle validation on save.
                newIds = slot.collectionIds.filter(id => id !== collectionId);
            } else {
                newIds = [...slot.collectionIds, collectionId];
            }
            
            return { ...slot, collectionIds: newIds };
        }));
    };
    
    // Check if a collection is selected in a specific slot
    const isSelected = (slot, collectionId) => slot.collectionIds.includes(collectionId);

    if (!isOpen) return null;

    return (
        <AnimatePresence>
            <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm">
                <motion.div
                    initial={{ opacity: 0, scale: 0.95 }}
                    animate={{ opacity: 1, scale: 1 }}
                    exit={{ opacity: 0, scale: 0.95 }}
                    className="bg-neutral-900 border border-neutral-800 rounded-2xl w-full max-w-2xl max-h-[85vh] overflow-hidden flex flex-col shadow-2xl"
                >
                    {/* Header */}
                    <div className="p-6 border-b border-neutral-800 flex justify-between items-center">
                        <h2 className="text-xl font-serif text-white">Target Configuration</h2>
                        <button onClick={onClose} className="p-2 text-neutral-500 hover:text-white rounded-lg hover:bg-neutral-800 transition-colors">
                            <X size={20} />
                        </button>
                    </div>

                    {/* Body */}
                    <div className="flex-1 overflow-y-auto p-6 space-y-6">
                        {loading ? (
                             <div className="flex justify-center p-8"><div className="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-amber-500"></div></div>
                        ) : (
                            <>
                                <p className="text-sm text-neutral-400">
                                    Define your Daily Target slots. Each slot generates one task per day.
                                    <br />
                                    <span className="text-amber-500 font-medium">Single Collection</span> &rarr; Linear Order (First Unsolved).
                                    <br />
                                    <span className="text-amber-500 font-medium">Multiple Collections</span> &rarr; Random Collection &rarr; Last Unsolved.
                                </p>

                                <div className="space-y-4">
                                    {slots.map((slot, index) => (
                                        <div key={index} className="p-4 bg-neutral-950 rounded-xl border border-neutral-800">
                                            <div className="flex justify-between items-center mb-4">
                                                <h3 className="font-medium text-white flex items-center gap-2">
                                                    <span className="bg-neutral-800 text-neutral-400 w-6 h-6 flex items-center justify-center rounded-full text-xs font-mono">{index + 1}</span>
                                                    Slot Configuration
                                                </h3>
                                                <button onClick={() => removeSlot(index)} className="text-neutral-600 hover:text-rose-500 transition-colors">
                                                    <Trash2 size={16} />
                                                </button>
                                            </div>

                                            {/* Collection Grid */}
                                            <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
                                                {collections.map(col => (
                                                    <button
                                                        key={col._id}
                                                        onClick={() => toggleCollectionInSlot(index, col._id)}
                                                        className={`px-3 py-2 rounded-lg text-xs font-medium text-left truncate transition-all ${
                                                            isSelected(slot, col._id)
                                                                ? 'bg-amber-500/20 text-amber-400 border border-amber-500/50'
                                                                : 'bg-neutral-900 text-neutral-500 border border-neutral-800 hover:border-neutral-700'
                                                        }`}
                                                    >
                                                        {col.title}
                                                    </button>
                                                ))}
                                            </div>
                                            
                                            {/* Logic Indicator */}
                                            <div className="mt-3 text-[10px] uppercase font-bold tracking-wider text-neutral-600">
                                                Mode: {slot.collectionIds.length <= 1 ? "Linear Progression" : "Random Discovery"}
                                            </div>
                                        </div>
                                    ))}
                                </div>

                                <button
                                    onClick={addSlot}
                                    className="w-full py-3 border border-dashed border-neutral-700 rounded-xl text-neutral-500 hover:text-white hover:border-neutral-500 hover:bg-neutral-800/50 transition-all flex items-center justify-center gap-2"
                                >
                                    <Plus size={16} />
                                    Add Target Slot
                                </button>
                            </>
                        )}
                    </div>

                    {/* Footer */}
                    <div className="p-6 border-t border-neutral-800 flex justify-end">
                        <button
                            onClick={handleSave}
                            disabled={saving}
                            className="bg-white text-black px-6 py-2 rounded-lg font-medium hover:bg-neutral-200 transition-colors flex items-center gap-2 disabled:opacity-50"
                        >
                            {saving ? 'Saving...' : (
                                <>
                                    <Save size={16} />
                                    Save Configuration
                                </>
                            )}
                        </button>
                    </div>
                </motion.div>
            </div>
        </AnimatePresence>
    );
};

export default DailyTargetSettingsModal;
