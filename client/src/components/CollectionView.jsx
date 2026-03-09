import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ArrowLeft, Plus, CheckCircle, XCircle, Trash2, ExternalLink, Shuffle } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import confetti from 'canvas-confetti';
import api from '../services/api';
import AddProblemModal from './AddProblemModal';

const CollectionView = () => {
  const { id } = useParams();
  const [collection, setCollection] = useState(null);
  const [problems, setProblems] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);

  useEffect(() => {
    fetchData();
  }, [id]);

  const fetchData = async () => {
    try {
      // We need to fetch collection details separately or assume we have them. 
      // Since the API for /collections returns all with counts, we might need a specific endpoint or just filter.
      // For now, let's fetch all collections to find this one (inefficient but simple for now) 
      // or add a specific endpoint. The plan said GET /collections/:id/problems but didn't specify GET /collections/:id.
      // Let's assume we can get collection details from the list or add a route.
      // I'll add a route for fetching single collection details in backend later if needed, 
      // but for now I'll just fetch all and find.
      const colRes = await api.get('/collections');
      const currentCol = colRes.data.find(c => c._id === id);
      setCollection(currentCol);

      const probRes = await api.get(`/collections/${id}/problems`);
      setProblems(probRes.data);
    } catch (error) {
      console.error('Error fetching data:', error);
    }
  };

  const updateStatus = async (problemId, newStatus) => {
    try {
      await api.patch(`/problems/${problemId}`, { status: newStatus });
      setProblems(problems.map(p => 
        p._id === problemId ? { ...p, status: newStatus } : p
      ));

      if (newStatus === 'Solved') {
        confetti({
          particleCount: 100,
          spread: 70,
          origin: { y: 0.6 }
        });
      }
      // Refresh collection stats
      const colRes = await api.get('/collections');
      const currentCol = colRes.data.find(c => c._id === id);
      setCollection(currentCol);
    } catch (error) {
      console.error('Error updating status:', error);
    }
  };

  const deleteProblem = async (problemId) => {
    if (window.confirm('Delete this problem?')) {
      try {
        await api.delete(`/problems/${problemId}`);
        setProblems(problems.filter(p => p._id !== problemId));
        // Refresh collection stats
        const colRes = await api.get('/collections');
        const currentCol = colRes.data.find(c => c._id === id);
        setCollection(currentCol);
      } catch (error) {
        console.error('Error deleting problem:', error);
      }
    }
  };

  const pickRandom = () => {
    const pending = problems.filter(p => p.status === 'Pending');
    if (pending.length > 0) {
      const random = pending[Math.floor(Math.random() * pending.length)];
      window.open(random.link, '_blank');
    } else {
      alert('No pending problems!');
    }
  };

  if (!collection) return <div className="p-8 text-white">Loading...</div>;

  const solvedCount = problems.filter(p => p.status === 'Solved').length;
  const progress = problems.length > 0 ? (solvedCount / problems.length) * 100 : 0;

  return (
    <div className="p-8 min-h-screen bg-gray-900 text-white">
      <Link to="/" className="inline-flex items-center text-gray-400 hover:text-white mb-6 transition-colors">
        <ArrowLeft size={20} className="mr-2" />
        Back to Dashboard
      </Link>

      <div className="mb-8">
        <div className="flex justify-between items-start mb-4">
          <div>
            <h1 className="text-3xl font-bold mb-2">{collection.title}</h1>
            <p className="text-gray-400">{collection.description}</p>
          </div>
          <div className="flex gap-3">
             <button
              onClick={pickRandom}
              className="flex items-center gap-2 bg-gray-800 hover:bg-gray-700 text-white px-4 py-2 rounded-lg border border-gray-700 transition-colors"
            >
              <Shuffle size={18} />
              I'm Feeling Lucky
            </button>
            <button
              onClick={() => setIsModalOpen(true)}
              className="flex items-center gap-2 bg-violet-600 hover:bg-violet-700 text-white px-4 py-2 rounded-lg transition-colors"
            >
              <Plus size={20} />
              Add Problem
            </button>
          </div>
        </div>

        <div className="bg-gray-800 rounded-full h-4 overflow-hidden">
          <motion.div
            initial={{ width: 0 }}
            animate={{ width: `${progress}%` }}
            className={`h-full bg-${collection.colorTheme}-500 transition-all duration-500`}
          />
        </div>
        <p className="text-right text-sm text-gray-400 mt-2">
          {solvedCount} of {problems.length} Solved ({Math.round(progress)}%)
        </p>
      </div>

      <div className="space-y-4">
        <AnimatePresence>
          {problems.map((problem) => (
            <motion.div
              key={problem._id}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, x: -20 }}
              className={`bg-gray-800 rounded-xl p-4 border border-gray-700 flex items-center justify-between group ${
                problem.status === 'Solved' ? 'border-emerald-500/30 bg-emerald-500/5' : 
                problem.status === 'Skipped' ? 'opacity-50' : ''
              }`}
            >
              <div className="flex items-center gap-4 flex-1">
                <button
                  onClick={() => updateStatus(problem._id, problem.status === 'Solved' ? 'Pending' : 'Solved')}
                  className={`p-2 rounded-full transition-colors ${
                    problem.status === 'Solved' 
                      ? 'text-emerald-500 bg-emerald-500/20' 
                      : 'text-gray-500 hover:text-emerald-500 hover:bg-gray-700'
                  }`}
                >
                  <CheckCircle size={24} />
                </button>

                <div className="flex-1">
                  <div className="flex items-center gap-3 mb-1">
                    <a 
                      href={problem.link} 
                      target="_blank" 
                      rel="noopener noreferrer"
                      className="text-lg font-medium hover:text-violet-400 flex items-center gap-2"
                    >
                      {problem.title}
                      <ExternalLink size={14} className="opacity-0 group-hover:opacity-100 transition-opacity" />
                    </a>
                    <span className={`text-xs px-2 py-1 rounded-full font-medium ${
                      problem.difficulty === 'Easy' ? 'bg-emerald-500/20 text-emerald-400' :
                      problem.difficulty === 'Medium' ? 'bg-amber-500/20 text-amber-400' :
                      'bg-rose-500/20 text-rose-400'
                    }`}>
                      {problem.difficulty}
                    </span>
                  </div>
                  {problem.notes && <p className="text-sm text-gray-400">{problem.notes}</p>}
                </div>
              </div>

              <div className="flex items-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                <button
                  onClick={() => updateStatus(problem._id, problem.status === 'Skipped' ? 'Pending' : 'Skipped')}
                  className={`p-2 rounded-lg text-gray-400 hover:text-amber-400 hover:bg-gray-700 ${
                    problem.status === 'Skipped' ? 'text-amber-400' : ''
                  }`}
                  title="Skip"
                >
                  <XCircle size={20} />
                </button>
                <button
                  onClick={() => deleteProblem(problem._id)}
                  className="p-2 rounded-lg text-gray-400 hover:text-rose-500 hover:bg-gray-700"
                  title="Delete"
                >
                  <Trash2 size={20} />
                </button>
              </div>
            </motion.div>
          ))}
        </AnimatePresence>
        
        {problems.length === 0 && (
          <div className="text-center py-12 text-gray-500">
            No problems yet. Add one to get started!
          </div>
        )}
      </div>

      {isModalOpen && (
        <AddProblemModal
          isOpen={isModalOpen}
          onClose={() => setIsModalOpen(false)}
          collectionId={id}
          onProblemAdded={fetchData}
        />
      )}
    </div>
  );
};

export default CollectionView;
