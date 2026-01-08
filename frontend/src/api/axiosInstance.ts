import axios from 'axios';
import { notification } from 'antd';

const axiosInstance = axios.create({
  baseURL: '/api/v1',
  headers: { 'Content-Type': 'application/json' },
});

axiosInstance.interceptors.response.use(
  (response) => {
    const method = response.config.method?.toUpperCase();
    if (['POST', 'PUT', 'DELETE'].includes(method!)) {
      notification.success({
        message: 'Success',
        description: response.data.message || 'Action completed successfully.',
      });
    }
    return response;
  },
  (error) => {
    notification.error({
      message: 'Error',
      description: error.response?.data?.message || 'Something went wrong. Please try again.',
    });
    return Promise.reject(error);
  }
);

export default axiosInstance;