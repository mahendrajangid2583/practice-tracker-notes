import axios from 'axios';

const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL || 'http://localhost:5001/api',
    withCredentials: true
});

// Add request interceptor
api.interceptors.request.use(request => {
    console.log('Starting Request:', request.method.toUpperCase(), request.url);
    return request;
});

// Add response interceptor
api.interceptors.response.use(response => {
    console.log('Response:', response.status, response.data);
    return response;
}, error => {
    console.error('API Error:', error.message);
    if (error.response) {
        console.error('Error Response:', error.response.status, error.response.data);
        if (error.response.status === 401) {
            window.dispatchEvent(new Event('auth:unauthorized'));
        }
    } else if (error.request) {
        console.error('No Response Received:', error.request);
    }
    return Promise.reject(error);
});

export const getCollections = async () => {
    const response = await api.get('/collections');
    return response.data;
};

export const createCollection = async (data) => {
    const response = await api.post('/collections', data);
    return response.data;
};

export const deleteCollection = async (id) => {
    const response = await api.delete(`/collections/${id}`);
    return response.data;
};

export const getCollectionDetails = async (id) => {
    const response = await api.get(`/collections/${id}`);
    return response.data;
};

export const updateCollection = async (id, data) => {
    const response = await api.patch(`/collections/${id}`, data);
    return response.data;
};

export const createTask = async (data) => {
    const response = await api.post('/tasks', data);
    return response.data;
};

export const updateTaskStatus = async (id, status) => {
    const response = await api.patch(`/tasks/${id}`, { status });
    return response.data;
};

export const updateTask = async (id, data) => {
    const response = await api.patch(`/tasks/${id}`, data);
    return response.data;
};

export const deleteTask = async (id) => {
    const response = await api.delete(`/tasks/${id}`);
    return response.data;
};

export const updateTaskNotes = async (id, notes) => {
    const response = await api.patch(`/tasks/${id}/notes`, { notes });
    return response.data;
};

export const getDailyTargets = async (date) => {
    const response = await api.get(`/daily-targets?date=${date}`);
    return response.data;
};

export const getActivityLog = async () => {
    const response = await api.get('/collections/activity');
    return response.data;
};

export const getTargetSettings = async () => {
    const response = await api.get('/target-settings');
    return response.data;
};

export const updateTargetSettings = async (data) => {
    const response = await api.put('/target-settings', data);
    return response.data;
};

export const searchOmni = async (query) => {
    const response = await api.get(`/search/omni?q=${encodeURIComponent(query)}`);
    return response.data;
};

export const searchGlobal = async (query) => {
    const response = await api.get(`/search/global?q=${encodeURIComponent(query)}`);
    return response.data;
};

export default api;
