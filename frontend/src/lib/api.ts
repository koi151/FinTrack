import axios from 'axios';
import { User } from 'oidc-client-ts';

const api = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Request Interceptor, automatically attaches the Access Token to every outgoing request.
 */
api.interceptors.request.use(
  (config) => {
    // Retrieve OIDC user data from Session Storage
    const oidcStorage = sessionStorage.getItem(
      "oidc.user:http://localhost:8081/realms/fintrack-realm:fintrack-web"
    );

    // If token exists, attach it to the Authorization header
    if (oidcStorage) {
      const user = User.fromStorageString(oidcStorage);
      if (user?.access_token) {
        config.headers.Authorization = `Bearer ${user.access_token}`;
      }
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

/**
 * Response Interceptor, handles global errors like 401 Unauthorized (Token expired/invalid).
 */
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Redirect to login or refresh token logic here
      console.error("Unauthorized! Token might be expired.");
    }
    return Promise.reject(error);
  }
);

export default api;