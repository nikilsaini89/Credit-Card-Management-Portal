import { BASE_URL } from '../constants/constants';
import axios from 'axios';
import type { User } from '../types/User';

export function getUserProfile(userId: number) {
  const token = localStorage.getItem('token');
  return axios.get(`/api/profile/${userId}`, {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });
}

export const updateUserProfile = async (
  userId: number,
  payload: Omit<User, 'id' | 'email' | 'password'>
) => {
  const token = localStorage.getItem('token');

  return axios.put(`${BASE_URL}/profile/${userId}`, payload,{
     headers: {
      Authorization: `Bearer ${token}`
    }
  });
};
