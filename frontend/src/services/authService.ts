import { BASE_URL } from '../constants/constants';
import axios from 'axios';
import type { User } from '../types/User';

export const login = async (email: string, password: string) => {
  return axios.post(`${BASE_URL}/auth/login`, { email, password });
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