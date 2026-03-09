import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate, useSearchParams } from 'react-router-dom';
import { ArrowLeft, CheckCircle, Circle, Plus, Trash2, ExternalLink, Clock, Edit2, Save, X, FileText, ChevronDown, ChevronUp, Maximize2, StickyNote, Search, CodeXml, Play } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import clsx from 'clsx';
import confetti from 'canvas-confetti';
import { getCollectionDetails, createTask, updateTaskStatus, deleteTask, updateCollection, updateTask, deleteCollection, updateTaskNotes } from '../services/api';
import NoteModal from '../components/NoteModal';
import TaskItem from '../components/TaskItem';
import ReadNoteModal from '../components/ReadNoteModal';
import SandpackModal from '../components/SandpackModal';
import DOMPurify from 'dompurify';

// Add hook to force links to open in new tab
DOMPurify.addHook('afterSanitizeAttributes', function (node) {
  if ('target' in node) {
    node.setAttribute('target', '_blank');
    node.setAttribute('rel', 'noopener noreferrer');
  }
});

const CollectionDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [collection, setCollection] = useState(null);
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  
  // New Task State
  const [newTaskTitle, setNewTaskTitle] = useState('');
  const [newTaskLink, setNewTaskLink] = useState('');
  const [newTaskDifficulty, setNewTaskDifficulty] = useState('Medium');

  // Editing State
  const [isEditingCollection, setIsEditingCollection] = useState(false);
  const [editedCollectionTitle, setEditedCollectionTitle] = useState('');
  
  const [editingTaskId, setEditingTaskId] = useState(null);
  const [editedTaskTitle, setEditedTaskTitle] = useState('');
  const [editedTaskLink, setEditedTaskLink] = useState('');

  // Note Modal State
  const [isNoteModalOpen, setIsNoteModalOpen] = useState(false);
  const [currentNoteTask, setCurrentNoteTask] = useState(null);
  
  // Sandpack Modal State
  const [isSandpackModalOpen, setIsSandpackModalOpen] = useState(false);
  const [currentSandpackTask, setCurrentSandpackTask] = useState(null);
  const [sandpackViewMode, setSandpackViewMode] = useState('split');

  // ...

  const handleOpenSandpack = (task, mode = 'split') => {
    setCurrentSandpackTask(task);
    setSandpackViewMode(mode);
    setIsSandpackModalOpen(true);
  };
  const [expandedTaskId, setExpandedTaskId] = useState(null);
  const [readModalTask, setReadModalTask] = useState(null);

  useEffect(() => {
    fetchData();
  }, [id]);

  // Auto-Focus Logic
  useEffect(() => {
    const focusId = searchParams.get('focus');
    if (focusId && tasks.length > 0) {
      setExpandedTaskId(focusId);
      // Wait for render
      setTimeout(() => {
        const element = document.getElementById(`task-${focusId}`);
        if (element) {
          element.scrollIntoView({ behavior: 'smooth', block: 'center' });
          element.classList.add('ring-2', 'ring-amber-500', 'ring-offset-2', 'ring-offset-black');
          setTimeout(() => element.classList.remove('ring-2', 'ring-amber-500', 'ring-offset-2', 'ring-offset-black'), 2000);
        }
      }, 500);
    }
  }, [searchParams, tasks]);

  const fetchData = async () => {
    try {
      const data = await getCollectionDetails(id);
      setCollection(data);
      setTasks(data.tasks || []);
      setEditedCollectionTitle(data.title);
    } catch (error) {
      console.error('Error fetching collection details:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateCollection = async () => {
    if (!editedCollectionTitle.trim()) return;
    try {
      await updateCollection(id, { title: editedCollectionTitle });
      setCollection(prev => ({ ...prev, title: editedCollectionTitle }));
      setIsEditingCollection(false);
    } catch (error) {
      console.error('Error updating collection:', error);
    }
  };

  const handleDeleteCollection = async () => {
    if (window.confirm('Are you sure you want to delete this entire collection? This action cannot be undone.')) {
      try {
        await deleteCollection(id);
        navigate('/');
      } catch (error) {
        console.error('Error deleting collection:', error);
      }
    }
  };

  const handleUpdateTask = async (taskId, updates) => {
    try {
      if (!updates) return;
      
      await updateTask(taskId, updates);
      setTasks(tasks.map(t => 
        t._id === taskId ? { ...t, ...updates } : t
      ));
      setEditingTaskId(null);
    } catch (error) {
      console.error('Error updating task:', error);
    }
  };

  const startEditingTask = (task) => {
    setEditingTaskId(task._id);
    setEditedTaskTitle(task.title);
    setEditedTaskLink(task.link || '');
  };

  const handleAddTask = async (e) => {
    e.preventDefault();
    if (!newTaskTitle.trim()) return;

    try {
      const newTask = await createTask({
        collectionId: id,
        title: newTaskTitle,
        link: newTaskLink,
        difficulty: newTaskDifficulty,
      });
      setTasks([newTask, ...tasks]);
      setNewTaskTitle('');
      setNewTaskLink('');
      setNewTaskDifficulty('Medium');
      
      setCollection(prev => ({
        ...prev,
        totalTasks: prev.totalTasks + 1
      }));
    } catch (error) {
      console.error('Error adding task:', error);
    }
  };

  const handleDeleteTask = async (taskId) => {
    if (!window.confirm('Delete this task?')) return;
    try {
      await deleteTask(taskId);
      const taskToDelete = tasks.find(t => t._id === taskId);
      setTasks(tasks.filter(t => t._id !== taskId));
      
      setCollection(prev => ({
        ...prev,
        totalTasks: prev.totalTasks - 1,
        completedTasks: taskToDelete.status === 'Done' ? prev.completedTasks - 1 : prev.completedTasks
      }));
    } catch (error) {
      console.error('Error deleting task:', error);
    }
  };

  const toggleTaskStatus = async (taskId, currentStatus) => {
    const newStatus = currentStatus === 'Done' ? 'Pending' : 'Done';
    
    setTasks(tasks.map(t => 
      t._id === taskId ? { ...t, status: newStatus } : t
    ));

    if (newStatus === 'Done') {
      confetti({
        particleCount: 80,
        spread: 100,
        origin: { y: 0.6 },
        colors: ['#fbbf24', '#ffffff', '#d4d4d4']
      });
      
      setCollection(prev => ({
        ...prev,
        completedTasks: prev.completedTasks + 1
      }));
    } else {
      setCollection(prev => ({
        ...prev,
        completedTasks: prev.completedTasks - 1
      }));
    }

    try {
      await updateTaskStatus(taskId, newStatus);
    } catch (error) {
      console.error('Error updating task:', error);
      fetchData();
    }
  };

  const handleOpenNotes = (task) => {
    setCurrentNoteTask(task);
    setIsNoteModalOpen(true);
  };

  const handleSaveNotes = async (taskId, notes) => {
    try {
      await updateTaskNotes(taskId, notes);
      setTasks(tasks.map(t => 
        t._id === taskId ? { ...t, notes } : t
      ));
    } catch (error) {
      console.error('Error saving notes:', error);
    }
  };



  const handleSaveSandpack = async (taskId, code) => {
    try {
      await updateTask(taskId, { visualization: code });
      setTasks(tasks.map(t => 
        t._id === taskId ? { ...t, visualization: code } : t
      ));
    } catch (error) {
      console.error('Error saving visualization:', error);
    }
  };

  const toggleNote = (taskId) => {
    setExpandedTaskId(prev => prev === taskId ? null : taskId);
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-neutral-950 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-amber-500"></div>
      </div>
    );
  }

  if (!collection) return <div className="min-h-screen bg-neutral-950 text-white p-8">Collection not found</div>;

  const progress = collection.totalTasks > 0 
    ? (collection.completedTasks / collection.totalTasks) * 100 
    : 0;

  const difficultyColor = {
    Easy: 'border-l-emerald-500/50',
    Medium: 'border-l-amber-500/50',
    Hard: 'border-l-rose-500/50',
  };

  return (
    <div className="min-h-screen bg-neutral-950 text-neutral-100 p-8 font-sans selection:bg-amber-500/30 overflow-x-hidden">
      <div className="max-w-4xl mx-auto">
        <Link to="/" className="group inline-flex items-center text-neutral-500 hover:text-white mb-12 transition-colors">
          <ArrowLeft size={20} className="mr-2 group-hover:-translate-x-1 transition-transform" />
          <span className="font-medium tracking-wide text-sm uppercase">Back to Dashboard</span>
        </Link>
        
        <button 
            onClick={() => window.dispatchEvent(new CustomEvent('open-search-palette'))}
            className="absolute top-8 right-8 p-2 text-neutral-500 hover:text-white bg-neutral-900/50 hover:bg-neutral-800 rounded-full transition-all border border-transparent hover:border-neutral-700"
            title="Search (Ctrl+K)"
        >
            <Search size={20} />
        </button>

        {/* Header */}
        <div className="mb-16">
          <div className="flex justify-between items-end mb-6">
            <div className="flex-1">
              <div className="flex items-center gap-3 mb-3">
                <span className="px-3 py-1 rounded-full text-[10px] font-bold bg-amber-500/10 text-amber-500 uppercase tracking-widest border border-amber-500/20">
                  {collection.type}
                </span>
              </div>
              
              {/* Editable Title */}
              {isEditingCollection ? (
                <div className="flex items-center gap-2 mb-2">
                  <input
                    type="text"
                    value={editedCollectionTitle}
                    onChange={(e) => setEditedCollectionTitle(e.target.value)}
                    className="bg-neutral-900 border border-amber-500/50 rounded-lg px-3 py-1 text-4xl font-serif text-white focus:outline-none w-full max-w-md"
                    autoFocus
                  />
                  <button onClick={handleUpdateCollection} className="p-2 bg-amber-500 text-black rounded-lg hover:bg-amber-400">
                    <Save size={20} />
                  </button>
                  <button onClick={() => setIsEditingCollection(false)} className="p-2 bg-neutral-800 text-white rounded-lg hover:bg-neutral-700">
                    <X size={20} />
                  </button>
                </div>
              ) : (
                <div className="flex items-center gap-3 mb-2 group">
                  <h1 className="text-5xl font-serif text-white truncate max-w-[200px] md:max-w-none">{collection.title}</h1>
                  <button 
                    onClick={() => setIsEditingCollection(true)}
                    className="opacity-0 group-hover:opacity-100 transition-opacity p-2 text-neutral-500 hover:text-amber-500"
                    title="Edit Title"
                  >
                    <Edit2 size={20} />
                  </button>
                  <button 
                    onClick={handleDeleteCollection}
                    className="opacity-0 group-hover:opacity-100 transition-opacity p-2 text-neutral-500 hover:text-red-500"
                    title="Delete Collection"
                  >
                    <Trash2 size={20} />
                  </button>
                </div>
              )}

              <p className="text-neutral-500 font-light">
                {collection.completedTasks} of {collection.totalTasks} Milestones Achieved
              </p>
            </div>
            <div className="text-right">
              <span className="text-6xl font-serif text-white tracking-tighter">{Math.round(progress)}%</span>
            </div>
          </div>

          {/* Luxury Progress Bar */}
          <div className="h-1 bg-neutral-900 rounded-full overflow-hidden">
            <motion.div
              initial={{ width: 0 }}
              animate={{ width: `${progress}%` }}
              className="h-full bg-gradient-to-r from-amber-200 via-amber-400 to-amber-600 shadow-[0_0_15px_rgba(251,191,36,0.5)]"
              transition={{ duration: 1.2, ease: "circOut" }}
            />
          </div>
        </div>

        {/* Add Task Form */}
        <form 
          onSubmit={handleAddTask} 
          className="mb-8 bg-neutral-900/50 p-4 sm:p-5 rounded-xl border border-neutral-800 shadow-lg"
        >
          <div className="flex flex-col md:flex-row gap-3 md:items-center">
            
            {/* 1. Title Input (Main) */}
            <div className="relative flex-1">
              <input
                type="text"
                value={newTaskTitle}
                onChange={(e) => setNewTaskTitle(e.target.value)}
                placeholder="What needs solving?"
                className="w-full bg-neutral-950 border border-neutral-700 text-slate-200 placeholder-slate-500 rounded-lg px-4 py-3 focus:outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/50 transition-all"
              />
            </div>

            {/* 2. Secondary Inputs Group (Link + Difficulty) */}
            <div className="flex flex-col sm:flex-row gap-3 md:w-auto">
              {/* Link */}
              <input
                type="text"
                value={newTaskLink}
                onChange={(e) => setNewTaskLink(e.target.value)}
                placeholder="Link (Optional)"
                className="w-full sm:w-48 bg-neutral-950 border border-neutral-700 text-slate-300 placeholder-slate-600 rounded-lg px-3 py-3 text-sm focus:outline-none focus:border-blue-500/50 transition-all"
              />

              {/* Difficulty Dropdown */}
              <div className="relative">
                <select
                  value={newTaskDifficulty}
                  onChange={(e) => setNewTaskDifficulty(e.target.value)}
                  className="w-full sm:w-32 appearance-none bg-neutral-950 border border-neutral-700 text-slate-300 rounded-lg px-4 py-3 text-sm focus:outline-none focus:border-purple-500/50 cursor-pointer"
                >
                  <option value="Easy">Easy</option>
                  <option value="Medium">Medium</option>
                  <option value="Hard">Hard</option>
                </select>
                {/* Custom Chevron for style */}
                <div className="absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none text-slate-500">
                  <svg width="10" height="6" viewBox="0 0 10 6" fill="currentColor"><path d="M1 1L5 5L9 1" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/></svg>
                </div>
              </div>
            </div>

            {/* 3. Submit Button */}
            <button
              type="submit"
              disabled={!newTaskTitle.trim()}
              className="w-full md:w-auto px-6 py-3 bg-white hover:bg-neutral-200 text-black font-bold rounded-lg shadow-lg active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed transition-all flex items-center justify-center gap-2"
            >
              <Plus size={20} />
              <span className="md:hidden">Add</span> {/* Text only on mobile */}
            </button>

          </div>
        </form>

        {/* Task List */}
        <div className="space-y-4">
          <AnimatePresence>
            {tasks.map((task) => (
              <TaskItem
                key={task._id}
                task={task}
                onStatusChange={toggleTaskStatus}
                onUpdate={handleUpdateTask}
                onDelete={handleDeleteTask}
                onOpenNotes={(t, readOnly) => {
                    if (readOnly) setReadModalTask(t);
                    else handleOpenNotes(t);
                }}
                onOpenSandpack={handleOpenSandpack}
              />
            ))}
          </AnimatePresence>
          
          {tasks.length === 0 && (
            <div className="text-center py-20 border border-dashed border-neutral-800 rounded-2xl">
              <p className="text-neutral-500 font-serif italic text-lg">Silence is golden. Add a task to begin.</p>
            </div>
          )}
        </div>
      </div>

      {/* Note Modal */}
      {currentNoteTask && (
        <NoteModal
            isOpen={isNoteModalOpen}
            onClose={() => setIsNoteModalOpen(false)}
            initialValue={currentNoteTask.notes}
            onSave={(notes) => handleSaveNotes(currentNoteTask._id, notes)}
        />
      )}

      {/* Sandpack Modal */}
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

      {/* Read Note Modal */}
      <ReadNoteModal
        isOpen={!!readModalTask}
        onClose={() => setReadModalTask(null)}
        note={readModalTask?.notes}
        title={readModalTask?.title}
      />
    </div>
  );
};

export default CollectionDetails;
