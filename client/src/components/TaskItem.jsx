import React, { useState } from 'react';
import { CheckCircle, Circle, ExternalLink, Clock, Edit2, Save, X, ChevronDown, ChevronUp, Maximize2, StickyNote, CodeXml, Play, Trash2 } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import clsx from 'clsx';
import DOMPurify from 'dompurify';

const difficultyColor = {
    Easy: 'border-l-emerald-500/50',
    Medium: 'border-l-amber-500/50',
    Hard: 'border-l-rose-500/50',
};

const TaskItem = ({ 
    task, 
    onStatusChange, 
    onUpdate, 
    onDelete, 
    onOpenNotes, 
    onOpenSandpack
}) => {
    const [isExpanded, setIsExpanded] = useState(false);
    const [isEditing, setIsEditing] = useState(false);
    const [editedTitle, setEditedTitle] = useState(task.title);
    const [editedLink, setEditedLink] = useState(task.link || '');
    const [editedDifficulty, setEditedDifficulty] = useState(task.difficulty || 'Medium');
    const [isFullscreenNote, setIsFullscreenNote] = useState(false); // Can trigger parent callback

    const toggleNote = () => setIsExpanded(!isExpanded);

    const handleSaveEdit = () => {
        if (editedTitle.trim()) {
            onUpdate(task._id, { title: editedTitle, link: editedLink, difficulty: editedDifficulty });
            setIsEditing(false);
        }
    };

    const handleCancelEdit = () => {
        setIsEditing(false);
        setEditedTitle(task.title);
        setEditedLink(task.link || '');
        setEditedDifficulty(task.difficulty || 'Medium');
    };

    return (
        <motion.div
            id={`task-${task._id}`}
            layout
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, height: 0, marginBottom: 0 }}
            className={clsx(
                "group flex flex-col sm:flex-row items-start sm:items-center justify-between p-5 bg-neutral-900/30 hover:bg-neutral-900/60 rounded-xl border border-white/5 transition-all duration-300",
                difficultyColor[task.difficulty],
                task.status === 'Done' && "opacity-50 grayscale"
            )}
        >
            <div className="flex flex-row items-center gap-3 sm:gap-5 flex-1 w-full">
                {/* Status Toggle */}
                <button
                    onClick={() => onStatusChange(task._id, task.status)}
                    className={clsx(
                        "transition-all duration-300 transform group-hover:scale-110 shrink-0",
                        task.status === 'Done' ? "text-amber-500" : "text-neutral-600 hover:text-amber-400"
                    )}
                >
                    {task.status === 'Done' ? <CheckCircle size={24} /> : <Circle size={24} />}
                </button>

                <div className="flex-1 w-full min-w-0">
                    <div className="flex items-center justify-between">
                        <div className="flex-1 min-w-0">
                            {isEditing ? (
                                <div className="flex flex-col gap-3 w-full sm:flex-row sm:items-center sm:gap-2">
                                    <input
                                        type="text"
                                        value={editedTitle}
                                        onChange={(e) => setEditedTitle(e.target.value)}
                                        className="bg-neutral-950 border border-amber-500/50 rounded px-2 py-1 text-white focus:outline-none flex-1 w-full sm:w-auto"
                                        autoFocus
                                    />
                                    <input
                                        type="text"
                                        value={editedLink}
                                        onChange={(e) => setEditedLink(e.target.value)}
                                        placeholder="Link"
                                        className="bg-neutral-950 border border-amber-500/50 rounded px-2 py-1 text-white focus:outline-none w-full sm:w-1/3 text-sm"
                                    />
                                    <select
                                        value={editedDifficulty}
                                        onChange={(e) => setEditedDifficulty(e.target.value)}
                                        className="bg-neutral-950 border border-amber-500/50 rounded px-2 py-1 text-white text-sm focus:outline-none w-full sm:w-auto"
                                    >
                                        <option value="Easy">Easy</option>
                                        <option value="Medium">Medium</option>
                                        <option value="Hard">Hard</option>
                                    </select>
                                    <div className="flex w-full items-center justify-between mt-2 sm:mt-0 sm:w-auto sm:justify-start sm:gap-1">
                                        <div className="flex items-center gap-4 sm:gap-1">
                                            <button onClick={handleSaveEdit} className="text-amber-500 hover:text-amber-400"><Save size={18} /></button>
                                            <button onClick={handleCancelEdit} className="text-neutral-500 hover:text-white"><X size={18} /></button>
                                        </div>
                                        {/* Mobile Delete */}
                                        <button onClick={() => onDelete(task._id)} className="text-neutral-500 hover:text-rose-500 sm:hidden">
                                            <Trash2 size={18} />
                                        </button>
                                    </div>
                                </div>
                            ) : (
                                <div className="flex items-start gap-2">
                                    <h3 className={clsx(
                                        "font-medium text-lg transition-all font-serif tracking-wide whitespace-normal break-words pr-2",
                                        task.status === 'Done' ? "text-neutral-500 line-through decoration-neutral-700" : "text-neutral-200"
                                    )}>
                                        {task.link ? (
                                            <a
                                                href={task.link}
                                                target="_blank"
                                                rel="noopener noreferrer"
                                                className="hover:text-amber-400 hover:underline cursor-pointer transition-colors"
                                            >
                                                {task.title}
                                            </a>
                                        ) : (
                                            task.title
                                        )}
                                    </h3>
                                    {task.notes && task.notes.trim().length > 0 && (
                                        <button
                                            onClick={toggleNote}
                                            className="text-neutral-600 hover:text-amber-500 transition-colors p-1 shrink-0"
                                            title={isExpanded ? "Collapse Notes" : "Read Notes"}
                                        >
                                            {isExpanded ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
                                        </button>
                                    )}
                                </div>
                            )}
                        </div>
                    </div>

                    {/* Metadata Row */}
                    {!isEditing && (
                        <div className="flex items-center gap-4 mt-1.5 flex-wrap">
                            <span className={clsx("text-[10px] font-bold uppercase tracking-wider",
                                task.difficulty === 'Easy' ? 'text-emerald-500' :
                                    task.difficulty === 'Medium' ? 'text-amber-500' : 'text-rose-500'
                            )}>
                                {task.difficulty}
                            </span>

                            {task.rating && (
                                <span className="ml-2 text-[10px] font-mono font-bold px-1.5 py-0.5 rounded bg-slate-800 text-slate-400 border border-slate-700">
                                    {task.rating}
                                </span>
                            )}

                            <div className="flex items-center gap-1 text-xs text-neutral-600 font-mono">
                                <Clock size={10} />
                                <span>
                                    {new Date(task.addedAt || task.createdAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}
                                </span>
                            </div>

                            {task.link && (
                                <a
                                    href={task.link}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    className="flex items-center gap-1 text-xs text-neutral-500 hover:text-white transition-colors"
                                >
                                    <ExternalLink size={10} />
                                    Link
                                </a>
                            )}
                        </div>
                    )}

                    {/* Actions Row - Now below text */}
                    <div className={clsx(
                        "flex items-center gap-2 mt-3 opacity-100 sm:opacity-0 group-hover:opacity-100 transition-all duration-200",
                        isEditing ? "hidden sm:flex" : "flex"
                    )}>
                        {!isEditing && (
                            <>
                                {/* Visualization Buttons */}
                                {task.visualization ? (
                                    <>
                                        <button
                                            onClick={() => onOpenSandpack(task, 'preview')}
                                            className="p-2 rounded-lg transition-colors text-emerald-500 hover:bg-emerald-500/10"
                                            title="Run Visualization"
                                        >
                                            <Play size={18} />
                                        </button>
                                        <button
                                            onClick={() => onOpenSandpack(task, 'editor')}
                                            className="p-2 rounded-lg transition-colors text-slate-600 hover:text-amber-500 hover:bg-amber-500/10"
                                            title="Edit Code"
                                        >
                                            <CodeXml size={18} />
                                        </button>
                                    </>
                                ) : (
                                    <button
                                        onClick={() => onOpenSandpack(task, 'editor')}
                                        className="p-2 rounded-lg transition-colors text-slate-600 hover:text-emerald-500 hover:bg-emerald-500/10"
                                        title="Create Playground"
                                    >
                                        <CodeXml size={18} />
                                    </button>
                                )}

                                {/* Note Action Button (StickyNote) */}
                                <button
                                    onClick={() => onOpenNotes(task, false)}
                                    className={clsx(
                                        "p-2 rounded-lg transition-colors",
                                        task.notes && task.notes.trim().length > 0 ? "text-amber-400 hover:bg-amber-500/10" : "text-slate-600 hover:text-amber-500 hover:bg-amber-500/10"
                                    )}
                                    title={task.notes && task.notes.trim().length > 0 ? "Edit Notes" : "Add Notes"}
                                >
                                    <StickyNote size={18} />
                                </button>

                                <button
                                    onClick={() => setIsEditing(true)}
                                    className="p-2 text-neutral-600 hover:text-blue-500 hover:bg-blue-500/10 rounded-lg"
                                    title="Edit Title/Link"
                                >
                                    <Edit2 size={18} className="opacity-50" />
                                </button>
                            </>
                        )}
                        <button
                            onClick={() => onDelete(task._id)}
                            className="p-2 text-neutral-600 hover:text-rose-500 hover:bg-rose-500/10 rounded-lg"
                            title="Delete Task"
                        >
                            <Trash2 size={18} />
                        </button>
                    </div>

                    {/* Expanded Read Mode - Inline */}
                    <AnimatePresence>
                        {isExpanded && task.notes && (
                            <motion.div
                                initial={{ height: 0, opacity: 0 }}
                                animate={{ height: 'auto', opacity: 1 }}
                                exit={{ height: 0, opacity: 0 }}
                                className="overflow-hidden"
                            >
                                <div className="
                                    relative
                                    w-[calc(100%+2.5rem)] -ml-12 mr-0 py-6 px-5
                                    border-y border-neutral-800 bg-neutral-900/40
                                    md:w-auto md:ml-0 md:mr-0 md:mt-3 md:mb-4 md:rounded-xl md:border md:bg-neutral-900/60
                                    shadow-inner group/note
                                    mt-4
                                    lg:max-w-3xl lg:mx-auto
                                ">
                                    {/* Used to be maximize button here, but logic needs to be lifted if we want global fullscreen modal */}
                                     {/* Re-implementing maximize button if callback provided */}
                                     {onOpenNotes && (
                                         <button 
                                            // 2nd arg true = read only / fullscreen mode request
                                            onClick={() => onOpenNotes(task, true)}
                                            className="absolute top-2 right-2 p-2 text-slate-600 hover:text-amber-400 transition-colors"
                                            title="Read Fullscreen"
                                        >
                                            <Maximize2 size={16} />
                                        </button>
                                     )}

                                    <div
                                        className="prose-invert-custom text-slate-300 leading-relaxed font-sans text-sm md:text-base pr-6 max-h-[500px] overflow-y-auto"
                                        dangerouslySetInnerHTML={{ __html: DOMPurify.sanitize(task.notes) }}
                                    />
                                </div>
                            </motion.div>
                        )}
                    </AnimatePresence>
                </div>
            </div>


        </motion.div>
    );
};

export default TaskItem;
