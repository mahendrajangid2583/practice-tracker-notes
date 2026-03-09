import axios from 'axios';

// Dynamic API Base URL (Dev vs Prod)
const API_BASE = import.meta.env.PROD
    ? import.meta.env.VITE_API_URL+"/api"
    : "http://localhost:5001/api";

const authApi = axios.create({
    baseURL: API_BASE + '/auth',
    withCredentials: true
});

export const checkAuth = async () => {
    try {
        const response = await authApi.get('/me');
        return response.status === 200;
    } catch (error) {
        return false;
    }
};

export const login = async (pin) => {
    const response = await authApi.post('/login', { pin });
    return response.data;
};

export const logout = async () => {
    const response = await authApi.post('/logout');
    return response.data;
};
