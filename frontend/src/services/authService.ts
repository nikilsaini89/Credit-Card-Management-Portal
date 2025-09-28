import { BASE_URL } from '../constants/constants';
import axios from 'axios';
import type { User } from '../types/User';

export const login = async (email: string, password: string) => {
  return axios.post(`${BASE_URL}/auth/login`, { email, password }, {
  withCredentials: true
});

};

export const register = async (user: Omit<User, 'id'> & { password: string }) => {
  return axios.post(`${BASE_URL}/auth/register`, user);
};

export const logout = async (): Promise<void> => {
  const token = localStorage.getItem('token');
  try {
    await axios.post(`${BASE_URL}/auth/logout`, null, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
 
  } catch (error) {
    
    console.warn('Logout request failed:', error);
    
  } finally {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  }
};

export const refreshToken = async () => {
  try {
    const response = await axios.post(`${BASE_URL}/auth/refresh`, {}, {
      withCredentials: true 
    });
    const { accessToken, user } = response.data;

    localStorage.setItem('token', accessToken);
    localStorage.setItem('user', JSON.stringify(user));

    return accessToken;
  } catch (error) {
    console.error('Refresh token failed:', error);
    throw error;
  }
};

export const retryWithRefresh = async (error: any, originalRequest: any) => {
  if (error.response?.status === 403 && !originalRequest._retry) {
    originalRequest._retry = true;
    try {
      const newAccessToken = await refreshToken();
      originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`;
      console.log('Retrying request after token refresh');

      return axios(originalRequest);
    } catch (refreshError) {
      console.warn('Token refresh failed, forcing logout');
      logout(); 
      throw refreshError;
    }
  }
  throw error;
};


