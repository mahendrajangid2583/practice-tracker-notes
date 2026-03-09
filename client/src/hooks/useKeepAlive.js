import { useEffect } from 'react';

const useKeepAlive = () => {
    useEffect(() => {
        const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:5001';

        const pingBackend = async () => {
            try {
                // Use fetch for a lightweight request without interceptors
                // keepalive: true tells the browser to perform the request in the background
                await fetch(`${API_BASE}/ping`, {
                    method: 'GET',
                    keepalive: true,
                    // Prevent caching to ensure the request actually hits the server
                    cache: 'no-store'
                });
            } catch (error) {
                // Silently fail - we don't want to disturb the user or spam the console
                // Connection errors are expected if the user goes offline
            }
        };

        // Initial ping
        pingBackend();

        // Set up interval (every 5 minutes - 300,000ms)
        // Render free tier sleeps after 15 mins of inactivity
        // 5 mins is a safe buffer that isn't too aggressive
        const intervalId = setInterval(pingBackend, 300000);

        return () => clearInterval(intervalId);
    }, []);
};

export default useKeepAlive;
