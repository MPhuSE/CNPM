import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';

// 1. Quản lý Token trong bộ nhớ (Memory Token) thay vì localStorage để bảo mật
let memoryAccessToken: string | null = null;

export const setMemoryAccessToken = (token: string | null) => {
  memoryAccessToken = token;
};

export const getMemoryAccessToken = () => {
  return memoryAccessToken;
};

// Tạo axios instance
const axiosClient = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

// 2. Interceptor cho Request: Đính kèm Access Token từ bộ nhớ
axiosClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    if (memoryAccessToken && config.headers) {
      config.headers.Authorization = `Bearer ${memoryAccessToken}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Biến hỗ trợ cơ chế Refresh Token đồng thời (Pending Refresh Promise / Queue)
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (token: string) => void;
  reject: (error: any) => void;
}> = [];

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token!);
    }
  });
  failedQueue = [];
};

// 3. Interceptor cho Response: Xử lý 401, 403 và Refresh Token
axiosClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    if (!error.response) {
      return Promise.reject(error);
    }

    const status = error.response.status;

    // Xử lý lỗi 403 (Forbidden)
    if (status === 403) {
      console.warn('Truy cập bị từ chối (403): Bạn không có quyền thực hiện hành động này.');
    }

    // Xử lý lỗi 401 (Unauthorized - Hết hạn token)
    if (status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            if (originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${token}`;
            }
            return axiosClient(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const response = await axios.post('/api/v1/auth/refresh-token', {}, { withCredentials: true });
        const newAccessToken = response.data.accessToken || response.data.token;

        setMemoryAccessToken(newAccessToken);
        processQueue(null, newAccessToken);

        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        }
        return axiosClient(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        setMemoryAccessToken(null);
        
        // Xóa sạch query cache / bộ nhớ đệm khi phiên bị thu hồi
        localStorage.clear();
        sessionStorage.clear();

        window.location.href = '/login';
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export default axiosClient;