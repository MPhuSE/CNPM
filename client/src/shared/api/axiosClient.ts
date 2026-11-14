import axios from 'axios';

export const axiosClient = axios.create({
  baseURL: 'http://localhost:8080/api', // Thay đổi port nếu backend nhóm chạy cổng khác
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

// Interceptor tự động đính kèm Access Token vào request
axiosClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('access_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Interceptor xử lý làm mới token (Refresh Token) khi hết hạn (401)
axiosClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      try {
        const refreshToken = localStorage.getItem('refresh_token');
        const res = await axios.post('http://localhost:8080/api/auth/refresh', {
          refreshToken,
        });

        const { accessToken } = res.data;
        localStorage.setItem('access_token', accessToken);

        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return axiosClient(originalRequest);
      } catch (refreshError) {
        localStorage.clear();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);