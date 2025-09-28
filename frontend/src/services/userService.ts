import { BASE_URL } from '../constants/constants';
import axios from 'axios';
import type { User } from '../types/User';
import { retryWithRefresh } from './authService';

export async function getUserProfile(userId: number) {
  const token = localStorage.getItem('token');
  try {
    return await axios.get(`${BASE_URL}/profile/${userId}`, {
      headers: { Authorization: `Bearer ${token}` }
    });
  } catch (error: any) {
    return retryWithRefresh(error, {
      method: 'get',
      url: `${BASE_URL}/profile/${userId}`,
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
      _retry: false
    });
  }
}

export async function updateUserProfile(
  userId: number,
  payload: Omit<User, 'id' | 'email' | 'password'>
) {
  const token = localStorage.getItem('token');
  try {
    return await axios.put(`${BASE_URL}/profile/${userId}`, payload, {
      headers: { Authorization: `Bearer ${token}` }
    });
  } catch (error: any) {
    return retryWithRefresh(error, {
      method: 'put',
      url: `${BASE_URL}/profile/${userId}`,
      data: payload,
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
      _retry: false
    });
  }
}
