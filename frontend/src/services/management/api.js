import axios from 'axios';

const API_BASE_URL = '/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 날짜를 LocalDateTime 형식으로 변환하는 함수
const formatDateTimeForApi = (dateString, isEndOfDay = false) => {
  // 이미 시간이 포함된 경우 그대로 반환
  if (dateString.includes('T')) {
    return dateString;
  }
  
  // 날짜만 있는 경우 시간 추가
  if (isEndOfDay) {
    return `${dateString}T23:59:59`;
  }
  return `${dateString}T00:00:00`;
};

// Bread API
export const breadApi = {
  getAll: () => api.get('/breads'),
  getById: (id) => api.get(`/breads/${id}`),
  create: (data) => api.post('/breads', data),
  update: (id, data) => api.put(`/breads/${id}`, data),
};

// Inventory API
export const inventoryApi = {
  getAll: () => api.get('/inventory'),
  getById: (id) => api.get(`/inventory/bread/${id}`),
  getLowStock: () => api.get('/inventory/low-stock'),
  update: (breadId, data) => api.put(`/inventory/bread/${breadId}`, data),
};

// Sales API
export const salesApi = {
  create: (data) => api.post('/sales', data),
  getDaily: (date) => api.get(`/sales/daily?date=${date}`),
  getByPeriod: (startDate, endDate) => {
    const start = formatDateTimeForApi(startDate);
    const end = formatDateTimeForApi(endDate, true);
    return api.get(`/sales?startDate=${start}&endDate=${end}`);
  },
  getSummary: (startDate, endDate) => {
    const start = formatDateTimeForApi(startDate);
    const end = formatDateTimeForApi(endDate, true);
    return api.get(`/sales/summary?startDate=${start}&endDate=${end}`);
  },
};

// AI Analysis API
export const aiAnalysisApi = {
  analyze: (data) => api.post('/ai-analysis/analyze', data),
  quickAnalysis: () => api.get('/ai-analysis/quick-analysis'),
};

export default api;

