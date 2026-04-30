import axios from 'axios';
import { notification } from 'antd';

// Create Axios instance
const axiosInstance = axios.create({
  baseURL: '/api/v1',
  headers: { 'Content-Type': 'application/json' },
});

// Response Interceptor
axiosInstance.interceptors.response.use(
  (response) => {
    // Safely get the method and convert to uppercase
    const method = response.config.method ? response.config.method.toUpperCase() : '';

    // Trigger success notification only for mutation requests (POST, PUT, DELETE)
    if (['POST', 'PUT', 'DELETE'].includes(method)) {
      notification.success({
        message: 'Success',
        description: response.data.message || 'Operation completed successfully.',
        placement: 'topRight',
      });
    }
    return response;
  },
  (error) => {
    // Handle error response
    const errorMessage = error.response?.data?.message || 'An unexpected error occurred. Please try again.';

    notification.error({
      title: 'Error',
      description: errorMessage,
      placement: 'topRight',
    });


    return Promise.reject(error);
  }
);

export default axiosInstance;