import React, { useState } from 'react';
import { Lock } from 'lucide-react';
import { login } from '../services/auth';

const PinScreen = ({ onAuthenticated }) => {
    const [pin, setPin] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            await login(pin);
            onAuthenticated();
        } catch (err) {
            setError('Incorrect PIN');
            setPin('');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-slate-950 flex items-center justify-center p-4">
            <div className="max-w-md w-full bg-slate-900/50 border border-slate-800 rounded-2xl p-8 backdrop-blur-xl shadow-2xl">
                <div className="flex flex-col items-center mb-8">
                    <div className="w-16 h-16 bg-emerald-500/10 rounded-full flex items-center justify-center mb-4 border border-emerald-500/20">
                        <Lock className="w-8 h-8 text-emerald-400" />
                    </div>
                    <h1 className="text-2xl font-semibold text-slate-100">Owner Access</h1>
                    <p className="text-slate-400 mt-2 text-sm text-center">
                        Please enter your secure PIN to continue
                    </p>
                </div>

                <form onSubmit={handleSubmit} className="space-y-6">
                    <div>
                        <input
                            type="password"
                            inputmode="numeric"
                            autocomplete="off"
                            value={pin}
                            onChange={(e) => setPin(e.target.value)}
                            className="w-full bg-slate-950/50 border border-slate-700 rounded-xl px-4 py-3 text-center text-2xl tracking-[0.5em] text-emerald-400 placeholder-slate-700 focus:outline-none focus:border-emerald-500/50 focus:ring-1 focus:ring-emerald-500/50 transition-all"
                            placeholder="••••••••••"
                            autoFocus
                            maxLength={20}
                        />
                    </div>

                    {error && (
                        <div className="text-red-400 text-sm text-center bg-red-500/10 py-2 rounded-lg border border-red-500/20 animate-pulse">
                            {error}
                        </div>
                    )}

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full bg-emerald-600 hover:bg-emerald-500 text-white font-medium py-3 rounded-xl transition-colors disabled:opacity-50 disabled:cursor-not-allowed shadow-lg shadow-emerald-500/20"
                    >
                        {loading ? 'Verifying...' : 'Unlock System'}
                    </button>
                </form>

                <div className="mt-8 text-center">
                    <p className="text-xs text-slate-600 uppercase tracking-widest font-medium">
                        Protected Environment
                    </p>
                </div>
            </div>
        </div>
    );
};

export default PinScreen;
