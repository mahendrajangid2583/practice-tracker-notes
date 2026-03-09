import React, { useState, useEffect } from 'react';
import { Target, Settings } from 'lucide-react';
import { getDailyTargets, updateTaskStatus, updateTaskNotes, updateTask, deleteTask } from '../services/api';
import TaskItem from './TaskItem';
import DailyTargetSettingsModal from './DailyTargetSettingsModal';
import NoteModal from './NoteModal';
import ReadNoteModal from './ReadNoteModal';
import SandpackModal from './SandpackModal';
import confetti from 'canvas-confetti';
import { useData } from '../context/DataContext';

const DailyTargets = () => {
    const [targets, setTargets] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isSettingsOpen, setIsSettingsOpen] = useState(false);
    const { refreshData } = useData();

    // Modal States
    const [isNoteModalOpen, setIsNoteModalOpen] = useState(false);
    const [currentNoteTask, setCurrentNoteTask] = useState(null);
    const [readModalTask, setReadModalTask] = useState(null);
    const [isSandpackModalOpen, setIsSandpackModalOpen] = useState(false);
    const [currentSandpackTask, setCurrentSandpackTask] = useState(null);
    const [sandpackViewMode, setSandpackViewMode] = useState('split');


    // Get today's date in YYYY-MM-DD format
    const getTodayDate = () => {
        const d = new Date();
        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    };

    const fetchTargets = async () => {
        setLoading(true);
        try {
            const dateStr = getTodayDate();
            const data = await getDailyTargets(dateStr);
            setTargets(data || []);
        } catch (error) {
            console.error("Failed to fetch daily targets", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchTargets();
        const interval = setInterval(() => {
           // Check for day change logic could go here
        }, 60000); 
        return () => clearInterval(interval);
    }, []);

    // Handlers
    const handleToggleStatus = async (taskId, currentStatus) => {
        const newStatus = currentStatus === 'Done' ? 'Pending' : 'Done';
        setTargets(prev => prev.map(t => t._id === taskId ? { ...t, status: newStatus } : t));
        if (newStatus === 'Done') {
            confetti({ particleCount: 60, spread: 70, origin: { y: 0.7 }, colors: ['#fbbf24', '#ffffff'] });
        }
        try {
            await updateTaskStatus(taskId, newStatus);
            refreshData(true); 
        } catch (error) {
            setTargets(prev => prev.map(t => t._id === taskId ? { ...t, status: currentStatus } : t));
        }
    };

    const handleUpdateTask = async (taskId, data) => {
        try {
            await updateTask(taskId, data);
            setTargets(prev => prev.map(t => t._id === taskId ? { ...t, ...data } : t));
        } catch (error) {
            console.error("Update failed", error);
        }
    };

    const handleDeleteTask = async (taskId) => {
        if (!window.confirm("Remove this target? (Note: This deletes the actual task from its collection)")) return;
        try {
            await deleteTask(taskId);
            setTargets(prev => prev.filter(t => t._id !== taskId));
            refreshData(true);
        } catch (error) {
            console.error("Delete failed", error);
        }
    };
    
    // Notes
    const handleOpenNotes = (task) => {
        setCurrentNoteTask(task);
        setIsNoteModalOpen(true);
    };

    const handleSaveNotes = async (taskId, notes) => {
        try {
            await updateTaskNotes(taskId, notes);
            setTargets(prev => prev.map(t => t._id === taskId ? { ...t, notes } : t));
        } catch (error) {
            console.error("Save notes failed", error);
        }
    };

    // Sandpack
    const handleOpenSandpack = (task, mode = 'split') => {
        setCurrentSandpackTask(task);
        setSandpackViewMode(mode);
        setIsSandpackModalOpen(true);
    };

    const handleSaveSandpack = async (taskId, code) => {
        try {
            await updateTask(taskId, { visualization: code });
            setTargets(prev => prev.map(t => t._id === taskId ? { ...t, visualization: code } : t));
        } catch (error) {
            console.error("Save details failed", error);
        }
    };


    return (
        <section className="mb-16 relative">
            {/* Header */}
            <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-3">
                    <div className="p-2 bg-amber-500/10 rounded-lg border border-amber-500/20">
                        <Target className="text-amber-400" size={20} />
                    </div>
                    <h2 className="text-2xl font-serif text-white tracking-wide">
                        Daily Targets
                    </h2>
                    <div className="px-2 py-0.5 rounded-full bg-neutral-800 border border-neutral-700 text-neutral-400 text-xs font-mono">
                        {getTodayDate()}
                    </div>
                </div>
                
                <button 
                    onClick={() => setIsSettingsOpen(true)}
                    className="p-2 text-neutral-500 hover:text-white rounded-lg hover:bg-neutral-800 transition-colors"
                    title="Configure Targets"
                >
                    <Settings size={18} />
                </button>
            </div>

            {/* Content */}
            {loading ? (
                 <div className="flex gap-4">
                    <div className="h-32 w-full bg-neutral-900/30 rounded-xl animate-pulse"></div>
                    <div className="h-32 w-full bg-neutral-900/30 rounded-xl animate-pulse hidden md:block"></div>
                 </div>
            ) : targets.length === 0 ? (
                <div className="text-center py-12 border border-dashed border-neutral-800 rounded-xl bg-neutral-900/20">
                    <p className="text-neutral-500 mb-4">No targets generated for today.</p>
                    <button 
                        onClick={() => setIsSettingsOpen(true)}
                        className="text-amber-500 hover:text-amber-400 text-sm font-medium"
                    >
                        Configure Target Slots
                    </button>
                </div>
            ) : (
                <div className="space-y-4">
                    {targets.map(task => (
                        <TaskItem
                            key={task._id} 
                            task={task} 
                            onStatusChange={handleToggleStatus}
                            onUpdate={handleUpdateTask}
                            onDelete={handleDeleteTask}
                            onOpenNotes={(t, readOnly) => {
                                if (readOnly) setReadModalTask(t);
                                else handleOpenNotes(t);
                            }}
                            onOpenSandpack={handleOpenSandpack}
                        />
                    ))}
                </div>
            )}

            {/* Modals */}
            <DailyTargetSettingsModal 
                isOpen={isSettingsOpen} 
                onClose={() => setIsSettingsOpen(false)}
                onSettingsSaved={fetchTargets}
            />

            {currentNoteTask && (
                <NoteModal
                    isOpen={isNoteModalOpen}
                    onClose={() => setIsNoteModalOpen(false)}
                    initialValue={currentNoteTask.notes}
                    onSave={(notes) => handleSaveNotes(currentNoteTask._id, notes)}
                />
            )}

            {currentSandpackTask && (
                <SandpackModal
                    key={currentSandpackTask._id}
                    isOpen={isSandpackModalOpen}
                    onClose={() => setIsSandpackModalOpen(false)}
                    initialCode={currentSandpackTask.visualization}
                    taskId={currentSandpackTask._id}
                    onSave={(code) => handleSaveSandpack(currentSandpackTask._id, code)}
                    initialViewMode={sandpackViewMode}
                />
            )}

            <ReadNoteModal
                isOpen={!!readModalTask}
                onClose={() => setReadModalTask(null)}
                note={readModalTask?.notes}
                title={readModalTask?.title}
            />
        </section>
    );
};

export default DailyTargets;
